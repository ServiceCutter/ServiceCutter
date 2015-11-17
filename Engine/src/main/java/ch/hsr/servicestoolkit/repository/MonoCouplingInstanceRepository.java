package ch.hsr.servicestoolkit.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.CouplingType;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;

public interface MonoCouplingInstanceRepository extends CrudRepository<MonoCouplingInstance, Long> {

	Set<MonoCouplingInstance> findByModel(Model model);

	Set<MonoCouplingInstance> findByModelAndVariant(Model model, CouplingCriteriaVariant variant);

	default Map<String, List<MonoCouplingInstance>> findByModelGroupedByCriterion(final Model model) {
		return groupByCriterion(findByModel(model));
	}

	default Map<String, List<MonoCouplingInstance>> groupByCriterion(final Set<MonoCouplingInstance> instances) {
		Map<String, List<MonoCouplingInstance>> instancesByCriterion = new HashMap<>();
		for (MonoCouplingInstance instance : instances) {
			String ccName = instance.getVariant().getCouplingCriterion().getName();
			if (instancesByCriterion.get(ccName) == null) {
				instancesByCriterion.put(ccName, new ArrayList<MonoCouplingInstance>());
			}
			instancesByCriterion.get(ccName).add(instance);
		}
		return instancesByCriterion;
	}

	default Map<String, List<MonoCouplingInstance>> findByModelGroupedByCriterionFilteredByCriterionType(final Model model, final CouplingType type) {
		return groupByCriterion(findByModel(model).stream().filter(instance -> type.equals(instance.getVariant().getCouplingCriterion().getType())).collect(Collectors.toSet()));
	}

	default Set<MonoCouplingInstance> findByModelAndCriterion(final Model model, final String criterion) {
		return findByModel(model).stream().filter(instance -> criterion.equals(instance.getVariant().getCouplingCriterion().getName())).collect(Collectors.toSet());
	}

}
