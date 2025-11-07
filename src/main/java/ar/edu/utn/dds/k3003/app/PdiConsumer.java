// Paquete: ar.edu.utn.dds.k3003.app (o donde quieras)

import ar.edu.utn.dds.k3003.client.ImageLabelingService;
import ar.edu.utn.dds.k3003.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import ar.edu.utn.dds.k3003.services.OcrService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.rabbitmq.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

// ... (tus imports de OcrService, EtiquetadoService, PdIRepository, PdI, PdIDTO, Mongo...)

@Component
public class PdiConsumer {

    // El consumidor necesita sus propias dependencias
    private OcrService ocrService;
    private ImageLabelingService etiquetadoService;
    private PdIRepository pdiRepository;
    private ObjectMapper objectMapper;
    private MongoClient mongoClient; // Y el cliente de Mongo
    // ...

    @Autowired
    public PdiConsumer(OcrService ocrService, ImageLabelingService etiquetadoService,
                       PdIRepository pdiRepository, ObjectMapper objectMapper /*, ... inyecta Mongo */) {
        this.ocrService = ocrService;
        this.etiquetadoService = etiquetadoService;
        this.pdiRepository = pdiRepository;
        this.objectMapper = objectMapper;
        // ... (inicializa tu cliente de Mongo como en el ejemplo anterior)
    }

    /**
     * Este método se ejecutará UNA VEZ cuando Spring termine de arrancar.
     * Aquí es donde "encendemos" al obrero.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void iniciarConsumidor() {
        System.out.println(" [CONSUMIDOR] Iniciando consumidor de PDI...");

        try {
            Map<String, String> env = System.getenv();
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(env.get("MESSAGEBROKER_HOST"));
            factory.setUsername(env.get("MESSAGEBROKER_USERNAME"));
            factory.setPassword(env.get("MESSAGEBROKER_PASSWORD"));
            factory.setVirtualHost(env.get("MESSAGEBROKER_USERNAME"));

            String queueName = env.get("MESSAGEBROKER_QUEUE_NAME");

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            // El consumidor también declara la cola (para asegurar que exista)
            channel.queueDeclare(queueName, true, false, false, null);

            // Fair Dispatch: No me des más de 1 mensaje a la vez.
            channel.basicQos(1);

            System.out.println(" [*] Esperando mensajes. Este hilo se bloqueará.");

            // 2. Definir qué hacer con el mensaje (el trabajo real)
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String mensajeJson = new String(delivery.getBody(), StandardCharsets.UTF_8);

                try {
                    System.out.println(" [x] Recibido para procesar: " + mensajeJson);

                    // DESERIALIZAR
                    PdIDTO pdiDto = objectMapper.readValue(mensajeJson, PdIDTO.class);

                    // BUSCAR EL PDI
                    PdI pdi = pdiRepository.findById(pdiDto.id())
                            .orElseThrow(() -> new NoSuchElementException("No se encontró PDI " + pdiDto.id()));

                    // HACER TRABAJO LENTO
                    pdi.setContenido(ocrService.procesarImagen(pdi.getUrlImagen()));
                    try {
                        pdi.setEtiquetas(etiquetadoService.procesarImagen(pdiDto.urlImagen()));
                    } catch (Exception e) {
                        pdi.setEtiquetas(List.of());
                    }

                    // GUARDAR EN REPO
                    pdiRepository.save(pdi);

                    // ACTUALIZAR MONGODB
                    //actualizarMongoDb(pdi); // (Usa el mismo método private que te mostré antes)

                    System.out.println(" [✔] Procesado PDI: " + pdi.getId());

                    // AVISAR A RABBIT QUE TERMINÉ (ACK)
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                } catch (Exception e) {
                    System.err.println(" [!] Falla al procesar: " + mensajeJson + " - " + e.getMessage());
                    // NO HACEMOS ACK (o hacemos basicNack) para que el mensaje se re-encole (o vaya a dead-letter)
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false); // No re-encolar, o true si quieres que reintente
                }
            };

            // 4. Empezar a consumir (autoAck = false)
            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });

        } catch (Exception e) {
            // Si falla al iniciar (ej. RabbitMQ está caído), la app fallará al arrancar
            throw new RuntimeException("No se pudo iniciar el consumidor de PDI", e);
        }
    }

    // ... (pon aquí tu método private void actualizarMongoDb(PdI pdi) ... )
}