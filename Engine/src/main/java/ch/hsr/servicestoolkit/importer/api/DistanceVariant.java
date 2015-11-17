package ch.hsr.servicestoolkit.importer.api;

import java.util.List;

public class DistanceVariant {

	private List<String> fields;
	private String couplingCriterionName;
	private String variantName;

	public DistanceVariant(final String variantName, final String couplingCriterionName, final List<String> dataFields) {
		super();
		this.variantName = variantName;
		this.couplingCriterionName = couplingCriterionName;
		this.fields = dataFields;
	}

	public DistanceVariant() {
	}

	public List<String> getFields() {
		return fields;
	}

	public String getVariantName() {
		return variantName;
	}

	public String getCouplingCriterionName() {
		return couplingCriterionName;
	}

}
