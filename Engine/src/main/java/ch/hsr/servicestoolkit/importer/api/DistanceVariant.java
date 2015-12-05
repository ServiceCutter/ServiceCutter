package ch.hsr.servicestoolkit.importer.api;

import java.util.List;

public class DistanceVariant {

	private List<String> nanoentities;
	private String couplingCriterionName;
	private String characteristic;

	public DistanceVariant(final String characteristic, final String couplingCriterionName, final List<String> nanoentities) {
		super();
		this.characteristic = characteristic;
		this.couplingCriterionName = couplingCriterionName;
		this.nanoentities = nanoentities;
	}

	public DistanceVariant() {
	}

	public List<String> getNanoentities() {
		return nanoentities;
	}

	public String getVariantName() {
		return characteristic;
	}

	public String getCouplingCriterionName() {
		return couplingCriterionName;
	}

}
