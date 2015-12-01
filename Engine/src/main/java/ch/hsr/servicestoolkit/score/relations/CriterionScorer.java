package ch.hsr.servicestoolkit.score.relations;

import java.util.Map;
import java.util.Set;

import ch.hsr.servicestoolkit.model.MonoCouplingInstance;

public interface CriterionScorer {

	public Map<FieldTuple, Double> getScores(final Set<MonoCouplingInstance> instances);
}
