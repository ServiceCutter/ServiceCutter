package ch.hsr.servicestoolkit.importer.api;

import java.util.List;

public class ImportCharacteristic {

	private List<String> nanoentities;
	private String compatibilityCriterion;
	private String characteristic;

	public ImportCharacteristic(final String characteristic, final String couplingCriterionName, final List<String> nanoentities) {
		super();
		this.characteristic = characteristic;
		this.setCompatibilityCriterion(couplingCriterionName);
		this.nanoentities = nanoentities;
	}

	public ImportCharacteristic() {
	}

	public List<String> getNanoentities() {
		return nanoentities;
	}

	public String getCharacteristicName() {
		return characteristic;
	}

	public String getCompatibilityCriterion() {
		return compatibilityCriterion;
	}

	public void setCompatibilityCriterion(final String compatibilityCriterion) {
		this.compatibilityCriterion = compatibilityCriterion;
	}

}
