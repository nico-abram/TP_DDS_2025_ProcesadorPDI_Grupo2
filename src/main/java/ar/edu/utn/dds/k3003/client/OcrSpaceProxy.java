package ar.edu.utn.dds.k3003.client;

import ar.edu.utn.dds.k3003.services.OcrService;
import okhttp3.OkHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;

public class OcrSpaceProxy implements OcrService {

    private final OcrSpaceApiRetrofitClient api;
    private final String apiKey;

    public OcrSpaceProxy() {
        var env = System.getenv();
        this.apiKey = env.getOrDefault("APIKEY_OCR", "K86398197588957");
        //this.apiKey = "K86398197588957";//apiKey;

        OkHttpClient client = new OkHttpClient.Builder().build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(env.getOrDefault("URL_OCR", "https://api.ocr.space")) // base URL de OCR.Space
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build();

        api = retrofit.create(OcrSpaceApiRetrofitClient.class);
    }

    @Override
    public String procesarImagen(String imageUrl) throws Exception {
        try {
            var response = api.parseImage(apiKey, imageUrl).execute();

            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException("Error al llamar al servicio OCR: " + response.code());
            }

            return response.body().parsedResults.get(0).parsedText;
        } catch (Exception ex) {
            return "";
        }
    }
}
