package ch.hsr.servicestoolkit.score.relations;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.DualCouplingInstance;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;

public class SeparationCriterionScorer {

	private static final double SEPARATION_PENALTY = -10d;

	public Map<String, Map<FieldTuple, Double>> getScores(final List<MonoCouplingInstance> separationCriteriaInstances) {
		Map<String, Map<FieldTuple, Double>> result = new HashMap<>();

		// get all instances group by separation CC
		for (Entry<String, List<MonoCouplingInstance>> instancesEntry : DistanceCriterionScorer.getInstancesByCriterion(separationCriteriaInstances).entrySet()) {
			Map<FieldTuple, Double> resultPerCC = new HashMap<>();
			String criterionName = instancesEntry.getKey();

			// TODO: we assume there is only one variant for separation
			// criteria, we should refactor the variants model to be used only
			// for distance criteria
			if (instancesEntry.getValue().size() != 1) {
				throw new InvalidParameterException("exactly 1 variant expected for SEPARATION criterion " + criterionName + " but was " + instancesEntry.getValue().size());
			}

			DualCouplingInstance relevantVariant = (DualCouplingInstance) instancesEntry.getValue().get(0);
			for (DataField fieldA : relevantVariant.getDataFields()) {
				for (DataField fieldB : relevantVariant.getSecondDataFields()) {
					resultPerCC.put(new FieldTuple(fieldA, fieldB), SEPARATION_PENALTY);
				}
			}
			result.put(criterionName, resultPerCC);
		}
		return result;
	}
}
