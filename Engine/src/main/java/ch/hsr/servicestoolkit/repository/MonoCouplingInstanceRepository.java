package ch.hsr.servicestoolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.MonoCouplingInstance;

public interface MonoCouplingInstanceRepository extends CrudRepository<MonoCouplingInstance, Long> {

	@Query("select distinct i from MonoCouplingInstance i join i.dataFields s where s.id in (select d.id from Model m join m.dataFields d where m.id = ?1)")
	List<MonoCouplingInstance> findByModel(Long modelId);

}
