package ch.hsr.servicestoolkit.scorer.criterionScorer;

import java.util.Set;

import ch.hsr.servicestoolkit.model.systemdata.Nanoentity;
import ch.hsr.servicestoolkit.scorer.Scorer;

public class SeparatedGroupCriterionScorer extends RelatedGroupCriteriaScorer {

	public SeparatedGroupCriterionScorer(final Set<Nanoentity> allNanoentitiesOfModel) {
		super(Scorer.MIN_SCORE, Scorer.NO_SCORE, allNanoentitiesOfModel);
	}

}
