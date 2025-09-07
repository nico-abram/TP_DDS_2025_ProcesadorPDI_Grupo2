package ar.edu.utn.dds.k3003.repository;

import ar.edu.utn.dds.k3003.model.PdI;
import java.util.List;
import java.util.Optional;

public interface PdIRepository {
  public Optional<PdI> findById(String id);
  public List<PdI> findByHechoId(String hechoId);
  public List<PdI> findAll();
  public PdI save(PdI pdi);
  public void deleteAllInBatch();
}
