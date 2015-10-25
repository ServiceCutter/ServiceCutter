package ch.hsr.servicestoolkit.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.MonoCouplingInstance;

public interface MonoCouplingInstanceRepository extends CrudRepository<MonoCouplingInstance, Long> {

	@Query("select distinct i from MonoCouplingInstance i join i.dataFields s where s.id in (select d.id from Model m join m.dataFields d where m.id = ?1) union select distinct i from DualCouplingInstance i join i.secondDataFields s where s.id in (select d.id from Model m join m.dataFields d where m.id = ?1)")
	Set<MonoCouplingInstance> findByModel(Long modelId);

}
