package ch.hsr.servicecutter.model.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicecutter.model.criteria.CouplingCriterion;
import ch.hsr.servicecutter.model.criteria.CouplingCriterionCharacteristic;

public interface CouplingCriterionCharacteristicRepository extends CrudRepository<CouplingCriterionCharacteristic, Long> {

	public CouplingCriterionCharacteristic readByNameAndCouplingCriterion(String name, CouplingCriterion couplingCriterion);

	public List<CouplingCriterionCharacteristic> readByCouplingCriterion(CouplingCriterion couplingCriterion);

	public CouplingCriterionCharacteristic readByCouplingCriterionAndIsDefault(CouplingCriterion criterion, boolean isDefault);

}
