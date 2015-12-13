package ch.hsr.servicestoolkit.score.relations;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.hsr.servicestoolkit.model.CouplingInstance;
import ch.hsr.servicestoolkit.model.Nanoentity;

public class SeparationCriteriaScorer implements CriterionScorer {

	private static final double SEPARATION_PENALTY = -10d;

	@Override
	public Map<EntityPair, Double> getScores(final Set<CouplingInstance> instances) {
		Map<EntityPair, Double> resultPerCC = new HashMap<>();

		// TODO: we assume there is only one characteristic for separation
		// criteria, we should refactor the characteristics model to be used
		// only for distance criteria
		if (instances.size() > 1) {
			throw new InvalidParameterException("0 or 1 characteristic expected for seperation criterion but was " + instances.size());
		} else if (instances.isEmpty()) {
			return resultPerCC;
		}

		// TODO: be able to handle more than 2 groups
		CouplingInstance relevantCharacteristic = instances.iterator().next();
		for (Nanoentity nanoentityA : relevantCharacteristic.getNanoentities()) {
			for (Nanoentity nanoentityB : relevantCharacteristic.getSecondNanoentities()) {
				resultPerCC.put(new EntityPair(nanoentityA, nanoentityB), SEPARATION_PENALTY);
			}
		}
		return resultPerCC;

	}
}
