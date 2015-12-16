package ch.hsr.servicecutter.importer.api;

import java.util.List;

public class RelatedGroups {

	private List<RelatedGroup> relatedGroups;
	private String couplingCriterionName;

	public RelatedGroups(final String characteristicName, final String couplingCriterionName, final List<RelatedGroup> relatedGroups) {
		this.couplingCriterionName = couplingCriterionName;
		this.relatedGroups = relatedGroups;
	}

	public RelatedGroups() {
	}

	public String getCouplingCriterionName() {
		return couplingCriterionName;
	}

	public List<RelatedGroup> getRelatedGroups() {
		return relatedGroups;
	}
}
