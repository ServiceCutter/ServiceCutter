package ch.hsr.servicecutter.scorer.criterionScorer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.hsr.servicecutter.model.solver.EntityPair;
import ch.hsr.servicecutter.model.usersystem.CouplingInstance;
import ch.hsr.servicecutter.model.usersystem.Nanoentity;

public class RelatedGroupCriteriaScorer implements CriterionScorer {

	private double penalty;
	private double premium;
	private Iterable<Nanoentity> allNanoentities;
	private boolean penaltyToOtherGroups;
	private boolean penaltyToAll;

	public RelatedGroupCriteriaScorer(final double penalty, final double premium, final Iterable<Nanoentity> iterable, final boolean penaltyToOtherGroups,
			final boolean penaltyToAll) {
		this.penalty = penalty;
		this.premium = premium;
		this.allNanoentities = iterable;
		this.penaltyToOtherGroups = penaltyToOtherGroups;
		this.penaltyToAll = penaltyToAll;
	}

	// constructor to only set premium within scores
	public RelatedGroupCriteriaScorer(final double premium) {
		this(0d, premium, Collections.emptyList(), false, false);
	}

	// constructor to set penalty to other groups
	public RelatedGroupCriteriaScorer(final double penalty, final double premium) {
		this(penalty, premium, Collections.emptyList(), true, false);
	}

	// constructor to set penalty to all nanoentities not in group
	public RelatedGroupCriteriaScorer(final double penalty, final double premium, final Iterable<Nanoentity> allEntities) {
		this(penalty, premium, allEntities, false, true);
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
			if (penalty != 0 && penaltyToAll) {
				setPenaltyToOtherFields(result, instance, allNanoentities);
			}

			if (penalty != 0 && penaltyToOtherGroups) {
				List<Nanoentity> allNanoentitiesInInstances = instances.stream().flatMap(i -> i.getAllNanoentities().stream()).collect(Collectors.toList());
				setPenaltyToOtherFields(result, instance, allNanoentitiesInInstances);
			}
		}
		return result;
	}

	private void setPenaltyToOtherFields(final Map<EntityPair, Double> result, final CouplingInstance instance, final Iterable<Nanoentity> otherNanoentities) {
		for (Nanoentity groupEntity : instance.getAllNanoentities()) {
			for (Nanoentity otherEntity : otherNanoentities) {
				if (!instance.getAllNanoentities().contains(otherEntity)) {
					result.put(new EntityPair(groupEntity, otherEntity), penalty);
				}
			}
		}
	}

}
