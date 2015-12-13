package ch.hsr.servicestoolkit.score.relations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic;
import ch.hsr.servicestoolkit.model.CouplingInstance;
import ch.hsr.servicestoolkit.model.Nanoentity;

public class SemanticProximityCriterionScorer implements CriterionScorer {
	Map<EntityPair, Double> result = new HashMap<>();
	// TODO: make configurable in UI
	private static final int SCORE_WRITE = 10;
	private static final int SCORE_READ = 3;
	// use the same score as for reads for two fields of which
	// one is read and the other written
	private static final int SCORE_MIXED = 3;
	private static final int SCORE_AGGREGATION = 1;

	@Override
	public Map<EntityPair, Double> getScores(final Set<CouplingInstance> instances) {
		for (CouplingInstance instance : instances) {
			List<Nanoentity> fieldsWritten = instance.getSecondNanoentities();
			List<Nanoentity> fieldsRead = instance.getNanoentities();
			addScoreForWriteAccess(fieldsWritten);
			addScoreForReadAccess(fieldsRead);
			addScoreForMixedAccess(fieldsWritten, fieldsRead);
		}

		List<CouplingInstance> aggregationInstances = instances.stream().filter(instance -> instance.isCharacteristic(CouplingCriterionCharacteristic.AGGREGATION)).collect(Collectors.toList());
		for (CouplingInstance aggregationInstance : aggregationInstances) {
			for (Nanoentity fieldA : aggregationInstance.getAllNanoentities()) {
				for (Nanoentity fieldB : aggregationInstance.getSecondNanoentities()) {
					addToResult(fieldA, fieldB, SCORE_AGGREGATION);
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
	 * Fields read and written in same uc
	 * 
	 * @param frequency
	 */
	private void addScoreForMixedAccess(final List<Nanoentity> fieldsWritten, final List<Nanoentity> fieldsRead) {
		for (Nanoentity fieldWritten : fieldsWritten) {
			for (Nanoentity fieldRead : fieldsRead) {
				addToResult(fieldRead, fieldWritten, SCORE_MIXED);
			}
		}
	}

	private void addScoreForReadAccess(final List<Nanoentity> fieldsRead) {
		for (int i = 0; i < fieldsRead.size() - 1; i++) {
			for (int j = i + 1; j < fieldsRead.size(); j++) {
				addToResult(fieldsRead.get(i), fieldsRead.get(j), SCORE_READ);
			}
		}
	}

	private void addScoreForWriteAccess(final List<Nanoentity> fieldsWritten) {
		for (int i = 0; i < fieldsWritten.size() - 1; i++) {
			for (int j = i + 1; j < fieldsWritten.size(); j++) {
				addToResult(fieldsWritten.get(i), fieldsWritten.get(j), SCORE_WRITE);
			}
		}
	}

	private void addToResult(final Nanoentity fieldA, final Nanoentity fieldB, final double score) {
		EntityPair fieldTuple = new EntityPair(fieldA, fieldB);
		if (result.get(fieldTuple) == null) {
			result.put(fieldTuple, score);
		} else {
			result.put(fieldTuple, score + result.get(fieldTuple));
		}
	}

}
