package ar.edu.utn.dds.k3003.client;

import ar.edu.utn.dds.k3003.facades.dtos.SolicitudDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Body;
import retrofit2.http.Query;
import okhttp3.RequestBody;
import java.util.List;

public interface ApiLayerImageLabelingClient {

  @GET("image_labeling/url")
  Call<List<ApiLayerLabel>> getImage(@Query("url") String imageUrl, @Header("apikey") String apikey);
}