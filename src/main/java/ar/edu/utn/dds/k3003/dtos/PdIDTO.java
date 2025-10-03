package ar.edu.utn.dds.k3003.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record PdIDTO(String id,String hechoId, String descripcion, String lugar, LocalDateTime momento,
                     String contenido, List<String> etiquetas, String urlImagen) {

  public PdIDTO(String id,String hechoId) {
    this(id,hechoId, null, null, null, null, List.of(), null);
  }

}