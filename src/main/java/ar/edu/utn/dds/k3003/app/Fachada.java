package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.repository.InMemoryPdIRepo;
import ar.edu.utn.dds.k3003.repository.PdIRepository;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class Fachada implements FachadaProcesadorPdI {

    private FachadaSolicitudes fachadaSolicitudes;

    @Getter private PdIRepository pdiRepository;

    private final AtomicLong generadorID = new AtomicLong(1);

    protected Fachada() {
        this.pdiRepository = new InMemoryPdIRepo();
    }

    @Autowired
    public Fachada(PdIRepository pdiRepository) {
        this.pdiRepository = pdiRepository;
    }

    @Override
    public void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes) {
        this.fachadaSolicitudes = fachadaSolicitudes;
    }

    @Override
    public PdIDTO procesar(PdIDTO pdiDTORecibido) {
        if (!fachadaSolicitudes.estaActivo(pdiDTORecibido.hechoId())) {
            throw new IllegalStateException("El hecho está inactivo o fue borrado");
        }

        PdI nuevoPdI = recibirPdIDTO(pdiDTORecibido);

        // Buscar duplicado a mano
        Optional<PdI> yaProcesado =
                pdiRepository.findByHechoId(nuevoPdI.getHechoId()).stream()
                        .filter(
                                p ->
                                        p.getDescripcion().equals(nuevoPdI.getDescripcion())
                                                && p.getLugar().equals(nuevoPdI.getLugar())
                                                && p.getMomento().equals(nuevoPdI.getMomento())
                                                && p.getContenido().equals(nuevoPdI.getContenido()))
                        .findFirst();

        if (yaProcesado.isPresent()) {
            return convertirADTO(yaProcesado.get());
        }

        nuevoPdI.setId(generadorID.getAndIncrement());
        nuevoPdI.setEtiquetas(etiquetar(nuevoPdI.getContenido()));
        pdiRepository.save(nuevoPdI);

        System.out.println(
                "Se guardó el PdI con ID "
                        + nuevoPdI.getId()
                        + " en hechoId: "
                        + nuevoPdI.getHechoId());

        PdIDTO pdiDTOAEnviar = convertirADTO(nuevoPdI);
        return pdiDTOAEnviar;
    }

    @Override
    public PdIDTO buscarPdIPorId(String idString) {
        Long id = Long.parseLong(idString);
        PdI pdi =
                pdiRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NoSuchElementException(
                                                "No se encontró el PdI con id: " + id));
        PdIDTO pdiDTO = convertirADTO(pdi);
        return pdiDTO;
    }

    @Override
    public List<PdIDTO> buscarPorHecho(String hechoId) {
        List<PdI> lista = pdiRepository.findByHechoId(hechoId);

        System.out.println("Buscando por hechoId: " + hechoId + " - Encontrados: " + lista.size());

        List<PdIDTO> listaPdiDTO =
                lista.stream().map(this::convertirADTO).collect(Collectors.toList());

        return listaPdiDTO;
    }

    public PdIDTO convertirADTO(PdI pdi) {
        return new PdIDTO(
                String.valueOf(pdi.getId()),
                pdi.getHechoId(),
                pdi.getDescripcion(),
                pdi.getLugar(),
                pdi.getMomento(),
                pdi.getContenido(),
                pdi.getEtiquetas());
    }

    public List<String> etiquetar(String contenido) {
        List<String> etiquetas = new ArrayList<>();
        if (contenido != null) {
            if (contenido.toLowerCase().contains("fuego")) {
                etiquetas.add("incendio");
            }

            if (contenido.toLowerCase().contains("agua")) {
                etiquetas.add("inundación");
            }
        }
        if (etiquetas.isEmpty()) {
            etiquetas.add("sin clasificar");
        }
        return etiquetas;
    }

    public PdI recibirPdIDTO(PdIDTO pdiDTO) {
        PdI nuevoPdI =
                new PdI(
                        pdiDTO.hechoId(),
                        pdiDTO.descripcion(),
                        pdiDTO.lugar(),
                        pdiDTO.momento(),
                        pdiDTO.contenido());
        return nuevoPdI;
    }

    //    @Override
    //    public List<PdIDTO> pdis() {
    //        return this.PdIRepository.findAll()
    //                .stream()
    //                .map(this::convertirADTO)
    //                .toList();
    //    }

}
