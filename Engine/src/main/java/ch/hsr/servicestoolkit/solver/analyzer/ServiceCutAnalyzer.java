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
import ch.hsr.servicestoolkit.model.CouplingInstance;
import ch.hsr.servicestoolkit.model.InstanceType;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.Nanoentity;
import ch.hsr.servicestoolkit.model.repository.NanoentityRepository;
import ch.hsr.servicestoolkit.score.relations.EntityPair;
import ch.hsr.servicestoolkit.score.relations.Score;
import ch.hsr.servicestoolkit.solver.Service;
import ch.hsr.servicestoolkit.solver.SolverResult;

@Component
public class ServiceCutAnalyzer {

	private NanoentityRepository nanoentityRepository;

	private Logger log = LoggerFactory.getLogger(ServiceCutAnalyzer.class);

	@Autowired
	public ServiceCutAnalyzer(final NanoentityRepository nanoentityRepository) {
		this.nanoentityRepository = nanoentityRepository;
	}

	private Nanoentity findNanoentity(final String nanoentityName, final Model model) {
		Nanoentity nanoentity;
		if (nanoentityName.contains(".")) {
			String[] splittedName = nanoentityName.split("\\.");
			nanoentity = nanoentityRepository.findByContextAndNameAndModel(splittedName[0], splittedName[1], model);
		} else {
			nanoentity = nanoentityRepository.findByNameAndModel(nanoentityName, model);
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

		// relation scores and shared Nanoentities
		List<Service> serviceList = new ArrayList<>(solverResult.getServices());
		List<ServiceRelation> relations = new ArrayList<>();

		for (int a = 0; a < serviceList.size() - 1; a++) {
			for (int b = a + 1; b < serviceList.size(); b++) {
				ServiceTuple serviceTuple = new ServiceTuple(serviceList.get(a).getName(), serviceList.get(b).getName());
				Double score = getProximityScoreFor(serviceList.get(a), serviceList.get(b), scores, model);
				final List<String> sharedFiels = getSharedNanoentities(serviceList.get(a), serviceList.get(b), useCaseResponsibilites);
				if (score > 0) {
					log.info("create service relation for services {} and {} with score {} and nanoentities {}", serviceList.get(a).getName(), serviceList.get(b).getName(), score,
							sharedFiels.toString());
					relations.add(new ServiceRelation(sharedFiels, score, serviceTuple.getServiceA(), serviceTuple.getServiceB()));
				}
			}
		}
		solverResult.setRelations(relations);

	}

	private List<String> getSharedNanoentities(final Service serviceA, final Service serviceB, final Map<Service, List<CouplingInstance>> useCaseResponsibilites) {
		// use set to avoid duplicates
		Set<String> result = new HashSet<String>();
		result.addAll(getSharedNanoentities(useCaseResponsibilites.get(serviceA), serviceB));
		result.addAll(getSharedNanoentities(useCaseResponsibilites.get(serviceB), serviceA));
		return new ArrayList<String>(result);
	}

	private List<String> getSharedNanoentities(final List<CouplingInstance> primaryUseCases, final Service otherService) {
		// for all use cases of primaryService, collect the nanoentities placed
		// in
		// otherService and return it as StringList
		if (primaryUseCases == null || primaryUseCases.isEmpty()) {
			return Collections.emptyList();
		}
		return primaryUseCases.stream().flatMap(instance -> instance.getAllNanoentities().stream().filter(nanoentity -> otherService.getNanoentities().contains(nanoentity.getContextName())))
				.map(nanoentity -> nanoentity.getContextName()).collect(Collectors.toList());
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
		model.getCouplingInstances().stream().filter((instance) -> {
			return InstanceType.USE_CASE.equals(instance.getType());
		}).forEach((instance) -> {
			Service responsibleService = getResponsibleService(set, instance);
			Assert.notNull(responsibleService, "Responsible service for " + instance.getName() + " not found!");
			if (useCaseResponsibilites.get(responsibleService) == null) {
				useCaseResponsibilites.put(responsibleService, new ArrayList<>());
			}
			useCaseResponsibilites.get(responsibleService).add(instance);
		});
		return useCaseResponsibilites;
	}

	private Service getResponsibleService(final Set<Service> set, final CouplingInstance dualInstance) {
		final double SCORE_WRITE = 1d;
		final double SCORE_READ = 0.25d;
		Service responsibleService = null;
		Double highestScore = 0d;
		for (final Service service : set) {
			final long numberOfNanoentitiesWritten = dualInstance.getSecondNanoentities().stream().filter(nanoentity -> service.getNanoentities().contains(nanoentity.getContextName())).count();
			final long numberOfNanoentitiesRead = dualInstance.getNanoentities().stream().filter(nanoentity -> service.getNanoentities().contains(nanoentity.getContextName())).count();
			final double score = numberOfNanoentitiesRead * SCORE_READ + numberOfNanoentitiesWritten * SCORE_WRITE;
			if (score > highestScore) {
				highestScore = score;
				responsibleService = service;
			}
		}
		return responsibleService;
	}

	private Double getProximityScoreFor(final Service serviceA, final Service serviceB, final Map<EntityPair, Map<String, Score>> scores, final Model model) {
		Double score = 0d;
		for (String nanoentityA : serviceA.getNanoentities()) {
			for (String nanoentityB : serviceB.getNanoentities()) {
				EntityPair nanoentityTuple = new EntityPair(findNanoentity(nanoentityA, model), findNanoentity(nanoentityB, model));
				final Map<String, Score> scoresByTuple = scores.get(nanoentityTuple);
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
