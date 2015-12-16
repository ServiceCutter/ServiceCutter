package ch.hsr.servicecutter.scorer.criterionScorer;

import java.util.Map;
import java.util.Set;

import ch.hsr.servicecutter.model.usersystem.CouplingInstance;
import ch.hsr.servicecutter.scorer.EntityPair;

public interface CriterionScorer {

	public Map<EntityPair, Double> getScores(final Set<CouplingInstance> instances);
}
