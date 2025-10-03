package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.dtos.PdIDTO;
import java.util.ArrayList;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Data
@Entity
public class PdI {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String id;
  private String hechoId;
  private String descripcion;
  private String lugar;
  private LocalDateTime momento;
  @Column(name = "contenido", length = 1048576)
  private String contenido;
  private String urlImagen;

  @ElementCollection
  @CollectionTable(name = "pdi_etiquetas", joinColumns = @JoinColumn(name = "pdi_id"))
  @Column(name = "etiqueta")
  public List<String> etiquetas = new ArrayList<>();

/*
  public PdI(String id, String hecho){
    this.id = id;
    this.hecho = hecho;
  }
*/
  public PdI(PdIDTO dto){
//    this.id = dto.id();
    this.hechoId = dto.hechoId();
    this.lugar = dto.lugar();
    this.descripcion = dto.descripcion();
    this.momento = dto.momento();
    this.contenido = dto.contenido();
    this.etiquetas = dto.etiquetas();
    this.urlImagen = dto.urlImagen();
  }

  public PdI() {

  }

  public PdIDTO dto() {
     return new PdIDTO(id, hechoId, descripcion, lugar, momento, contenido, etiquetas, urlImagen);
  }
}
