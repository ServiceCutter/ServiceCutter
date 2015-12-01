package ch.hsr.servicestoolkit.score.relations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.hsr.servicestoolkit.model.NanoEntity;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import jersey.repackaged.com.google.common.collect.Lists;

public class CharacteristicsCriteriaScorer implements CriterionScorer {

	public Map<String, Map<EntityPair, Double>> getScores(final Map<String, Set<MonoCouplingInstance>> instancesByCriterion) {
		Map<String, Map<EntityPair, Double>> result = new HashMap<>();

		// get all instances group by distance CC
		for (Entry<String, Set<MonoCouplingInstance>> instancesEntry : instancesByCriterion.entrySet()) {
			result.put(instancesEntry.getKey(), getScores(instancesEntry.getValue()));
		}
		return result;
	}

	@Override
	public Map<EntityPair, Double> getScores(final Set<MonoCouplingInstance> instances) {
		Map<EntityPair, Double> resultPerCC = new HashMap<>();
		// compare all variants with each other
		List<MonoCouplingInstance> variants = Lists.newArrayList(instances);

		for (int i = 0; i < variants.size() - 1; i++) {
			for (int j = i + 1; j < variants.size(); j++) {
				// for all fields in two different variants, calculate the
				// distance
				MonoCouplingInstance variantI = variants.get(i);
				MonoCouplingInstance variantJ = variants.get(j);
				for (NanoEntity fieldFromI : variantI.getAllFields()) {
					for (NanoEntity fieldFromJ : variantJ.getAllFields()) {
						int distance = Math.abs(variantI.getVariant().getWeight() - variantJ.getVariant().getWeight());
						if (distance != 0) {
							resultPerCC.put(new EntityPair(fieldFromI, fieldFromJ), distance * -1d);
						}

					}
				}
			}
		}
		return resultPerCC;
	}

}
