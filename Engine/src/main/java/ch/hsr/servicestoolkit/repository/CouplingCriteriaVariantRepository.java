package ch.hsr.servicestoolkit.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.CouplingCriterion;

public interface CouplingCriteriaVariantRepository extends CrudRepository<CouplingCriteriaVariant, Long> {

	public CouplingCriteriaVariant readByNameAndCouplingCriterion(String name, CouplingCriterion couplingCriterion);

	public List<CouplingCriteriaVariant> readByCouplingCriterion(CouplingCriterion couplingCriterion);

	public CouplingCriteriaVariant readByCouplingCriterionAndIsDefault(CouplingCriterion criterion, boolean isDefault);

}
