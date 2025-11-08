package ar.edu.utn.dds.k3003.app;

import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.rabbitmq.client.*;

import ar.edu.utn.dds.k3003.client.OcrSpaceProxy;
import ar.edu.utn.dds.k3003.client.SolicitudesProxy;
import ar.edu.utn.dds.k3003.client.ApiLayerImageLabelingProxy;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import ar.edu.utn.dds.k3003.services.OcrService;
import ar.edu.utn.dds.k3003.client.ImageLabelingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import ar.edu.utn.dds.k3003.config.JacksonConfig;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.web.client.RestTemplate;

@Service
public class Fachada {

  private PdIRepository pdiRepository;
  private Integer pdiID = 0;
  private SolicitudesProxy fachadaSolicitudes;
  private OcrService ocrService;
  private ImageLabelingService etiquetadoService;
  private ObjectMapper objectMapper;

  @Autowired
  public Fachada(PdIRepository pdiRepository) {
    this.pdiRepository = pdiRepository;
    this.objectMapper = (new JacksonConfig()).objectMapper();
    this.fachadaSolicitudes = new SolicitudesProxy(objectMapper);
    this.etiquetadoService = new ApiLayerImageLabelingProxy(objectMapper);
    this.ocrService = new OcrSpaceProxy();
  }

  @SneakyThrows
  public PdIDTO procesar(PdIDTO pdiDto) throws IllegalStateException {

    var activo = false;
    try {
      activo = fachadaSolicitudes.estaActivo(pdiDto.hechoId());
    } catch(IOException ex) {
      throw new IllegalStateException("Error llamando a solicitudes");
    }

    if(activo) {
      if(buscarPorHecho(pdiDto.hechoId()).isEmpty()) {
        //procesarImagen
        PdI pdiNuevo = new PdI(pdiDto);

        //pdiID++;
        this.pdiRepository.save(pdiNuevo);
        //encolar
        this.encolarPendienteDeProcesamiento(pdiNuevo);

        return pdiNuevo.dto();
      }
      else {
        return buscarPorHecho(pdiDto.hechoId()).get(0);
      }
    } else {
      throw new IllegalStateException("No esta activo");
    }
  }

  public void procesarPdiDesdeWorker(PdIDTO pdiSinEtiquetado) {
      Optional<PdI> pdIOptional = this.pdiRepository.findById(pdiSinEtiquetado.id());
      if (pdIOptional.isEmpty()) {
          throw new NoSuchElementException(pdIOptional + " no existe");
      }

      PdI pdi = pdIOptional.get();

      try {
        pdi.setContenido(ocrService.procesarImagen(pdi.getUrlImagen()));
      } catch (Exception e) {
          System.out.println("Error llamando a la api de OCR");
          pdi.setContenido("");
      }
      try {
          pdi.etiquetas = etiquetadoService.procesarImagen(pdi.getUrlImagen());
      } catch (Exception e) {
          System.out.println("Error llamando a la api de etiquetado");
          pdi.etiquetas = List.of();
      }

      this.pdiRepository.save(pdi);

      // Agregar en mongodb
      Map<String, String> env = System.getenv();
      try (MongoClient mongoClient = MongoClients.create(env.get("MONGODB_URI"))) {
          MongoDatabase database = mongoClient.getDatabase("busqueda_hechos");
          MongoCollection<Document> collection = database.getCollection("busqueda_hechos");

          Document query = new Document("hecho_id", new Document("$eq", pdi.getHechoId()));
          Document hecho = collection.find(query).first();

          // List<String> tags = hecho.getList("tags", String.class);
          List<String> tags = (List<String>)hecho.get("tags");
          tags.addAll(Arrays.asList(pdi.getDescripcion().split("\\s+")));
          tags.addAll(Arrays.asList(pdi.getLugar().split("\\s+")));
          tags.addAll(Arrays.asList(pdi.getContenido().split("\\s+")));
          tags.addAll(pdi.getEtiquetas());
          hecho.append("tags", tags);

          // Document doc1 = new Document("hecho_id", guardado.getId()).append("tags", tags).append("titulo", guardado.getString("titulo"));
          collection.replaceOne(query, hecho);

      }
  }

  private void encolarPendienteDeProcesamiento(PdI pdiNuevo) {
    try {
      Map<String, String> env = System.getenv();
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(env.get("MESSAGEBROKER_HOST"));
      factory.setUsername(env.get("MESSAGEBROKER_USERNAME"));
      factory.setPassword(env.get("MESSAGEBROKER_PASSWORD"));
      // En el plan más barato, el VHOST == USER
      factory.setVirtualHost(env.get("MESSAGEBROKER_USERNAME"));
      String exchangeName = env.get("MESSAGEBROKER_QUEUE_NAME");

      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();

      String mensajeJson = objectMapper.writeValueAsString(pdiNuevo.dto());

      channel.exchangeDeclare(exchangeName,  BuiltinExchangeType.FANOUT, true, false, false, Map.of(
              "key", "value"
      ));

      channel.basicPublish(exchangeName, "", null, mensajeJson.getBytes(StandardCharsets.UTF_8));

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public PdIDTO buscarPdIPorId(String var1) throws NoSuchElementException {
    Optional<PdI> pdIOptional = this.pdiRepository.findById(var1);
    if (pdIOptional.isEmpty()) {
      throw new NoSuchElementException(pdIOptional + " no existe");
    }

    PdI pdi = pdIOptional.get();

    return pdi.dto();
  }


  public List<PdIDTO> buscarPorHecho(String var1) throws NoSuchElementException {

    List<PdI> pdIList = this.pdiRepository.findByHechoId(var1);

    return pdIList.stream().map(pdi -> pdi.dto()).toList();
  }

  public List<PdIDTO> buscarTodos() {
    List<PdI> pdIList = this.pdiRepository.findAll();

    return pdIList.stream().map(pdi -> pdi.dto()).toList();

  }


  public void setFachadaSolicitudes(FachadaSolicitudes var1) {

  }

  public String borrarTodo() {
    pdiRepository.deleteAllInBatch();
    return "Borrado exitosamente";
  }
}