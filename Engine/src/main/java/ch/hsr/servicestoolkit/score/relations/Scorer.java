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
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.repository.NanoentityRepository;
import ch.hsr.servicestoolkit.model.repository.CouplingInstanceRepository;

@Component
public class Scorer {
	private final CouplingInstanceRepository couplingInstancesRepo;

	public static final double MAX_SCORE = 10d;
	public static final double MIN_SCORE = -10d;

	private Logger log = LoggerFactory.getLogger(Scorer.class);

	private NanoentityRepository nanoentityRepo;

	@Autowired
	public Scorer(final CouplingInstanceRepository repo, final NanoentityRepository nanoentityRepo) {
		this.couplingInstancesRepo = repo;
		this.nanoentityRepo = nanoentityRepo;
	}

	// TODO unused?
	public Map<EntityPair, Map<String, Score>> updateConfig(final Map<EntityPair, Map<String, Score>> scores, Function<String, Double> priorityProvider) {
		Map<EntityPair, Map<String, Score>> result = new HashMap<>();
		for (Entry<EntityPair, Map<String, Score>> scoresByFieldTuple : scores.entrySet()) {
			result.put(scoresByFieldTuple.getKey(), new HashMap<>());
			for (Entry<String, Score> scoreByCriterion : scoresByFieldTuple.getValue().entrySet()) {
				result.get(scoresByFieldTuple.getKey()).put(scoreByCriterion.getKey(),
						// scoreByCriterion.getValue().withPriority(config.getPriorityForCouplingCriterion(scoreByCriterion.getKey())));
						scoreByCriterion.getValue().withPriority(priorityProvider.apply(scoreByCriterion.getKey())));
			}
		}
		return result;
	}

	public Map<EntityPair, Map<String, Score>> getScores(final Model model, Function<String, Double> priorityProvider) {
		if (new HashSet<>(couplingInstancesRepo.findByModel(model)).isEmpty()) {
			throw new InvalidParameterException("model needs at least 1 coupling criterion in order for gephi clusterer to work");
		}
		Map<EntityPair, Map<String, Score>> result = new HashMap<>();

		addScoresForCharacteristicsCriteria(model, priorityProvider, result);
		addScoresForConstraintsCriteria(model, priorityProvider, result);
		addScoresForProximityCriteria(model, priorityProvider, result);
		return result;

	}

	private void addScoresForProximityCriteria(final Model model, Function<String, Double> priorityProvider, final Map<EntityPair, Map<String, Score>> result) {
		Map<EntityPair, Double> lifecycleScores = new CohesiveGroupCriteriaScorer().getScores(couplingInstancesRepo.findByModelAndCriterion(model, CouplingCriterion.IDENTITY_LIFECYCLE));
		addScoresByCriterionToResult(result, CouplingCriterion.IDENTITY_LIFECYCLE, lifecycleScores, priorityProvider.apply(CouplingCriterion.IDENTITY_LIFECYCLE));

		Map<EntityPair, Double> semanticProximityScores = new SemanticProximityCriterionScorer()
				.getScores(couplingInstancesRepo.findByModelAndCriterion(model, CouplingCriterion.SEMANTIC_PROXIMITY));
		addScoresByCriterionToResult(result, CouplingCriterion.SEMANTIC_PROXIMITY, semanticProximityScores, priorityProvider.apply(CouplingCriterion.SEMANTIC_PROXIMITY));

		Map<EntityPair, Double> responsibilityScores = new CohesiveGroupCriteriaScorer().getScores(couplingInstancesRepo.findByModelAndCriterion(model, CouplingCriterion.RESPONSIBILITY));
		addScoresByCriterionToResult(result, CouplingCriterion.RESPONSIBILITY, responsibilityScores, priorityProvider.apply(CouplingCriterion.RESPONSIBILITY));
	}

	private void addScoresForCharacteristicsCriteria(final Model model, Function<String, Double> priorityProvider, final Map<EntityPair, Map<String, Score>> result) {
		Map<String, Map<EntityPair, Double>> scoresByCriterion = new CharacteristicsCriteriaScorer()
				.getScores(couplingInstancesRepo.findByModelGroupedByCriterionFilteredByCriterionType(model, CouplingType.COMPATIBILITY));
		for (Entry<String, Map<EntityPair, Double>> distanceScores : scoresByCriterion.entrySet()) {
			addScoresByCriterionToResult(result, distanceScores.getKey(), distanceScores.getValue(), priorityProvider.apply(distanceScores.getKey()));
		}
	}

	private void addScoresForConstraintsCriteria(final Model model, Function<String, Double> priorityProvider, final Map<EntityPair, Map<String, Score>> result) {
		Map<EntityPair, Double> securityScores = new SeparationCriteriaScorer().getScores(couplingInstancesRepo.findByModelAndCriterion(model, CouplingCriterion.SECURITY_CONSTRAINT));
		addScoresByCriterionToResult(result, CouplingCriterion.SECURITY_CONSTRAINT, securityScores, priorityProvider.apply(CouplingCriterion.SECURITY_CONSTRAINT));

		Map<EntityPair, Double> predefinedServiceScores = new ExclusiveGroupCriteriaScorer(MIN_SCORE, MAX_SCORE, nanoentityRepo.findAll())
				.getScores(couplingInstancesRepo.findByModelAndCriterion(model, CouplingCriterion.PREDEFINED_SERVICE));
		addScoresByCriterionToResult(result, CouplingCriterion.PREDEFINED_SERVICE, predefinedServiceScores, priorityProvider.apply(CouplingCriterion.PREDEFINED_SERVICE));
	}

	private void addScoresByCriterionToResult(final Map<EntityPair, Map<String, Score>> result, final String couplingCriterionName, final Map<EntityPair, Double> scores, final Double priority) {
		for (Entry<EntityPair, Double> fieldScore : scores.entrySet()) {
			addScoresToResult(result, fieldScore.getKey(), couplingCriterionName, fieldScore.getValue(), priority);
		}
	}

	private void addScoresToResult(final Map<EntityPair, Map<String, Score>> result, final EntityPair fields, final String criterionName, final double score, final double priority) {
		if (fields.nanoentityA.getId().equals(fields.nanoentityB.getId())) {
			log.warn("score on same field ignored. Field: {}, Score: {}, Criterion: {}", fields.nanoentityA, score, criterionName);
			return;
		}

		if (result.get(fields) == null) {
			result.put(fields, new HashMap<>());
		}
		result.get(fields).put(criterionName, new Score(score, priority));
	}

}
