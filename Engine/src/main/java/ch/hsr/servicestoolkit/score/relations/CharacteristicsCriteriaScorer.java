package ch.hsr.servicestoolkit.score.relations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.hsr.servicestoolkit.model.CouplingInstance;
import ch.hsr.servicestoolkit.model.Nanoentity;
import jersey.repackaged.com.google.common.collect.Lists;

public class CharacteristicsCriteriaScorer implements CriterionScorer {

	public Map<String, Map<EntityPair, Double>> getScores(final Map<String, Set<CouplingInstance>> instancesByCriterion) {
		Map<String, Map<EntityPair, Double>> result = new HashMap<>();

		// get all instances group by distance CC
		for (Entry<String, Set<CouplingInstance>> instancesEntry : instancesByCriterion.entrySet()) {
			result.put(instancesEntry.getKey(), getScores(instancesEntry.getValue()));
		}
		return result;
	}

	@Override
	public Map<EntityPair, Double> getScores(final Set<CouplingInstance> instances) {
		Map<EntityPair, Double> resultPerCC = new HashMap<>();
		// compare all characteristics with each other
		List<CouplingInstance> characteristics = Lists.newArrayList(instances);

		for (int i = 0; i < characteristics.size() - 1; i++) {
			for (int j = i + 1; j < characteristics.size(); j++) {
				// for all nanoentities in two different characteristics,
				// calculate the distance
				CouplingInstance characteristicI = characteristics.get(i);
				CouplingInstance characteristicJ = characteristics.get(j);
				for (Nanoentity nanoentityFromI : characteristicI.getAllNanoentities()) {
					for (Nanoentity nanoentityFromJ : characteristicJ.getAllNanoentities()) {
						int distance = Math.abs(characteristicI.getCharacteristic().getWeight() - characteristicJ.getCharacteristic().getWeight());
						if (distance != 0) {
							resultPerCC.put(new EntityPair(nanoentityFromI, nanoentityFromJ), distance * -1d);
						}

					}
				}
			}
		}
		return resultPerCC;
	}

}
