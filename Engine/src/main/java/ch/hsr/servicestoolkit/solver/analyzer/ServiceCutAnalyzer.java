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

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.DualCouplingInstance;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.repository.CouplingCriteriaVariantRepository;
import ch.hsr.servicestoolkit.repository.CouplingCriterionRepository;
import ch.hsr.servicestoolkit.repository.DataFieldRepository;
import ch.hsr.servicestoolkit.repository.MonoCouplingInstanceRepository;
import ch.hsr.servicestoolkit.score.relations.FieldPair;
import ch.hsr.servicestoolkit.score.relations.Score;
import ch.hsr.servicestoolkit.solver.Service;
import ch.hsr.servicestoolkit.solver.SolverResult;

@Component
public class ServiceCutAnalyzer {

	private DataFieldRepository dataFieldRepository;
	private MonoCouplingInstanceRepository monoCouplingInstanceRepository;
	private CouplingCriteriaVariantRepository variantRepository;
	private CouplingCriterionRepository couplingCriterionRepository;

	private Logger log = LoggerFactory.getLogger(ServiceCutAnalyzer.class);

	@Autowired
	public ServiceCutAnalyzer(final DataFieldRepository dataFieldRepository, final MonoCouplingInstanceRepository monoCouplingInstanceRepository,
			final CouplingCriteriaVariantRepository variantRepository, final CouplingCriterionRepository couplingCriterionRepository) {
		this.dataFieldRepository = dataFieldRepository;
		this.monoCouplingInstanceRepository = monoCouplingInstanceRepository;
		this.variantRepository = variantRepository;
		this.couplingCriterionRepository = couplingCriterionRepository;

	}

	private DataField findDataField(final String fieldName, final Model model) {
		DataField dataField;
		if (fieldName.contains(".")) {
			String[] splittedName = fieldName.split("\\.");
			dataField = dataFieldRepository.findByContextAndNameAndModel(splittedName[0], splittedName[1], model);
		} else {
			dataField = dataFieldRepository.findByNameAndModel(fieldName, model);
		}
		return dataField;

	}

	public void analyseResult(final SolverResult solverResult, final Map<FieldPair, Map<String, Score>> scores, final Model model) {
		// use case responsibility
		final Map<Service, List<DualCouplingInstance>> useCaseResponsibilites = getUseCaseResponsibilites(solverResult.getServices(), model);
		solverResult.setUseCaseResponsibility(transformResponsibilityMap(useCaseResponsibilites));
		for (Entry<Service, List<DualCouplingInstance>> responsibility : useCaseResponsibilites.entrySet()) {
			log.info("attach use cases {} to service {}", responsibility.getValue().toString(), responsibility.getKey().getName());
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
					log.info("create service relation for services {} and {} with score {} and fields {}", serviceList.get(a).getName(), serviceList.get(b).getName(), score,
							sharedFiels.toString());
					relations.add(new ServiceRelation(sharedFiels, score, serviceTuple.getServiceA(), serviceTuple.getServiceB()));
				}
			}
		}
		solverResult.setRelations(relations);

	}

	private List<String> getSharedFields(final Service serviceA, final Service serviceB, final Map<Service, List<DualCouplingInstance>> useCaseResponsibilites) {
		// use set to avoid duplicates
		Set<String> result = new HashSet<String>();
		result.addAll(getSharedFields(useCaseResponsibilites.get(serviceA), serviceB));
		result.addAll(getSharedFields(useCaseResponsibilites.get(serviceB), serviceA));
		return new ArrayList<String>(result);
	}

	private List<String> getSharedFields(final List<DualCouplingInstance> primaryUseCases, final Service otherService) {
		// for all use cases of primaryService, collect the fields placed in
		// otherService and return it as StringList
		if (primaryUseCases == null || primaryUseCases.isEmpty()) {
			return Collections.emptyList();
		}
		return primaryUseCases.stream().flatMap(instance -> instance.getAllFields().stream().filter(field -> otherService.getDataFields().contains(field.getContextName())))
				.map(field -> field.getContextName()).collect(Collectors.toList());
	}

	private Map<String, List<String>> transformResponsibilityMap(final Map<Service, List<DualCouplingInstance>> useCaseResponsibilites) {
		Map<String, List<String>> result = new HashMap<>();
		for (Entry<Service, List<DualCouplingInstance>> responsibility : useCaseResponsibilites.entrySet()) {
			result.put(responsibility.getKey().getName(), responsibility.getValue().stream().map(instance -> instance.getName()).collect(Collectors.toList()));
		}
		return result;
	}

	private Map<Service, List<DualCouplingInstance>> getUseCaseResponsibilites(final Set<Service> set, final Model model) {
		Map<Service, List<DualCouplingInstance>> useCaseResponsibilites = new HashMap<>();
		CouplingCriterion criterion = couplingCriterionRepository.readByName(CouplingCriterion.SEMANTIC_PROXIMITY);
		CouplingCriteriaVariant useCaseVariant = variantRepository.readByNameAndCouplingCriterion(CouplingCriteriaVariant.SHARED_FIELD_ACCESS, criterion);
		for (MonoCouplingInstance instance : monoCouplingInstanceRepository.findByModelAndVariant(model, useCaseVariant)) {
			DualCouplingInstance dualInstance = (DualCouplingInstance) instance;
			Service responsibleService = getResponsibleService(set, dualInstance);
			if (useCaseResponsibilites.get(responsibleService) == null) {
				useCaseResponsibilites.put(responsibleService, new ArrayList<>());
			}
			useCaseResponsibilites.get(responsibleService).add(dualInstance);
		}
		return useCaseResponsibilites;
	}

	private Service getResponsibleService(final Set<Service> set, final DualCouplingInstance dualInstance) {
		final double SCORE_WRITE = 1d;
		final double SCORE_READ = 0.25d;
		Service responsibleService = null;
		Double highestScore = 0d;
		for (final Service service : set) {
			final long numberOfFieldsWritten = dualInstance.getSecondDataFields().stream().filter(field -> service.getDataFields().contains(field.getContextName())).count();
			final long numberOfFieldsRead = dualInstance.getDataFields().stream().filter(field -> service.getDataFields().contains(field.getContextName())).count();
			final double score = numberOfFieldsRead * SCORE_READ + numberOfFieldsWritten * SCORE_WRITE;
			if (score > highestScore) {
				highestScore = score;
				responsibleService = service;
			}
		}
		return responsibleService;
	}

	private Double getProximityScoreFor(final Service serviceA, final Service serviceB, final Map<FieldPair, Map<String, Score>> scores, final Model model) {
		Double score = 0d;
		for (String fieldA : serviceA.getDataFields()) {
			for (String fieldB : serviceB.getDataFields()) {
				FieldPair fieldTuple = new FieldPair(findDataField(fieldA, model), findDataField(fieldB, model));
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
