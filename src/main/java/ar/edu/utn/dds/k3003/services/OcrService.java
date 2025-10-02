package ar.edu.utn.dds.k3003.services;

import ar.edu.utn.dds.k3003.client.OcrApi;
import ar.edu.utn.dds.k3003.client.OcrResponse;
import okhttp3.MultipartBody;
import retrofit2.Response;

public class OcrService {

    private final OcrApi ocrApi;

    public OcrService(OcrApi ocrApi) {
        this.ocrApi = ocrApi;
    }

    public String procesarImagen(MultipartBody.Part imagen, String apiKey) throws Exception {
        Response<OcrResponse> response = ocrApi.processImage(imagen, apiKey).execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body().getParsedText();
        } else {
            throw new RuntimeException("Error en OCR API: " + response.errorBody().string());
        }
    }
}
