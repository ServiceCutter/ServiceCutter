package ch.hsr.servicestoolkit.scorer.criterionScorer;

import java.util.Set;

import ch.hsr.servicestoolkit.model.systemdata.Nanoentity;
import ch.hsr.servicestoolkit.scorer.Scorer;

public class ExclusiveGroupCriterionScorer extends RelatedGroupCriteriaScorer {

	public ExclusiveGroupCriterionScorer(final Set<Nanoentity> set) {
		super(Scorer.MIN_SCORE, Scorer.MAX_SCORE, set);
	}

}
