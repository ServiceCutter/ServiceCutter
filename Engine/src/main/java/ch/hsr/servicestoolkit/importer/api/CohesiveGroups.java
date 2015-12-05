package ch.hsr.servicestoolkit.importer.api;

import java.util.List;

public class CohesiveGroups {

	private List<CohesiveGroup> cohesiveGroups;
	private String couplingCriterionName;
	private String variantName;

	public CohesiveGroups(final String variantName, final String couplingCriterionName, final List<CohesiveGroup> cohesiveGroups) {
		this.variantName = variantName;
		this.couplingCriterionName = couplingCriterionName;
		this.cohesiveGroups = cohesiveGroups;
	}

	public CohesiveGroups() {
	}

	public List<CohesiveGroup> getCohesiveGroups() {
		return cohesiveGroups;
	}

	public String getVariantName() {
		return variantName;
	}

	public String getCouplingCriterionName() {
		return couplingCriterionName;
	}
}
