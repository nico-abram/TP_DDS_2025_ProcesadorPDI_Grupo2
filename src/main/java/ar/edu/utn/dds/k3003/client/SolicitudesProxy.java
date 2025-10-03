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

public class SolicitudesProxy {

  private final String endpoint;
  private final SolicitudRetrofitClient service;

  public SolicitudesProxy(ObjectMapper objectMapper) {

    var env = System.getenv();
    this.endpoint = env.getOrDefault("URL_SOLICITUDES", "https://two025-tp-dds-solicitudes.onrender.com/");

    var retrofit =
        new Retrofit.Builder()
            .baseUrl(this.endpoint)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build();

    this.service = retrofit.create(SolicitudRetrofitClient.class);
  }

  public SolicitudesProxy(String endpoint, SolicitudRetrofitClient service) {
    this.endpoint = endpoint;
    this.service = service;
  }


  @SneakyThrows
  public boolean estaActivo(String hechoId) throws java.io.IOException {
    Response<Boolean> execute = service.get(hechoId).execute();
    if (execute.isSuccessful()) {
        var activo = execute.body();
        return activo.booleanValue();
//        return sols.isEmpty() || !sols.stream().anyMatch(s -> s.estado().equals(EstadoSolicitudBorradoEnum.ACEPTADA));
    }
    if(execute.code() == HttpStatus.NOT_FOUND.getCode()) {
      throw new NoSuchElementException("No se encontro el hecho");
    }
    throw new RuntimeException("Error conectandose con solicitudes");
  }
}