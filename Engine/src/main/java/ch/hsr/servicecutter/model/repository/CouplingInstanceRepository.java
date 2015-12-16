package ch.hsr.servicecutter.model.repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicecutter.model.criteria.CouplingCriterionCharacteristic;
import ch.hsr.servicecutter.model.criteria.CouplingType;
import ch.hsr.servicecutter.model.systemdata.CouplingInstance;
import ch.hsr.servicecutter.model.systemdata.InstanceType;
import ch.hsr.servicecutter.model.systemdata.Model;

public interface CouplingInstanceRepository extends CrudRepository<CouplingInstance, Long> {

	Set<CouplingInstance> findByModel(Model model);

	Set<CouplingInstance> findByModelAndCharacteristic(Model model, CouplingCriterionCharacteristic characteristic);

	default Map<String, Set<CouplingInstance>> findByModelGroupedByCriterion(final Model model) {
		return groupByCriterion(findByModel(model));
	}

	default Map<String, Set<CouplingInstance>> groupByCriterion(final Set<CouplingInstance> instances) {
		Map<String, Set<CouplingInstance>> instancesByCriterion = new HashMap<>();
		for (CouplingInstance instance : instances) {
			String ccName = instance.getCouplingCriterion().getName();
			if (instancesByCriterion.get(ccName) == null) {
				instancesByCriterion.put(ccName, new HashSet<CouplingInstance>());
			}
			instancesByCriterion.get(ccName).add(instance);
		}
		return instancesByCriterion;
	}

	default Map<String, Set<CouplingInstance>> findByModelGroupedByCriterionFilteredByCriterionType(final Model model, final CouplingType type) {
		return groupByCriterion(findByModel(model).stream().filter(instance -> type.equals(instance.getCouplingCriterion().getType())).collect(Collectors.toSet()));
	}

	default Set<CouplingInstance> findByModelAndCriterion(final Model model, final String criterion) {
		return findByModel(model).stream().filter(instance -> criterion.equals(instance.getCouplingCriterion().getName())).collect(Collectors.toSet());
	}

	Set<CouplingInstance> findByModelAndInstanceType(final Model model, final InstanceType type);

}
