package ch.hsr.servicecutter.scorer.criterionScorer;

import java.util.Set;

import ch.hsr.servicecutter.model.usersystem.Nanoentity;
import ch.hsr.servicecutter.scorer.Scorer;

public class ExclusiveGroupCriterionScorer extends RelatedGroupCriteriaScorer {

	public ExclusiveGroupCriterionScorer(final Set<Nanoentity> set) {
		super(Scorer.MIN_SCORE, Scorer.MAX_SCORE, set);
	}

}
