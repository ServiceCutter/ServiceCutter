package ch.hsr.servicestoolkit.scorer.criterionScorer;

import java.util.Map;
import java.util.Set;

import ch.hsr.servicestoolkit.model.systemdata.CouplingInstance;
import ch.hsr.servicestoolkit.scorer.EntityPair;

public interface CriterionScorer {

	public Map<EntityPair, Double> getScores(final Set<CouplingInstance> instances);
}
