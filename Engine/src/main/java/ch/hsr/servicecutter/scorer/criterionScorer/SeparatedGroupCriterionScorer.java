package ch.hsr.servicecutter.scorer.criterionScorer;

import ch.hsr.servicecutter.scorer.Scorer;

public class SeparatedGroupCriterionScorer extends RelatedGroupCriteriaScorer {

	public SeparatedGroupCriterionScorer() {
		super(Scorer.MIN_SCORE, Scorer.NO_SCORE);
	}

}
