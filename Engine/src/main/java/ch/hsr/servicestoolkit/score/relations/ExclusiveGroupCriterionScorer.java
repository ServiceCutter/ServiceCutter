package ch.hsr.servicestoolkit.score.relations;

import java.util.Set;

import ch.hsr.servicestoolkit.model.Nanoentity;

public class ExclusiveGroupCriterionScorer extends RelatedGroupCriteriaScorer {

	public ExclusiveGroupCriterionScorer(final Set<Nanoentity> set) {
		super(Scorer.MIN_SCORE, Scorer.MAX_SCORE, set);
	}

}
