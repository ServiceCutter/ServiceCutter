package ch.hsr.servicestoolkit.importer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.hsr.servicestoolkit.model.criteria.CouplingCriterionCharacteristic;
import ch.hsr.servicestoolkit.model.criteria.CouplingType;
import ch.hsr.servicestoolkit.model.repository.CouplingCriterionCharacteristicRepository;
import ch.hsr.servicestoolkit.model.repository.CouplingCriterionRepository;
import ch.hsr.servicestoolkit.model.repository.CouplingInstanceRepository;
import ch.hsr.servicestoolkit.model.repository.NanoentityRepository;
import ch.hsr.servicestoolkit.model.systemdata.CouplingInstance;
import ch.hsr.servicestoolkit.model.systemdata.InstanceType;
import ch.hsr.servicestoolkit.model.systemdata.Model;
import ch.hsr.servicestoolkit.model.systemdata.Nanoentity;

@Component
public class ModelCompleter {

	private CouplingInstanceRepository couplingInstanceRepository;
	private CouplingCriterionRepository couplingCriterionRepository;
	private CouplingCriterionCharacteristicRepository characteristicRepository;
	private NanoentityRepository nanoentityRepository;

	private final Logger log = LoggerFactory.getLogger(ModelCompleter.class);

	@Autowired
	public ModelCompleter(final CouplingCriterionRepository couplingCriterionRepository, final CouplingCriterionCharacteristicRepository characteristicRepository,
			final CouplingInstanceRepository couplingInstanceRepository, final NanoentityRepository nanoentityRepository) {
		this.couplingCriterionRepository = couplingCriterionRepository;
		this.characteristicRepository = characteristicRepository;
		this.couplingInstanceRepository = couplingInstanceRepository;
		this.nanoentityRepository = nanoentityRepository;
	}

	/**
	 * creates characteristics instances for the default characteristic of a
	 * coupling criteria with all nanoentities in the model for which no
	 * characteristic is defined.
	 */
	public void completeModelWithDefaultsForDistance(final Model model) {
		Set<Nanoentity> allNanoentitiesInModel = nanoentityRepository.findByModel(model);
		Map<String, Set<CouplingInstance>> instancesByCriterion = couplingInstanceRepository.findByModelGroupedByCriterionFilteredByCriterionType(model, CouplingType.COMPATIBILITY);

		// For every criterion
		for (Entry<String, Set<CouplingInstance>> criterion : instancesByCriterion.entrySet()) {
			Set<Nanoentity> definedNanoentities = criterion.getValue().stream().flatMap(instance -> instance.getAllNanoentities().stream()).collect(Collectors.toSet());
			// find missing nanoentities which need to have an instance
			Set<Nanoentity> missingNanoentities = allNanoentitiesInModel.stream().filter(nanoentity -> !definedNanoentities.contains(nanoentity)).collect(Collectors.toSet());

			if (!missingNanoentities.isEmpty()) {
				CouplingCriterionCharacteristic defaultCharacteristic = characteristicRepository.readByCouplingCriterionAndIsDefault(couplingCriterionRepository.readByName(criterion.getKey()), true);
				Set<CouplingInstance> instances = couplingInstanceRepository.findByModelAndCharacteristic(model, defaultCharacteristic);
				CouplingInstance instance;
				if (instances.size() == 1) {
					instance = instances.iterator().next();
				} else if (instances.size() == 0) {
					instance = new CouplingInstance(defaultCharacteristic, InstanceType.CHARACTERISTIC);
					model.addCouplingInstance(instance);
					instance.setName(defaultCharacteristic.getName());
					couplingInstanceRepository.save(instance);
				} else {
					throw new RuntimeException("only one instance per characteristic expected for distance criterion");
				}
				for (Nanoentity nanoentity : missingNanoentities) {
					instance.addNanoentity(nanoentity);
				}
				log.info("Complete model with instance of characteristic {} of criterion {} and nanoentities {}", defaultCharacteristic.getName(), criterion.getKey(), missingNanoentities);
			}

		}
	}

}
