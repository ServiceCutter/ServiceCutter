package ch.hsr.servicestoolkit.importer.api;

import java.util.List;

public class SeparationCriterion {

	private List<String> groupAFields;
	private List<String> groupBFields;
	private String couplingCriterionName;
	private String variantName;

	public SeparationCriterion(final String variantName, final String couplingCriterionName, final List<String> groupA, final List<String> groupB) {
		this.variantName = variantName;
		this.couplingCriterionName = couplingCriterionName;
		this.groupAFields = groupA;
		this.groupBFields = groupB;
	}

	public SeparationCriterion() {
	}

	public List<String> getGroupAFields() {
		return groupAFields;
	}

	public List<String> getGroupBFields() {
		return groupBFields;
	}

	public String getVariantName() {
		return variantName;
	}

	public String getCouplingCriterionName() {
		return couplingCriterionName;
	}

}
