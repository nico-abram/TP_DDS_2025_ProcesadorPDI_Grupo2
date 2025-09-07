package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.client.SolicitudesProxy;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.repository.InMemoryPdIRepo;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class Fachada implements FachadaProcesadorPdI {

  private PdIRepository pdiRepository;
  private Integer pdiID = 0;
  private FachadaSolicitudes fachadaSolicitudes;
  private ObjectMapper objectMapper;

  public Fachada() {
    this.pdiRepository = new InMemoryPdIRepo();
  }

  @Autowired
  public Fachada(PdIRepository pdiRepository) {
    this.pdiRepository = pdiRepository;
    this.objectMapper = new ObjectMapper();
    this.fachadaSolicitudes = new SolicitudesProxy(objectMapper);
  }

  public PdIDTO procesar(PdIDTO pdiDto) throws IllegalStateException {


    if(fachadaSolicitudes.estaActivo(pdiDto.hechoId())) {
      if(buscarPorHecho(pdiDto.hechoId()).isEmpty()) {
        PdI pdiNuevo = new PdI(pdiDto);
        //pdiID++;
        this.pdiRepository.save(pdiNuevo);
        return pdiNuevo.dto();
      }
      else {
        return buscarPorHecho(pdiDto.hechoId()).get(0);
      }
    } else {
      throw new IllegalStateException("No esta activo");
    }
  }


  public PdIDTO buscarPdIPorId(String var1) throws NoSuchElementException {
    Optional<PdI> pdIOptional = this.pdiRepository.findById(var1);
    if (pdIOptional.isEmpty()) {
      throw new NoSuchElementException(pdIOptional + " no existe");
    }

    PdI pdi = pdIOptional.get();

    return pdi.dto();
  }


  public List<PdIDTO> buscarPorHecho(String var1) throws NoSuchElementException {

    List<PdI> pdIList = this.pdiRepository.findByHechoId(var1);

    return pdIList.stream().map(pdi -> pdi.dto()).toList();
  }

  public List<PdIDTO> buscarTodos() {
    List<PdI> pdIList = this.pdiRepository.findAll();

    return pdIList.stream().map(pdi -> pdi.dto()).toList();

  }


  public void setFachadaSolicitudes(FachadaSolicitudes var1) {
    fachadaSolicitudes = var1;
  }

  public String borrarTodo() {
    pdiRepository.deleteAllInBatch();
    return "Borrado exitosamente";
  }
}