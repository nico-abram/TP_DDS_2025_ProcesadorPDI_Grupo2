package ar.edu.utn.dds.k3003.services;

import ar.edu.utn.dds.k3003.client.OcrSpaceApiRetrofitClient;
import ar.edu.utn.dds.k3003.client.OcrResponse;
import okhttp3.MultipartBody;
import retrofit2.Response;

public interface OcrService {
    public String procesarImagen(String imageUrl) throws Exception;
}
