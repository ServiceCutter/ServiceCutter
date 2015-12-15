package ch.hsr.servicestoolkit.score.relations;

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

import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingType;
import ch.hsr.servicestoolkit.model.InstanceType;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.repository.CouplingInstanceRepository;
import ch.hsr.servicestoolkit.model.repository.NanoentityRepository;

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

	// TODO unused?
	public Map<EntityPair, Map<String, Score>> updateConfig(final Map<EntityPair, Map<String, Score>> scores, final Function<String, Double> priorityProvider) {
		Map<EntityPair, Map<String, Score>> result = new HashMap<>();
		for (Entry<EntityPair, Map<String, Score>> scoresByNanoentityTuple : scores.entrySet()) {
			result.put(scoresByNanoentityTuple.getKey(), new HashMap<>());
			for (Entry<String, Score> scoreByCriterion : scoresByNanoentityTuple.getValue().entrySet()) {
				result.get(scoresByNanoentityTuple.getKey()).put(scoreByCriterion.getKey(),
						// scoreByCriterion.getValue().withPriority(config.getPriorityForCouplingCriterion(scoreByCriterion.getKey())));
						scoreByCriterion.getValue().withPriority(priorityProvider.apply(scoreByCriterion.getKey())));
			}
		}
		return result;
	}

	public Map<EntityPair, Map<String, Score>> getScores(final Model model, final Function<String, Double> priorityProvider) {
		if (new HashSet<>(couplingInstancesRepo.findByModel(model)).isEmpty()) {
			throw new InvalidParameterException("model needs at least 1 coupling criterion in order for gephi clusterer to work");
		}
		Map<EntityPair, Map<String, Score>> result = new HashMap<>();

		addScoresForCharacteristicsCriteria(model, priorityProvider, result);
		addScoresForConstraintsCriteria(model, priorityProvider, result);
		addScoresForProximityCriteria(model, priorityProvider, result);
		return result;

	}

	private void addScoresForProximityCriteria(final Model model, final Function<String, Double> priorityProvider, final Map<EntityPair, Map<String, Score>> result) {
		Map<EntityPair, Double> lifecycleScores = new CohesiveGroupCriterionScorer(nanoentityRepo.findByModel(model))
				.getScores(couplingInstancesRepo.findByModelAndCriterion(model, CouplingCriterion.IDENTITY_LIFECYCLE));
		addScoresByCriterionToResult(result, CouplingCriterion.IDENTITY_LIFECYCLE, lifecycleScores, priorityProvider.apply(CouplingCriterion.IDENTITY_LIFECYCLE));

		Map<EntityPair, Double> semanticProximityScores = new SemanticProximityCriterionScorer()
				.getScores(couplingInstancesRepo.findByModelAndCriterion(model, CouplingCriterion.SEMANTIC_PROXIMITY));
		addScoresByCriterionToResult(result, CouplingCriterion.SEMANTIC_PROXIMITY, semanticProximityScores, priorityProvider.apply(CouplingCriterion.SEMANTIC_PROXIMITY));

		Map<EntityPair, Double> responsibilityScores = new CohesiveGroupCriterionScorer(nanoentityRepo.findByModel(model))
				.getScores(couplingInstancesRepo.findByModelAndCriterion(model, CouplingCriterion.SHARED_OWNER));
		addScoresByCriterionToResult(result, CouplingCriterion.SHARED_OWNER, responsibilityScores, priorityProvider.apply(CouplingCriterion.SHARED_OWNER));

		// latency
		Map<EntityPair, Double> latencyScores = new CohesiveGroupCriterionScorer(nanoentityRepo.findByModel(model))
				.getScores(couplingInstancesRepo.findByModelAndInstanceType(model, InstanceType.USE_CASE));
		addScoresByCriterionToResult(result, CouplingCriterion.LATENCY, latencyScores, priorityProvider.apply(CouplingCriterion.LATENCY));

		// security contextuality
		Map<EntityPair, Double> securityContextualityScores = new CohesiveGroupCriterionScorer(nanoentityRepo.findByModel(model))
				.getScores(couplingInstancesRepo.findByModelAndCriterion(model, CouplingCriterion.SECURITY_CONTEXUALITY));
		addScoresByCriterionToResult(result, CouplingCriterion.SECURITY_CONTEXUALITY, securityContextualityScores, priorityProvider.apply(CouplingCriterion.SECURITY_CONTEXUALITY));

	}

	private void addScoresForCharacteristicsCriteria(final Model model, final Function<String, Double> priorityProvider, final Map<EntityPair, Map<String, Score>> result) {
		Map<String, Map<EntityPair, Double>> scoresByCriterion = new CharacteristicsCriteriaScorer()
				.getScores(couplingInstancesRepo.findByModelGroupedByCriterionFilteredByCriterionType(model, CouplingType.COMPATIBILITY));
		for (Entry<String, Map<EntityPair, Double>> distanceScores : scoresByCriterion.entrySet()) {
			addScoresByCriterionToResult(result, distanceScores.getKey(), distanceScores.getValue(), priorityProvider.apply(distanceScores.getKey()));
		}
	}

	private void addScoresForConstraintsCriteria(final Model model, final Function<String, Double> priorityProvider, final Map<EntityPair, Map<String, Score>> result) {
		Map<EntityPair, Double> securityScores = new SeparatedGroupCriterionScorer(nanoentityRepo.findByModel(model))
				.getScores(couplingInstancesRepo.findByModelAndCriterion(model, CouplingCriterion.SECURITY_CONSTRAINT));
		addScoresByCriterionToResult(result, CouplingCriterion.SECURITY_CONSTRAINT, securityScores, priorityProvider.apply(CouplingCriterion.SECURITY_CONSTRAINT));

		Map<EntityPair, Double> predefinedServiceScores = new ExclusiveGroupCriterionScorer(nanoentityRepo.findByModel(model))
				.getScores(couplingInstancesRepo.findByModelAndCriterion(model, CouplingCriterion.PREDEFINED_SERVICE));
		addScoresByCriterionToResult(result, CouplingCriterion.PREDEFINED_SERVICE, predefinedServiceScores, priorityProvider.apply(CouplingCriterion.PREDEFINED_SERVICE));

		Map<EntityPair, Double> consistencyConstraintScores = new CohesiveGroupCriterionScorer(nanoentityRepo.findByModel(model))
				.getScores(couplingInstancesRepo.findByModelAndCriterion(model, CouplingCriterion.CONSISTENCY_CONSTRAINT));
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
