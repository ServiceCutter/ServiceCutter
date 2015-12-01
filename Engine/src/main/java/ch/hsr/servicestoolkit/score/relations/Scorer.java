package ch.hsr.servicestoolkit.score.relations;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingType;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.repository.MonoCouplingInstanceRepository;
import ch.hsr.servicestoolkit.solver.SolverConfiguration;

public class Scorer {
	private final MonoCouplingInstanceRepository monoCouplingInstancesRepo;

	private Logger log = LoggerFactory.getLogger(Scorer.class);

	public Scorer(final MonoCouplingInstanceRepository repo) {
		this.monoCouplingInstancesRepo = repo;
	}

	public Map<FieldPair, Map<String, Score>> updateConfig(final Map<FieldPair, Map<String, Score>> scores, final SolverConfiguration config) {
		Map<FieldPair, Map<String, Score>> result = new HashMap<>();
		for (Entry<FieldPair, Map<String, Score>> scoresByFieldTuple : scores.entrySet()) {
			result.put(scoresByFieldTuple.getKey(), new HashMap<>());
			for (Entry<String, Score> scoreByCriterion : scoresByFieldTuple.getValue().entrySet()) {
				result.get(scoresByFieldTuple.getKey()).put(scoreByCriterion.getKey(),
						scoreByCriterion.getValue().withPriority(config.getPriorityForCouplingCriterion(scoreByCriterion.getKey())));
			}
		}
		return result;
	}

	public Map<FieldPair, Map<String, Score>> getScores(final Model model, final SolverConfiguration config) {
		if (new HashSet<>(monoCouplingInstancesRepo.findByModel(model)).isEmpty()) {
			throw new InvalidParameterException("model needs at least 1 coupling criterion in order for gephi clusterer to work");
		}
		Map<FieldPair, Map<String, Score>> result = new HashMap<>();

		addScoresForDistanceCriteria(model, config, result);
		addScoresForSeparationCriteria(model, config, result);
		addScoresForProximityCriteria(model, config, result);
		return result;

	}

	private void addScoresForProximityCriteria(final Model model, final SolverConfiguration config, final Map<FieldPair, Map<String, Score>> result) {
		Map<FieldPair, Double> lifecycleScores = new LifecycleCriterionScorer()
				.getScores(monoCouplingInstancesRepo.findByModelAndCriterion(model, CouplingCriterion.IDENTITY_LIFECYCLE));
		addScoresByCriterionToResult(result, CouplingCriterion.IDENTITY_LIFECYCLE, lifecycleScores, config.getPriorityForCouplingCriterion(CouplingCriterion.IDENTITY_LIFECYCLE));

		Map<FieldPair, Double> semanticProximityScores = new SemanticProximityCriterionScorer()
				.getScores(monoCouplingInstancesRepo.findByModelAndCriterion(model, CouplingCriterion.SEMANTIC_PROXIMITY));
		addScoresByCriterionToResult(result, CouplingCriterion.SEMANTIC_PROXIMITY, semanticProximityScores,
				config.getPriorityForCouplingCriterion(CouplingCriterion.SEMANTIC_PROXIMITY));
	}

	private void addScoresForDistanceCriteria(final Model model, final SolverConfiguration config, final Map<FieldPair, Map<String, Score>> result) {
		Map<String, Map<FieldPair, Double>> scoresByCriterion = new DistanceCriterionScorer()
				.getScores(monoCouplingInstancesRepo.findByModelGroupedByCriterionFilteredByCriterionType(model, CouplingType.DISTANCE));
		for (Entry<String, Map<FieldPair, Double>> distanceScores : scoresByCriterion.entrySet()) {
			addScoresByCriterionToResult(result, distanceScores.getKey(), distanceScores.getValue(), config.getPriorityForCouplingCriterion(distanceScores.getKey()));
		}
	}

	// TODO refactor: maybe introduce common interface for scorers
	private void addScoresForSeparationCriteria(final Model model, final SolverConfiguration config, final Map<FieldPair, Map<String, Score>> result) {
		Map<String, Map<FieldPair, Double>> scoresByCriterion = new SeparationCriterionScorer()
				.getScores(monoCouplingInstancesRepo.findByModelGroupedByCriterionFilteredByCriterionType(model, CouplingType.SEPARATION));
		for (Entry<String, Map<FieldPair, Double>> separationScores : scoresByCriterion.entrySet()) {
			addScoresByCriterionToResult(result, separationScores.getKey(), separationScores.getValue(), config.getPriorityForCouplingCriterion(separationScores.getKey()));
		}
	}

	private void addScoresByCriterionToResult(final Map<FieldPair, Map<String, Score>> result, final String couplingCriterionName, final Map<FieldPair, Double> scores,
			final Double priority) {
		for (Entry<FieldPair, Double> fieldScore : scores.entrySet()) {
			addScoresToResult(result, fieldScore.getKey(), couplingCriterionName, fieldScore.getValue(), priority);
		}
	}

	private void addScoresToResult(final Map<FieldPair, Map<String, Score>> result, final FieldPair fields, final String criterionName, final double score,
			final double priority) {
		if (fields.fieldA.getId().equals(fields.fieldB.getId())) {
			log.warn("score on same field ignored. Field: {}, Score: {}, Criterion: {}", fields.fieldA, score, criterionName);
			return;
		}

		if (result.get(fields) == null) {
			result.put(fields, new HashMap<>());
		}
		result.get(fields).put(criterionName, new Score(score, priority));
	}

}
