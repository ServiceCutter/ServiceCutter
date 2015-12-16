package ch.hsr.servicestoolkit.scorer.criterionScorer;

import java.util.Set;

import ch.hsr.servicestoolkit.model.systemdata.Nanoentity;
import ch.hsr.servicestoolkit.scorer.Scorer;

public class CohesiveGroupCriterionScorer extends RelatedGroupCriteriaScorer {

	public CohesiveGroupCriterionScorer(final Set<Nanoentity> allNanoentitiesOfModel) {
		super(Scorer.NO_SCORE, Scorer.MAX_SCORE, allNanoentitiesOfModel);
	}

}
