package ch.hsr.servicestoolkit.score.relations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.DualCouplingInstance;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.score.cuts.CouplingCriterionScoring;

public class SemanticProximityCriterionScorer {
	Map<FieldTuple, Double> result = new HashMap<>();
	// TODO: make configurable in UI
	private static final int SCORE_WRITE = 10;
	private static final int SCORE_READ = 3;
	// use the same score as for reads for two fields of which
	// one is read and the other written
	private static final int SCORE_MIXED = 3;
	private static final int SCORE_AGGREGATION = 1;

	public Map<FieldTuple, Double> getScores(final List<MonoCouplingInstance> proximityInstances) {
		for (MonoCouplingInstance fieldAccessInstance : proximityInstances) {
			DualCouplingInstance fieldAccessInstanceDual = (DualCouplingInstance) fieldAccessInstance;
			// TODO: Frequency
			List<DataField> fieldsWritten = fieldAccessInstanceDual.getSecondDataFields();
			List<DataField> fieldsRead = fieldAccessInstanceDual.getDataFields();
			addScoreForWriteAccess(fieldsWritten);
			addScoreForReadAccess(fieldsRead);
			addScoreForMixedAccess(fieldsWritten, fieldsRead);
		}

		List<MonoCouplingInstance> aggregationInstances = proximityInstances.stream()
				.filter(instance -> instance.getVariant().getName().equals(CouplingCriteriaVariant.AGGREGATION)).collect(Collectors.toList());
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
	 */
	private void addScoreForMixedAccess(final List<DataField> fieldsWritten, final List<DataField> fieldsRead) {
		for (int i = 0; i < fieldsRead.size(); i++) {
			for (int j = 0; j < fieldsWritten.size(); j++) {
				addToResult(fieldsRead.get(i), fieldsRead.get(j), SCORE_MIXED);
			}
		}
	}

	private void addScoreForReadAccess(final List<DataField> fieldsRead) {
		for (int i = 0; i < fieldsRead.size() - 1; i++) {
			for (int j = i + 1; j < fieldsRead.size(); j++) {
				addToResult(fieldsRead.get(i), fieldsRead.get(j), SCORE_READ);
			}
		}
	}

	private void addScoreForWriteAccess(final List<DataField> fieldsWritten) {
		for (int i = 0; i < fieldsWritten.size() - 1; i++) {
			for (int j = i + 1; j < fieldsWritten.size(); j++) {
				addToResult(fieldsWritten.get(i), fieldsWritten.get(j), SCORE_WRITE);
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
