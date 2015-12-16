package ch.hsr.servicecutter.scorer.criterionScorer;

import java.util.Set;

import ch.hsr.servicecutter.model.systemdata.Nanoentity;
import ch.hsr.servicecutter.scorer.Scorer;

public class CohesiveGroupCriterionScorer extends RelatedGroupCriteriaScorer {

	public CohesiveGroupCriterionScorer(final Set<Nanoentity> allNanoentitiesOfModel) {
		super(Scorer.NO_SCORE, Scorer.MAX_SCORE, allNanoentitiesOfModel);
	}

}
