package ch.hsr.servicestoolkit.score.relations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.DualCouplingInstance;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.score.cuts.CouplingCriterionScoring;

public class SemanticProximityCriterionScorer implements CriterionScorer {
	Map<FieldTuple, Double> result = new HashMap<>();
	// TODO: make configurable in UI
	private static final int SCORE_WRITE = 10;
	private static final int SCORE_READ = 3;
	// use the same score as for reads for two fields of which
	// one is read and the other written
	private static final int SCORE_MIXED = 3;
	private static final int SCORE_AGGREGATION = 1;

	@Override
	public Map<FieldTuple, Double> getScores(final Set<MonoCouplingInstance> instances) {
		for (MonoCouplingInstance fieldAccessInstance : instances) {
			DualCouplingInstance fieldAccessInstanceDual = (DualCouplingInstance) fieldAccessInstance;
			Double frequency = fieldAccessInstanceDual.getFrequency();
			if (frequency == null) {
				frequency = 1d;
			}
			List<DataField> fieldsWritten = fieldAccessInstanceDual.getSecondDataFields();
			List<DataField> fieldsRead = fieldAccessInstanceDual.getDataFields();
			addScoreForWriteAccess(fieldsWritten, frequency);
			addScoreForReadAccess(fieldsRead, frequency);
			addScoreForMixedAccess(fieldsWritten, fieldsRead, frequency);
		}

		List<MonoCouplingInstance> aggregationInstances = instances.stream().filter(instance -> instance.getVariant().getName().equals(CouplingCriteriaVariant.AGGREGATION))
				.collect(Collectors.toList());
		for (MonoCouplingInstance aggregationInstance : aggregationInstances) {
			DualCouplingInstance aggregationInstanceDual = (DualCouplingInstance) aggregationInstance;
			for (DataField fieldA : aggregationInstanceDual.getAllFields()) {
				for (DataField fieldB : aggregationInstanceDual.getSecondDataFields()) {
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

	void normalizeResult(final Map<FieldTuple, Double> result) {
		// scores in reversed order
		List<Double> scores = result.values().stream().sorted((d1, d2) -> Double.compare(d2, d1)).collect(Collectors.toList());
		if (scores.isEmpty()) {
			return;
		}
		int tenPercent = Math.max(1, (int) (scores.size() * 0.1d));
		double referenceValue = scores.get(tenPercent - 1);
		double divisor = referenceValue / CouplingCriterionScoring.MAX_SCORE;
		for (FieldTuple key : result.keySet()) {
			double newScore = Math.min(CouplingCriterionScoring.MAX_SCORE, result.get(key) / divisor);
			result.put(key, newScore);
		}
	}

	/**
	 * Fields read and written in same uc
	 * 
	 * @param frequency
	 */
	private void addScoreForMixedAccess(final List<DataField> fieldsWritten, final List<DataField> fieldsRead, final Double frequency) {
		for (DataField fieldWritten : fieldsWritten) {
			for (DataField fieldRead : fieldsRead) {
				addToResult(fieldRead, fieldWritten, SCORE_MIXED * frequency);
			}
		}
	}

	private void addScoreForReadAccess(final List<DataField> fieldsRead, final Double frequency) {
		for (int i = 0; i < fieldsRead.size() - 1; i++) {
			for (int j = i + 1; j < fieldsRead.size(); j++) {
				addToResult(fieldsRead.get(i), fieldsRead.get(j), SCORE_READ * frequency);
			}
		}
	}

	private void addScoreForWriteAccess(final List<DataField> fieldsWritten, final Double frequency) {
		for (int i = 0; i < fieldsWritten.size() - 1; i++) {
			for (int j = i + 1; j < fieldsWritten.size(); j++) {
				addToResult(fieldsWritten.get(i), fieldsWritten.get(j), SCORE_WRITE * frequency);
			}
		}
	}

	private void addToResult(final DataField fieldA, final DataField fieldB, final double score) {
		FieldTuple fieldTuple = new FieldTuple(fieldA, fieldB);
		if (result.get(fieldTuple) == null) {
			result.put(fieldTuple, score);
		} else {
			result.put(fieldTuple, score + result.get(fieldTuple));
		}
	}

}
