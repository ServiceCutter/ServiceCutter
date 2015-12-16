package ch.hsr.servicecutter.scorer;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.hsr.servicecutter.model.criteria.CouplingCriterion;
import ch.hsr.servicecutter.model.criteria.CouplingType;
import ch.hsr.servicecutter.model.repository.CouplingInstanceRepository;
import ch.hsr.servicecutter.model.repository.NanoentityRepository;
import ch.hsr.servicecutter.model.solver.EntityPair;
import ch.hsr.servicecutter.model.usersystem.InstanceType;
import ch.hsr.servicecutter.model.usersystem.UserSystem;
import ch.hsr.servicecutter.scorer.criterionScorer.CharacteristicsCriteriaScorer;
import ch.hsr.servicecutter.scorer.criterionScorer.CohesiveGroupCriterionScorer;
import ch.hsr.servicecutter.scorer.criterionScorer.ExclusiveGroupCriterionScorer;
import ch.hsr.servicecutter.scorer.criterionScorer.SemanticProximityCriterionScorer;
import ch.hsr.servicecutter.scorer.criterionScorer.SeparatedGroupCriterionScorer;

@Component
public class Scorer {
	private final CouplingInstanceRepository couplingInstancesRepo;

	public static final double MAX_SCORE = 10d;
	public static final double MIN_SCORE = -10d;
	public static final double NO_SCORE = 0d;

	private Logger log = LoggerFactory.getLogger(Scorer.class);

	private NanoentityRepository nanoentityRepo;

	@Autowired
	public Scorer(final CouplingInstanceRepository repo, final NanoentityRepository nanoentityRepo) {
		this.couplingInstancesRepo = repo;
		this.nanoentityRepo = nanoentityRepo;
	}

	public Map<EntityPair, Map<String, Score>> getScores(final UserSystem userSystem, final Function<String, Double> priorityProvider) {
		if (new HashSet<>(couplingInstancesRepo.findByUserSystem(userSystem)).isEmpty()) {
			throw new InvalidParameterException("userSystem needs at least 1 coupling criterion in order for gephi clusterer to work");
		}
		Map<EntityPair, Map<String, Score>> result = new HashMap<>();

		addScoresForCharacteristicsCriteria(userSystem, priorityProvider, result);
		addScoresForConstraintsCriteria(userSystem, priorityProvider, result);
		addScoresForProximityCriteria(userSystem, priorityProvider, result);
		return result;

	}

	private void addScoresForProximityCriteria(final UserSystem userSystem, final Function<String, Double> priorityProvider, final Map<EntityPair, Map<String, Score>> result) {
		Map<EntityPair, Double> lifecycleScores = new CohesiveGroupCriterionScorer(nanoentityRepo.findByUserSystem(userSystem))
				.getScores(couplingInstancesRepo.findByUserSystemAndCriterion(userSystem, CouplingCriterion.IDENTITY_LIFECYCLE));
		addScoresByCriterionToResult(result, CouplingCriterion.IDENTITY_LIFECYCLE, lifecycleScores, priorityProvider.apply(CouplingCriterion.IDENTITY_LIFECYCLE));

		Map<EntityPair, Double> semanticProximityScores = new SemanticProximityCriterionScorer()
				.getScores(couplingInstancesRepo.findByUserSystemAndCriterion(userSystem, CouplingCriterion.SEMANTIC_PROXIMITY));
		addScoresByCriterionToResult(result, CouplingCriterion.SEMANTIC_PROXIMITY, semanticProximityScores, priorityProvider.apply(CouplingCriterion.SEMANTIC_PROXIMITY));

		Map<EntityPair, Double> responsibilityScores = new CohesiveGroupCriterionScorer(nanoentityRepo.findByUserSystem(userSystem))
				.getScores(couplingInstancesRepo.findByUserSystemAndCriterion(userSystem, CouplingCriterion.SHARED_OWNER));
		addScoresByCriterionToResult(result, CouplingCriterion.SHARED_OWNER, responsibilityScores, priorityProvider.apply(CouplingCriterion.SHARED_OWNER));

		// latency
		Map<EntityPair, Double> latencyScores = new CohesiveGroupCriterionScorer(nanoentityRepo.findByUserSystem(userSystem))
				.getScores(couplingInstancesRepo.findByUserSystemAndInstanceType(userSystem, InstanceType.USE_CASE));
		addScoresByCriterionToResult(result, CouplingCriterion.LATENCY, latencyScores, priorityProvider.apply(CouplingCriterion.LATENCY));

		// security contextuality
		Map<EntityPair, Double> securityContextualityScores = new CohesiveGroupCriterionScorer(nanoentityRepo.findByUserSystem(userSystem))
				.getScores(couplingInstancesRepo.findByUserSystemAndCriterion(userSystem, CouplingCriterion.SECURITY_CONTEXUALITY));
		addScoresByCriterionToResult(result, CouplingCriterion.SECURITY_CONTEXUALITY, securityContextualityScores, priorityProvider.apply(CouplingCriterion.SECURITY_CONTEXUALITY));

	}

	private void addScoresForCharacteristicsCriteria(final UserSystem userSystem, final Function<String, Double> priorityProvider, final Map<EntityPair, Map<String, Score>> result) {
		Map<String, Map<EntityPair, Double>> scoresByCriterion = new CharacteristicsCriteriaScorer()
				.getScores(couplingInstancesRepo.findByUserSystemGroupedByCriterionFilteredByCriterionType(userSystem, CouplingType.COMPATIBILITY));
		for (Entry<String, Map<EntityPair, Double>> distanceScores : scoresByCriterion.entrySet()) {
			addScoresByCriterionToResult(result, distanceScores.getKey(), distanceScores.getValue(), priorityProvider.apply(distanceScores.getKey()));
		}
	}

	private void addScoresForConstraintsCriteria(final UserSystem userSystem, final Function<String, Double> priorityProvider, final Map<EntityPair, Map<String, Score>> result) {
		Map<EntityPair, Double> securityScores = new SeparatedGroupCriterionScorer(nanoentityRepo.findByUserSystem(userSystem))
				.getScores(couplingInstancesRepo.findByUserSystemAndCriterion(userSystem, CouplingCriterion.SECURITY_CONSTRAINT));
		addScoresByCriterionToResult(result, CouplingCriterion.SECURITY_CONSTRAINT, securityScores, priorityProvider.apply(CouplingCriterion.SECURITY_CONSTRAINT));

		Map<EntityPair, Double> predefinedServiceScores = new ExclusiveGroupCriterionScorer(nanoentityRepo.findByUserSystem(userSystem))
				.getScores(couplingInstancesRepo.findByUserSystemAndCriterion(userSystem, CouplingCriterion.PREDEFINED_SERVICE));
		addScoresByCriterionToResult(result, CouplingCriterion.PREDEFINED_SERVICE, predefinedServiceScores, priorityProvider.apply(CouplingCriterion.PREDEFINED_SERVICE));

		Map<EntityPair, Double> consistencyConstraintScores = new CohesiveGroupCriterionScorer(nanoentityRepo.findByUserSystem(userSystem))
				.getScores(couplingInstancesRepo.findByUserSystemAndCriterion(userSystem, CouplingCriterion.CONSISTENCY_CONSTRAINT));
		addScoresByCriterionToResult(result, CouplingCriterion.CONSISTENCY_CONSTRAINT, consistencyConstraintScores,
				priorityProvider.apply(CouplingCriterion.CONSISTENCY_CONSTRAINT));
	}

	private void addScoresByCriterionToResult(final Map<EntityPair, Map<String, Score>> result, final String couplingCriterionName, final Map<EntityPair, Double> scores,
			final Double priority) {
		for (Entry<EntityPair, Double> nanoentityScore : scores.entrySet()) {
			addScoresToResult(result, nanoentityScore.getKey(), couplingCriterionName, nanoentityScore.getValue(), priority);
		}
	}

	private void addScoresToResult(final Map<EntityPair, Map<String, Score>> result, final EntityPair nanoentities, final String criterionName, final double score,
			final double priority) {
		if (nanoentities.nanoentityA.getId().equals(nanoentities.nanoentityB.getId())) {
			log.warn("score on same nanoentity ignored. Nanoentity: {}, Score: {}, Criterion: {}", nanoentities.nanoentityA, score, criterionName);
			return;
		}

		if (result.get(nanoentities) == null) {
			result.put(nanoentities, new HashMap<>());
		}
		result.get(nanoentities).put(criterionName, new Score(score, priority));
	}

}
