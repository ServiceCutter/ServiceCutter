package ch.hsr.servicestoolkit.score.relations;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.hsr.servicestoolkit.model.NanoEntity;
import ch.hsr.servicestoolkit.model.DualCouplingInstance;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;

public class SeparationCriteriaScorer implements CriterionScorer {

	private static final double SEPARATION_PENALTY = -10d;

	public Map<String, Map<EntityPair, Double>> getScores(final Map<String, Set<MonoCouplingInstance>> instancesByCriterion) {
		Map<String, Map<EntityPair, Double>> result = new HashMap<>();

		// get all instances group by separation CC
		for (Entry<String, Set<MonoCouplingInstance>> instancesEntry : instancesByCriterion.entrySet()) {
			result.put(instancesEntry.getKey(), getScores(instancesEntry.getValue()));
		}
		return result;
	}

	@Override
	public Map<EntityPair, Double> getScores(final Set<MonoCouplingInstance> instances) {
		Map<EntityPair, Double> resultPerCC = new HashMap<>();

		// TODO: we assume there is only one variant for separation
		// criteria, we should refactor the variants model to be used only
		// for distance criteria
		if (instances.size() != 1) {
			throw new InvalidParameterException("exactly 1 variant expected for SEPARATION criterion but was " + instances.size());
		}

		DualCouplingInstance relevantVariant = (DualCouplingInstance) instances.iterator().next();
		for (NanoEntity fieldA : relevantVariant.getDataFields()) {
			for (NanoEntity fieldB : relevantVariant.getSecondDataFields()) {
				resultPerCC.put(new EntityPair(fieldA, fieldB), SEPARATION_PENALTY);
			}
		}
		return resultPerCC;

	}
}
