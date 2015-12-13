package ch.hsr.servicestoolkit.importer.api;

import java.util.List;

public class CohesiveGroups {

	private List<CohesiveGroup> cohesiveGroups;
	private String couplingCriterionName;
	private String characteristicName;

	public CohesiveGroups(final String characteristicName, final String couplingCriterionName, final List<CohesiveGroup> cohesiveGroups) {
		this.characteristicName = characteristicName;
		this.couplingCriterionName = couplingCriterionName;
		this.cohesiveGroups = cohesiveGroups;
	}

	public CohesiveGroups() {
	}

	public List<CohesiveGroup> getCohesiveGroups() {
		return cohesiveGroups;
	}

	public String getCharacteristicName() {
		return characteristicName;
	}

	public String getCouplingCriterionName() {
		return couplingCriterionName;
	}
}
