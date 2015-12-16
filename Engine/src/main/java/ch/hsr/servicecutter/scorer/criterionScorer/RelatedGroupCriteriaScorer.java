package ch.hsr.servicecutter.scorer.criterionScorer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.hsr.servicecutter.model.userdata.CouplingInstance;
import ch.hsr.servicecutter.model.userdata.Nanoentity;
import ch.hsr.servicecutter.scorer.EntityPair;

public class RelatedGroupCriteriaScorer implements CriterionScorer {

	private double penalty;
	private double premium;
	private Iterable<Nanoentity> allNanoentities;

	public RelatedGroupCriteriaScorer(final double penalty, final double premium, final Iterable<Nanoentity> iterable) {
		this.penalty = penalty;
		this.premium = premium;
		this.allNanoentities = iterable;
	}

	@Override
	public Map<EntityPair, Double> getScores(final Set<CouplingInstance> instances) {
		Map<EntityPair, Double> result = new HashMap<>();
		for (CouplingInstance instance : instances) {
			// add Premium to nanoentities in same group
			if (premium != 0d) {
				for (int i = 0; i < instance.getAllNanoentities().size() - 1; i++) {
					for (int j = i + 1; j < instance.getAllNanoentities().size(); j++) {
						result.put(new EntityPair(instance.getAllNanoentities().get(i), instance.getAllNanoentities().get(j)), premium);
					}
				}
			}
			// add penalty to from nanoentities in group to all other
			// TODO: this overwrites some relations with the same value,
			// optimize!
			if (penalty != 0) {
				for (Nanoentity groupEntity : instance.getAllNanoentities()) {
					for (Nanoentity otherEntity : allNanoentities) {
						if (!instance.getAllNanoentities().contains(otherEntity)) {
							result.put(new EntityPair(groupEntity, otherEntity), penalty);
						}
					}
				}
			}
		}
		return result;
	}

}
