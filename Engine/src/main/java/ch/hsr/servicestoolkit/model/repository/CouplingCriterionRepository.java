package ch.hsr.servicestoolkit.model.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingType;

public interface CouplingCriterionRepository extends CrudRepository<CouplingCriterion, Long> {

	CouplingCriterion readByName(String name);

	List<CouplingCriterion> readByType(CouplingType type);

}
