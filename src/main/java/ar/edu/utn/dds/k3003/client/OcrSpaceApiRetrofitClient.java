package ar.edu.utn.dds.k3003.client;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OcrSpaceApiRetrofitClient {

    @GET("parse/imageurl") // endpoint base de OCR.Space
    Call<OcrResponse> parseImage(
            @Query("apikey") String apiKey,
            @Query("url") String imageUrl
    );
}
