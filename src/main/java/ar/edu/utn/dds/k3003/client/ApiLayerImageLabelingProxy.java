package ar.edu.utn.dds.k3003.client;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.EstadoSolicitudBorradoEnum;
import ar.edu.utn.dds.k3003.facades.dtos.SolicitudDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.HttpStatus;
import java.util.*;
import lombok.SneakyThrows;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ApiLayerImageLabelingProxy implements ImageLabelingService {

  private final String endpoint;
  private final ApiLayerImageLabelingClient service;

  public ApiLayerImageLabelingProxy(ObjectMapper om) {

    var env = System.getenv();
    this.endpoint = env.getOrDefault("URL_APILAYER", "https://api.apilayer.com/");

    var retrofit =
        new Retrofit.Builder()
            .baseUrl(this.endpoint)
            .addConverterFactory(JacksonConverterFactory.create(om))
            .build();

    this.service = retrofit.create(ApiLayerImageLabelingClient.class);
  }

  public ApiLayerImageLabelingProxy(String endpoint, ApiLayerImageLabelingClient service) {
    this.endpoint = endpoint;
    this.service = service;
  }

  @SneakyThrows
  @Override
  public List<String> procesarImagen(String imageUrl) {
    try {
        var env = System.getenv();
        Response<List<ApiLayerLabel>> execute = service.getImage(imageUrl, env.getOrDefault("APIKEY_APILAYER", "")).execute();
        if (execute.isSuccessful()) {
            var etiquetas = execute.body();
            return etiquetas.stream().map(etiq -> etiq.label).toList();
        }

        throw new RuntimeException("Error conectandose con ApiLayer");
    } catch (Exception ex) {
        return new ArrayList<String>();
    }
  }

}