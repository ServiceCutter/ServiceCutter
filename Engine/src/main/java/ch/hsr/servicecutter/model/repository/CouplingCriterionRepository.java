package ch.hsr.servicecutter.model.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicecutter.model.criteria.CouplingCriterion;
import ch.hsr.servicecutter.model.criteria.CouplingType;

public interface CouplingCriterionRepository extends CrudRepository<CouplingCriterion, Long> {

	CouplingCriterion readByName(String name);

	List<CouplingCriterion> readByType(CouplingType type);

}
