package ch.hsr.servicestoolkit.score.relations;

import java.util.Set;

import ch.hsr.servicestoolkit.model.Nanoentity;

public class CohesiveGroupCriterionScorer extends RelatedGroupCriteriaScorer {

	public CohesiveGroupCriterionScorer(final Set<Nanoentity> allNanoentitiesOfModel) {
		super(Scorer.NO_SCORE, Scorer.MAX_SCORE, allNanoentitiesOfModel);
	}

}
