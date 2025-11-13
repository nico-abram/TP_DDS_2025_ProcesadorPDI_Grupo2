package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.EstadoSolicitudBorradoEnum;
import ar.edu.utn.dds.k3003.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.facades.dtos.SolicitudDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class PdIController {

  private final Fachada fachada;

  @Autowired
  public PdIController(Fachada fachada) {
    this.fachada = fachada;
  }

  @GetMapping("/pdis")
  public ResponseEntity<List<PdIDTO>> listarPdI(@RequestParam(value = "hecho", required = false) String hecho) {
    if (hecho == null) {
      // Si no se proporciona el parámetro "hecho", devuelve todos los PdIDTO
      return ResponseEntity.ok(fachada.buscarTodos());
    } else {
      // Si se proporciona el parámetro "hecho", devuelve los PdIDTO filtrados por "hecho"
      return ResponseEntity.ok(fachada.buscarPorHecho(hecho));
    }
  }

    @PostMapping("/pdis/{id}/process")
    public ResponseEntity<Integer> procesarPdi(@PathVariable String id) {
        fachada.procesarPdiDesdeWorker(id);
        return ResponseEntity.ok(1);
    }

  @GetMapping("/pdis/{id}")
  public ResponseEntity<PdIDTO> obtenerPdI(@PathVariable String id) {
    return ResponseEntity.ok(fachada.buscarPdIPorId(id));
  }

  @PostMapping("/pdis")
  public ResponseEntity<PdIDTO> crearPdI(@RequestBody PdIDTO pdIDTO) {
    return ResponseEntity.ok(fachada.procesar(pdIDTO));
  }

  @DeleteMapping("/todo")
  public ResponseEntity<String> borrarTodo() {
    return ResponseEntity.ok(fachada.borrarTodo());
  }

}
