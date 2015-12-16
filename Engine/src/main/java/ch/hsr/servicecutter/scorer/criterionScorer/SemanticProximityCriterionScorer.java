package ch.hsr.servicecutter.scorer.criterionScorer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.hsr.servicecutter.model.solver.EntityPair;
import ch.hsr.servicecutter.model.usersystem.CouplingInstance;
import ch.hsr.servicecutter.model.usersystem.InstanceType;
import ch.hsr.servicecutter.model.usersystem.Nanoentity;
import ch.hsr.servicecutter.scorer.Scorer;

public class SemanticProximityCriterionScorer implements CriterionScorer {
	Map<EntityPair, Double> result = new HashMap<>();
	private static final int SCORE_WRITE = 10;
	private static final int SCORE_READ = 3;
	// MIXED=reads for two nanoentities of which one is read and the other
	// written
	private static final int SCORE_MIXED = 3;
	private static final int SCORE_AGGREGATION = 1;

	@Override
	public Map<EntityPair, Double> getScores(final Set<CouplingInstance> instances) {
		Set<CouplingInstance> useCaseInstances = instances;
		useCaseInstances = instances.stream().filter(instance -> instance.getType().equals(InstanceType.USE_CASE)).collect(Collectors.toSet());
		for (CouplingInstance instance : useCaseInstances) {
			List<Nanoentity> nanoentitiesWritten = instance.getSecondNanoentities();
			List<Nanoentity> nanoentitiesRead = instance.getNanoentities();
			addScoreForWriteAccess(nanoentitiesWritten);
			addScoreForReadAccess(nanoentitiesRead);
			addScoreForMixedAccess(nanoentitiesWritten, nanoentitiesRead);
		}

		List<CouplingInstance> aggregationInstances = instances.stream().filter(instance -> instance.getType().equals(InstanceType.AGGREGATION)).collect(Collectors.toList());
		for (CouplingInstance aggregationInstance : aggregationInstances) {
			for (Nanoentity nanoentityA : aggregationInstance.getAllNanoentities()) {
				for (Nanoentity nanoentityB : aggregationInstance.getAllNanoentities()) {
					addToResult(nanoentityA, nanoentityB, SCORE_AGGREGATION);
				}
			}
		}
		normalizeResult(result);
		return result;
	}

	/*
	 * normalizes the scores to values between 0 and 10. the best 10% of the
	 * scores get the score 10 the worst of these 10% becomes the
	 * referenceValue, so that: referenceValue / x = 10 => x =
	 * referenceValue/10. The other 90% are then divided by x to reach a number
	 * between 0 and 10
	 */

	void normalizeResult(final Map<EntityPair, Double> result) {
		// scores in reversed order
		List<Double> scores = result.values().stream().sorted((d1, d2) -> Double.compare(d2, d1)).collect(Collectors.toList());
		if (scores.isEmpty()) {
			return;
		}
		int tenPercent = Math.max(1, (int) (scores.size() * 0.1d));
		double referenceValue = scores.get(tenPercent - 1);
		double divisor = referenceValue / Scorer.MAX_SCORE;
		for (EntityPair key : result.keySet()) {
			double newScore = Math.min(Scorer.MAX_SCORE, result.get(key) / divisor);
			result.put(key, newScore);
		}
	}

	/**
	 * Nanoentities read and written in same Use Case
	 * 
	 * @param frequency
	 */
	private void addScoreForMixedAccess(final List<Nanoentity> nanoentitiesWritten, final List<Nanoentity> nanoentitiesRead) {
		for (Nanoentity nanoentityWritten : nanoentitiesWritten) {
			for (Nanoentity nanoentityRead : nanoentitiesRead) {
				addToResult(nanoentityRead, nanoentityWritten, SCORE_MIXED);
			}
		}
	}

	private void addScoreForReadAccess(final List<Nanoentity> nanoentitiesRead) {
		for (int i = 0; i < nanoentitiesRead.size() - 1; i++) {
			for (int j = i + 1; j < nanoentitiesRead.size(); j++) {
				addToResult(nanoentitiesRead.get(i), nanoentitiesRead.get(j), SCORE_READ);
			}
		}
	}

	private void addScoreForWriteAccess(final List<Nanoentity> nanoentitiesWritten) {
		for (int i = 0; i < nanoentitiesWritten.size() - 1; i++) {
			for (int j = i + 1; j < nanoentitiesWritten.size(); j++) {
				addToResult(nanoentitiesWritten.get(i), nanoentitiesWritten.get(j), SCORE_WRITE);
			}
		}
	}

	private void addToResult(final Nanoentity nanoentityA, final Nanoentity nanoentityB, final double score) {
		EntityPair fieldTuple = new EntityPair(nanoentityA, nanoentityB);
		if (result.get(fieldTuple) == null) {
			result.put(fieldTuple, score);
		} else {
			result.put(fieldTuple, score + result.get(fieldTuple));
		}
	}

}
