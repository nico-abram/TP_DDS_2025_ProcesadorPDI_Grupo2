package ar.edu.utn.dds.k3003.client;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface OcrApi {

    @Multipart
    @POST("/parse/image") // endpoint base de OCR.Space
    Call<OcrResponse> parseImage(
            @Part("apikey") RequestBody apiKey,
            @Part("language") RequestBody language,
            @Part("isOverlayRequired") RequestBody isOverlayRequired,
            @Part("url") RequestBody imageUrl
    );
}
