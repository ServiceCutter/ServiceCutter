package ch.hsr.servicestoolkit.importer.api;

import java.util.List;

public class SeparationCriterion {

	private List<String> groupAnanoentities;
	private List<String> groupBnanoentities;
	private String couplingCriterionName;
	private String characteristicName;

	public SeparationCriterion(final String characteristicName, final String couplingCriterionName, final List<String> groupA, final List<String> groupB) {
		this.characteristicName = characteristicName;
		this.couplingCriterionName = couplingCriterionName;
		this.groupAnanoentities = groupA;
		this.groupBnanoentities = groupB;
	}

	public SeparationCriterion() {
	}

	public List<String> getGroupAnanoentities() {
		return groupAnanoentities;
	}

	public List<String> getGroupBnanoentities() {
		return groupBnanoentities;
	}

	public String getCharacteristicName() {
		return characteristicName;
	}

	public String getCouplingCriterionName() {
		return couplingCriterionName;
	}

}
