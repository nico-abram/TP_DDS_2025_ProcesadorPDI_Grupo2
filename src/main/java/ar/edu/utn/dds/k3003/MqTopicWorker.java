package ar.edu.utn.dds.k3003;


import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.config.JacksonConfig;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import ar.edu.utn.dds.k3003.dtos.PdIDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.client.*;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MqTopicWorker extends DefaultConsumer {

    private String exchangeName;
    private EntityManagerFactory entityManagerFactory;
    private final Fachada fachada;
    private ObjectMapper objectMapper;

    protected MqTopicWorker(Channel channel, String exchangeName, Fachada fachada) {
        super(channel);
        this.exchangeName = exchangeName;
        this.entityManagerFactory = entityManagerFactory;
        this.fachada = fachada;
        this.objectMapper = (new JacksonConfig()).objectMapper();
    }


    public void init() throws IOException {
        try {
            var channel = this.getChannel();

            var durable = true;
            channel.queueDeclare(exchangeName, durable, false, false, null);
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {
                    String message = new String(delivery.getBody(), "UTF-8");
                    System.out.println(" Recibido mensaje de subscripcion '" + message + "'");
                    PdIDTO pdi = objectMapper.readValue(message, PdIDTO.class);


                    Map<String, String> env = System.getenv();
                    String portString = env.getOrDefault("PORT", "8080");
                    Integer port = Integer.parseInt(portString);

                    URL url = new URL("http://localhost:"+port.toString()+"/api/pdis/"+pdi.id()+"/process");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setConnectTimeout(50000);
                    con.setReadTimeout(50000);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer content = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    con.disconnect();

                    //fachada.procesarPdiDesdeWorker(pdi.id());

                    System.out.println(" Procesado desde worker: '" + pdi.id() + "'");
                } catch (Exception e) {
                   System.out.println(" Error procesando hecho desde subscripcion: '" + e.getMessage() + "'");
                }
            };
            channel.basicConsume(exchangeName, true, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            System.out.println(" Error suscribiendose a exchange: '" + e.getMessage() + "'");
        }
    }
}