package ch.hsr.servicestoolkit.score.relations;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.hsr.servicestoolkit.model.DualCouplingInstance;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.model.NanoEntity;

public class SeparationCriteriaScorer implements CriterionScorer {

	private static final double SEPARATION_PENALTY = -10d;

	@Override
	public Map<EntityPair, Double> getScores(final Set<MonoCouplingInstance> instances) {
		Map<EntityPair, Double> resultPerCC = new HashMap<>();

		// TODO: we assume there is only one variant for separation
		// criteria, we should refactor the variants model to be used only
		// for distance criteria
		if (instances.size() > 1) {
			throw new InvalidParameterException("0 or 1 variant expected for seperation criterion but was " + instances.size());
		} else if (instances.isEmpty()) {
			return resultPerCC;
		}

		// TODO: be able to handle more than 2 groups
		DualCouplingInstance relevantVariant = (DualCouplingInstance) instances.iterator().next();
		for (NanoEntity fieldA : relevantVariant.getDataFields()) {
			for (NanoEntity fieldB : relevantVariant.getSecondDataFields()) {
				resultPerCC.put(new EntityPair(fieldA, fieldB), SEPARATION_PENALTY);
			}
		}
		return resultPerCC;

	}
}
