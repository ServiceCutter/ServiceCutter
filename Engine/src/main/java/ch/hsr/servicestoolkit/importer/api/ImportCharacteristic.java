package ch.hsr.servicestoolkit.importer.api;

import java.util.List;

public class ImportCharacteristic {

	private List<String> nanoentities;
	private String couplingCriterionName;
	private String characteristic;

	public ImportCharacteristic(final String characteristic, final String couplingCriterionName, final List<String> nanoentities) {
		super();
		this.characteristic = characteristic;
		this.couplingCriterionName = couplingCriterionName;
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

	public String getCouplingCriterionName() {
		return couplingCriterionName;
	}

}
