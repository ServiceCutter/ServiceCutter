package ch.hsr.servicestoolkit.score.relations;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.hsr.servicestoolkit.model.CouplingInstance;

public class CohesiveGroupCriteriaScorer implements CriterionScorer {

	@Override
	public Map<EntityPair, Double> getScores(final Set<CouplingInstance> instances) {
		Map<EntityPair, Double> result = new HashMap<>();
		for (CouplingInstance instance : instances) {
			for (int i = 0; i < instance.getNanoentities().size() - 1; i++) {
				for (int j = i + 1; j < instance.getNanoentities().size(); j++) {
					result.put(new EntityPair(instance.getNanoentities().get(i), instance.getNanoentities().get(j)), Scorer.MAX_SCORE);
				}
			}
		}
		return result;
	}

}
