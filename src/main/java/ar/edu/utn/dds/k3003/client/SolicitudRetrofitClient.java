package ar.edu.utn.dds.k3003.client;

import ar.edu.utn.dds.k3003.facades.dtos.SolicitudDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Path;
import java.util.List;

public interface SolicitudRetrofitClient {

  @GET("hechos/{hechoId}/activo")
  Call<Boolean> get(@Path("hechoId") String hechoId);
}