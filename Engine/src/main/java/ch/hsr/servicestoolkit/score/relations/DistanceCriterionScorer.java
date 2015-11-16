package ch.hsr.servicestoolkit.score.relations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;

public class DistanceCriterionScorer {

	public Map<String, Map<FieldTuple, Double>> getScores(final List<MonoCouplingInstance> distanceCriteriaInstances) {
		Map<String, Map<FieldTuple, Double>> result = new HashMap<>();

		// get all instances group by distance CC
		for (Entry<String, List<MonoCouplingInstance>> instancesEntry : getInstancesByCriterion(distanceCriteriaInstances).entrySet()) {
			Map<FieldTuple, Double> resultPerCC = new HashMap<>();
			// compare all variants with each other
			List<MonoCouplingInstance> variants = instancesEntry.getValue();
			String criterionName = variants.get(0).getVariant().getCouplingCriterion().getName();
			for (int i = 0; i < variants.size() - 1; i++) {
				for (int j = i + 1; j < variants.size(); j++) {
					// for all fields in two different variants, calculate the
					// distance
					MonoCouplingInstance variantI = variants.get(i);
					MonoCouplingInstance variantJ = variants.get(j);
					for (DataField fieldFromI : variantI.getAllFields()) {
						for (DataField fieldFromJ : variantJ.getAllFields()) {
							int distance = Math.abs(variantI.getVariant().getWeight() - variantJ.getVariant().getWeight());
							if (distance != 0) {
								resultPerCC.put(new FieldTuple(fieldFromI, fieldFromJ), distance * -1d);
							}

						}
					}
				}
			}
			result.put(criterionName, resultPerCC);
		}
		return result;
	}

	private Map<String, List<MonoCouplingInstance>> getInstancesByCriterion(final List<MonoCouplingInstance> instances) {
		Map<String, List<MonoCouplingInstance>> instancesByCriterion = new HashMap<>();
		for (MonoCouplingInstance instance : instances) {
			String ccName = instance.getVariant().getCouplingCriterion().getName();
			if (instancesByCriterion.get(ccName) == null) {
				instancesByCriterion.put(ccName, new ArrayList<MonoCouplingInstance>());
			}
			instancesByCriterion.get(ccName).add(instance);
		}
		return instancesByCriterion;
	}

}
