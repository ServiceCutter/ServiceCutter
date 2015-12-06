package ch.hsr.servicestoolkit.score.relations;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.model.NanoEntity;

public class ExclusiveGroupCriteriaScorer implements CriterionScorer {

	private double penalty;
	private double premium;
	private Iterable<NanoEntity> allNanoentities;

	public ExclusiveGroupCriteriaScorer(final double penalty, final double premium, final Iterable<NanoEntity> iterable) {
		this.penalty = penalty;
		this.premium = premium;
		this.allNanoentities = iterable;
	}

	@Override
	public Map<EntityPair, Double> getScores(final Set<MonoCouplingInstance> instances) {
		Map<EntityPair, Double> result = new HashMap<>();
		for (MonoCouplingInstance instance : instances) {
			// add Premium to nanoentities in same group
			for (int i = 0; i < instance.getDataFields().size() - 1; i++) {
				for (int j = i + 1; j < instance.getDataFields().size(); j++) {
					result.put(new EntityPair(instance.getDataFields().get(i), instance.getDataFields().get(j)), premium);
				}
			}
			// add penalty to from nanoentities in group to all other
			// TODO: this overwrites some relations with the same value,
			// optimize!
			for (NanoEntity groupEntity : instance.getDataFields()) {
				for (NanoEntity otherEntity : allNanoentities) {
					if (!instance.getAllFields().contains(otherEntity)) {
						result.put(new EntityPair(groupEntity, otherEntity), penalty);
					}
				}
			}
		}
		return result;
	}

}
