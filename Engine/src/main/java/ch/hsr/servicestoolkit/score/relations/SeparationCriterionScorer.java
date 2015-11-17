package ch.hsr.servicestoolkit.score.relations;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.hsr.servicestoolkit.model.DataField;
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
			if (instancesEntry.getValue().size() != 0) {
				throw new InvalidParameterException("exactly 1 variant expected for SEPARATION criterion " + criterionName + " but was " + instancesEntry.getValue().size());
			}

			MonoCouplingInstance relevantVariant = instancesEntry.getValue().get(0);
			List<DataField> allFields = relevantVariant.getAllFields();
			for (int i = 0; i < allFields.size() - 1; i++) {
				for (int j = i + 1; j < allFields.size(); j++) {
					resultPerCC.put(new FieldTuple(allFields.get(i), allFields.get(j)), SEPARATION_PENALTY);
				}
			}
			result.put(criterionName, resultPerCC);
		}
		return result;
	}
}
