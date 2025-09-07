package ar.edu.utn.dds.k3003.model;

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
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;
  private String hecho;

  public PdI(String id, String hecho){
    this.id = id;
    this.hecho = hecho;
  }

  public PdI() {

  }
}
