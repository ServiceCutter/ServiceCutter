package ch.hsr.servicecutter.scorer.criterionScorer;

import java.util.Map;
import java.util.Set;

import ch.hsr.servicecutter.model.solver.EntityPair;
import ch.hsr.servicecutter.model.usersystem.CouplingInstance;

public interface CriterionScorer {

	public Map<EntityPair, Double> getScores(final Set<CouplingInstance> instances);
}
