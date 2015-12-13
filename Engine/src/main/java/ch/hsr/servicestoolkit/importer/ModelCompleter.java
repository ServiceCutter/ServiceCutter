package ch.hsr.servicestoolkit.importer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic;
import ch.hsr.servicestoolkit.model.CouplingType;
import ch.hsr.servicestoolkit.model.NanoEntity;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.repository.CouplingCriteriaVariantRepository;
import ch.hsr.servicestoolkit.repository.CouplingCriterionRepository;
import ch.hsr.servicestoolkit.repository.DataFieldRepository;
import ch.hsr.servicestoolkit.repository.MonoCouplingInstanceRepository;

@Component
public class ModelCompleter {

	private MonoCouplingInstanceRepository couplingInstanceRepository;
	private CouplingCriterionRepository couplingCriterionRepository;
	private CouplingCriteriaVariantRepository variantRepository;
	private DataFieldRepository dataFieldRepository;

	private final Logger log = LoggerFactory.getLogger(ModelCompleter.class);

	@Autowired
	public ModelCompleter(final CouplingCriterionRepository couplingCriterionRepository, final CouplingCriteriaVariantRepository variantRepository,
			final MonoCouplingInstanceRepository couplingInstanceRepository, final DataFieldRepository dataFieldRepository) {
		this.couplingCriterionRepository = couplingCriterionRepository;
		this.variantRepository = variantRepository;
		this.couplingInstanceRepository = couplingInstanceRepository;
		this.dataFieldRepository = dataFieldRepository;
	}

	/**
	 * creates variants instances for the default variant of a coupling criteria
	 * with all fields in the model for which no variant is defined.
	 */
	public void completeModelWithDefaultsForDistance(final Model model) {
		Set<NanoEntity> allFieldsInModel = dataFieldRepository.findByModel(model);
		Map<String, Set<MonoCouplingInstance>> instancesByCriterion = couplingInstanceRepository.findByModelGroupedByCriterionFilteredByCriterionType(model, CouplingType.COMPATIBILITY);

		// For every criterion
		for (Entry<String, Set<MonoCouplingInstance>> criterion : instancesByCriterion.entrySet()) {
			Set<NanoEntity> definedFields = criterion.getValue().stream().flatMap(instance -> instance.getAllFields().stream()).collect(Collectors.toSet());
			// find missing fields which need to have an instance
			Set<NanoEntity> missingFields = allFieldsInModel.stream().filter(field -> !definedFields.contains(field)).collect(Collectors.toSet());

			if (!missingFields.isEmpty()) {
				CouplingCriterionCharacteristic defaultVariant = variantRepository.readByCouplingCriterionAndIsDefault(couplingCriterionRepository.readByName(criterion.getKey()), true);
				Set<MonoCouplingInstance> instances = couplingInstanceRepository.findByModelAndVariant(model, defaultVariant);
				MonoCouplingInstance instance;
				if (instances.size() == 1) {
					instance = instances.iterator().next();
				} else if (instances.size() == 0) {
					instance = defaultVariant.createInstance();
					instance.setModel(model);
					instance.setName(defaultVariant.getName());
					couplingInstanceRepository.save(instance);
				} else {
					throw new RuntimeException("only one instance per variant expected for distance criterion");
				}
				for (NanoEntity field : missingFields) {
					instance.addDataField(field);
				}
				log.info("Complete model with instance of variant {} of criterion {} and fields {}", defaultVariant.getName(), criterion.getKey(), missingFields);
			}

		}
	}

}
