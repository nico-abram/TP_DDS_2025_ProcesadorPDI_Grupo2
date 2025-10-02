package ar.edu.utn.dds.k3003.client;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class OcrRetrofitClient {

    private final OcrApi api;
    private final String apiKey;

    public OcrRetrofitClient(String apiKey) {
        this.apiKey = apiKey;

        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.ocr.space") // base URL de OCR.Space
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        api = retrofit.create(OcrApi.class);
    }

    public String procesarImagen(String imageUrl) throws Exception {
        RequestBody apiKeyPart = RequestBody.create(apiKey, okhttp3.MultipartBody.FORM);
        RequestBody langPart = RequestBody.create("spa", okhttp3.MultipartBody.FORM);
        RequestBody overlayPart = RequestBody.create("false", okhttp3.MultipartBody.FORM);
        RequestBody urlPart = RequestBody.create(imageUrl, okhttp3.MultipartBody.FORM);

        var response = api.parseImage(apiKeyPart, langPart, overlayPart, urlPart).execute();

        if (!response.isSuccessful() || response.body() == null) {
            throw new RuntimeException("Error al llamar al servicio OCR: " + response.code());
        }

        return response.body().parsedResults.get(0).parsedText;
    }
}
