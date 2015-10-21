package ch.hsr.servicestoolkit.repository;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.CouplingCriterion;

public interface CouplingCriteriaVariantRepository extends CrudRepository<CouplingCriteriaVariant, Long> {

	public CouplingCriteriaVariant readByNameAndCouplingCriterion(String name, CouplingCriterion couplingCriterion);

}
