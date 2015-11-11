package ch.hsr.servicestoolkit.repository;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;

public interface MonoCouplingInstanceRepository extends CrudRepository<MonoCouplingInstance, Long> {

	Set<MonoCouplingInstance> findByModel(Model model);

	Set<MonoCouplingInstance> findByModelAndNameAndVariant(Model model, String name, CouplingCriteriaVariant variant);
}
