package ch.hsr.servicestoolkit.solver.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic;
import ch.hsr.servicestoolkit.model.CouplingInstance;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.Nanoentity;
import ch.hsr.servicestoolkit.model.repository.CouplingCriterionCharacteristicRepository;
import ch.hsr.servicestoolkit.model.repository.CouplingCriterionRepository;
import ch.hsr.servicestoolkit.model.repository.CouplingInstanceRepository;
import ch.hsr.servicestoolkit.model.repository.NanoentityRepository;
import ch.hsr.servicestoolkit.score.relations.EntityPair;
import ch.hsr.servicestoolkit.score.relations.Score;
import ch.hsr.servicestoolkit.solver.Service;
import ch.hsr.servicestoolkit.solver.SolverResult;

@Component
public class ServiceCutAnalyzer {

	private NanoentityRepository nanoentityRepository;
	private CouplingInstanceRepository couplingInstanceRepository;
	private CouplingCriterionCharacteristicRepository characteristicRepository;
	private CouplingCriterionRepository couplingCriterionRepository;

	private Logger log = LoggerFactory.getLogger(ServiceCutAnalyzer.class);

	@Autowired
	public ServiceCutAnalyzer(final NanoentityRepository nanoentityRepository, final CouplingInstanceRepository couplingInstanceRepository,
			final CouplingCriterionCharacteristicRepository characteristicRepository, final CouplingCriterionRepository couplingCriterionRepository) {
		this.nanoentityRepository = nanoentityRepository;
		this.couplingInstanceRepository = couplingInstanceRepository;
		this.characteristicRepository = characteristicRepository;
		this.couplingCriterionRepository = couplingCriterionRepository;

	}

	private Nanoentity findNanoentity(final String fieldName, final Model model) {
		Nanoentity nanoentity;
		if (fieldName.contains(".")) {
			String[] splittedName = fieldName.split("\\.");
			nanoentity = nanoentityRepository.findByContextAndNameAndModel(splittedName[0], splittedName[1], model);
		} else {
			nanoentity = nanoentityRepository.findByNameAndModel(fieldName, model);
		}
		return nanoentity;

	}

	public void analyseResult(final SolverResult solverResult, final Map<EntityPair, Map<String, Score>> scores, final Model model) {
		// use case responsibility
		final Map<Service, List<CouplingInstance>> useCaseResponsibilites = getUseCaseResponsibilites(solverResult.getServices(), model);
		solverResult.setUseCaseResponsibility(transformResponsibilityMap(useCaseResponsibilites));
		for (Entry<String, List<String>> responsibility : solverResult.getUseCaseResponsibility().entrySet()) {
			log.info("attach use cases {} to service {}", responsibility.getValue().toString(), responsibility.getKey());
		}

		// relation scores and shared Data Fields
		List<Service> serviceList = new ArrayList<>(solverResult.getServices());
		List<ServiceRelation> relations = new ArrayList<>();

		for (int a = 0; a < serviceList.size() - 1; a++) {
			for (int b = a + 1; b < serviceList.size(); b++) {
				ServiceTuple serviceTuple = new ServiceTuple(serviceList.get(a).getName(), serviceList.get(b).getName());
				Double score = getProximityScoreFor(serviceList.get(a), serviceList.get(b), scores, model);
				final List<String> sharedFiels = getSharedFields(serviceList.get(a), serviceList.get(b), useCaseResponsibilites);
				if (score > 0) {
					log.info("create service relation for services {} and {} with score {} and fields {}", serviceList.get(a).getName(), serviceList.get(b).getName(), score, sharedFiels.toString());
					relations.add(new ServiceRelation(sharedFiels, score, serviceTuple.getServiceA(), serviceTuple.getServiceB()));
				}
			}
		}
		solverResult.setRelations(relations);

	}

	private List<String> getSharedFields(final Service serviceA, final Service serviceB, final Map<Service, List<CouplingInstance>> useCaseResponsibilites) {
		// use set to avoid duplicates
		Set<String> result = new HashSet<String>();
		result.addAll(getSharedFields(useCaseResponsibilites.get(serviceA), serviceB));
		result.addAll(getSharedFields(useCaseResponsibilites.get(serviceB), serviceA));
		return new ArrayList<String>(result);
	}

	private List<String> getSharedFields(final List<CouplingInstance> primaryUseCases, final Service otherService) {
		// for all use cases of primaryService, collect the fields placed in
		// otherService and return it as StringList
		if (primaryUseCases == null || primaryUseCases.isEmpty()) {
			return Collections.emptyList();
		}
		return primaryUseCases.stream().flatMap(instance -> instance.getAllNanoentities().stream().filter(field -> otherService.getNanoentities().contains(field.getContextName())))
				.map(field -> field.getContextName()).collect(Collectors.toList());
	}

	private Map<String, List<String>> transformResponsibilityMap(final Map<Service, List<CouplingInstance>> useCaseResponsibilites) {
		Map<String, List<String>> result = new HashMap<>();
		for (Entry<Service, List<CouplingInstance>> responsibility : useCaseResponsibilites.entrySet()) {
			if (responsibility.getKey() != null) {
				result.put(responsibility.getKey().getName(), responsibility.getValue().stream().map(instance -> instance.getName()).collect(Collectors.toList()));
			}
		}
		return result;
	}

	private Map<Service, List<CouplingInstance>> getUseCaseResponsibilites(final Set<Service> set, final Model model) {
		Map<Service, List<CouplingInstance>> useCaseResponsibilites = new HashMap<>();
		CouplingCriterion criterion = couplingCriterionRepository.readByName(CouplingCriterion.SEMANTIC_PROXIMITY);
		CouplingCriterionCharacteristic useCaseCharacteristic = characteristicRepository.readByNameAndCouplingCriterion(CouplingCriterionCharacteristic.SHARED_FIELD_ACCESS, criterion);
		for (CouplingInstance instance : couplingInstanceRepository.findByModelAndCharacteristic(model, useCaseCharacteristic)) {
			CouplingInstance dualInstance = instance;
			Service responsibleService = getResponsibleService(set, dualInstance);
			Assert.notNull(responsibleService, "Responsible service for " + dualInstance.getName() + " not found!");
			if (useCaseResponsibilites.get(responsibleService) == null) {
				useCaseResponsibilites.put(responsibleService, new ArrayList<>());
			}
			useCaseResponsibilites.get(responsibleService).add(dualInstance);
		}
		return useCaseResponsibilites;
	}

	private Service getResponsibleService(final Set<Service> set, final CouplingInstance dualInstance) {
		final double SCORE_WRITE = 1d;
		final double SCORE_READ = 0.25d;
		Service responsibleService = null;
		Double highestScore = 0d;
		for (final Service service : set) {
			final long numberOfFieldsWritten = dualInstance.getSecondNanoentities().stream().filter(field -> service.getNanoentities().contains(field.getContextName())).count();
			final long numberOfFieldsRead = dualInstance.getNanoentities().stream().filter(field -> service.getNanoentities().contains(field.getContextName())).count();
			final double score = numberOfFieldsRead * SCORE_READ + numberOfFieldsWritten * SCORE_WRITE;
			if (score > highestScore) {
				highestScore = score;
				responsibleService = service;
			}
		}
		return responsibleService;
	}

	private Double getProximityScoreFor(final Service serviceA, final Service serviceB, final Map<EntityPair, Map<String, Score>> scores, final Model model) {
		Double score = 0d;
		for (String fieldA : serviceA.getNanoentities()) {
			for (String fieldB : serviceB.getNanoentities()) {
				EntityPair fieldTuple = new EntityPair(findNanoentity(fieldA, model), findNanoentity(fieldB, model));
				final Map<String, Score> scoresByTuple = scores.get(fieldTuple);
				if (scoresByTuple == null) {
					continue;
				}
				Score semanticProximityScore = scoresByTuple.get(CouplingCriterion.SEMANTIC_PROXIMITY);
				if (semanticProximityScore != null) {
					score += semanticProximityScore.getPrioritizedScore();
				}
			}
		}
		return score;
	}

}
