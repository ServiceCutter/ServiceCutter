package ch.hsr.servicestoolkit.score.relations;

import java.util.Map;
import java.util.Set;

import ch.hsr.servicestoolkit.model.CouplingInstance;

public interface CriterionScorer {

	public Map<EntityPair, Double> getScores(final Set<CouplingInstance> instances);
}
