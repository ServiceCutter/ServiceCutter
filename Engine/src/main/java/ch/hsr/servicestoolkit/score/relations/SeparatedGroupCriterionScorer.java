package ch.hsr.servicestoolkit.score.relations;

import java.util.Set;

import ch.hsr.servicestoolkit.model.Nanoentity;

public class SeparatedGroupCriterionScorer extends RelatedGroupCriteriaScorer {

	public SeparatedGroupCriterionScorer(final Set<Nanoentity> allNanoentitiesOfModel) {
		super(Scorer.MIN_SCORE, Scorer.NO_SCORE, allNanoentitiesOfModel);
	}

}
