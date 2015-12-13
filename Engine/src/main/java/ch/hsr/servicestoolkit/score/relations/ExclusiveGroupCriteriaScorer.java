package ch.hsr.servicestoolkit.score.relations;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.hsr.servicestoolkit.model.CouplingInstance;
import ch.hsr.servicestoolkit.model.Nanoentity;

public class ExclusiveGroupCriteriaScorer implements CriterionScorer {

	private double penalty;
	private double premium;
	private Iterable<Nanoentity> allNanoentities;

	public ExclusiveGroupCriteriaScorer(final double penalty, final double premium, final Iterable<Nanoentity> iterable) {
		this.penalty = penalty;
		this.premium = premium;
		this.allNanoentities = iterable;
	}

	@Override
	public Map<EntityPair, Double> getScores(final Set<CouplingInstance> instances) {
		Map<EntityPair, Double> result = new HashMap<>();
		for (CouplingInstance instance : instances) {
			// add Premium to nanoentities in same group
			for (int i = 0; i < instance.getNanoentities().size() - 1; i++) {
				for (int j = i + 1; j < instance.getNanoentities().size(); j++) {
					result.put(new EntityPair(instance.getNanoentities().get(i), instance.getNanoentities().get(j)), premium);
				}
			}
			// add penalty to from nanoentities in group to all other
			// TODO: this overwrites some relations with the same value,
			// optimize!
			for (Nanoentity groupEntity : instance.getNanoentities()) {
				for (Nanoentity otherEntity : allNanoentities) {
					if (!instance.getAllNanoentities().contains(otherEntity)) {
						result.put(new EntityPair(groupEntity, otherEntity), penalty);
					}
				}
			}
		}
		return result;
	}

}
