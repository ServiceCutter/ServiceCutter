package ch.hsr.servicecutter.scorer.criterionScorer;

import java.util.Set;

import ch.hsr.servicecutter.model.userdata.Nanoentity;
import ch.hsr.servicecutter.scorer.Scorer;

public class SeparatedGroupCriterionScorer extends RelatedGroupCriteriaScorer {

	public SeparatedGroupCriterionScorer(final Set<Nanoentity> allNanoentitiesOfModel) {
		super(Scorer.MIN_SCORE, Scorer.NO_SCORE, allNanoentitiesOfModel);
	}

}
