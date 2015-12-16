package ch.hsr.servicecutter.model.repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicecutter.model.criteria.CouplingCriterionCharacteristic;
import ch.hsr.servicecutter.model.criteria.CouplingType;
import ch.hsr.servicecutter.model.userdata.CouplingInstance;
import ch.hsr.servicecutter.model.userdata.InstanceType;
import ch.hsr.servicecutter.model.userdata.UserSystem;

public interface CouplingInstanceRepository extends CrudRepository<CouplingInstance, Long> {

	Set<CouplingInstance> findByUserSystem(UserSystem userSystem);

	Set<CouplingInstance> findByUserSystemAndCharacteristic(UserSystem userSystem, CouplingCriterionCharacteristic characteristic);

	default Map<String, Set<CouplingInstance>> findByUserSystemGroupedByCriterion(final UserSystem userSystem) {
		return groupByCriterion(findByUserSystem(userSystem));
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

	default Map<String, Set<CouplingInstance>> findByUserSystemGroupedByCriterionFilteredByCriterionType(final UserSystem userSystem, final CouplingType type) {
		return groupByCriterion(findByUserSystem(userSystem).stream().filter(instance -> type.equals(instance.getCouplingCriterion().getType())).collect(Collectors.toSet()));
	}

	default Set<CouplingInstance> findByUserSystemAndCriterion(final UserSystem userSystem, final String criterion) {
		return findByUserSystem(userSystem).stream().filter(instance -> criterion.equals(instance.getCouplingCriterion().getName())).collect(Collectors.toSet());
	}

	Set<CouplingInstance> findByUserSystemAndInstanceType(final UserSystem userSystem, final InstanceType type);

}
