package ar.edu.utn.dds.k3003.client;

import java.util.List;

public interface ImageLabelingService {

  public List<String> procesarImagen(byte[] imagen);

}