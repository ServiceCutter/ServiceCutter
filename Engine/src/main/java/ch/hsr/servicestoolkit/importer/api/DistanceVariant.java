package ch.hsr.servicestoolkit.importer.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	public static void main(final String[] args) throws JsonProcessingException {
		List<DistanceVariant> variants = new ArrayList<>();
		List<String> fieldsOften = new ArrayList<>();
		fieldsOften.add("fieldA");
		fieldsOften.add("fieldB");
		DistanceVariant variantVolatility = new DistanceVariant("Often", "Volatility", fieldsOften);
		DistanceVariant variantVolatility2 = new DistanceVariant("Regularly", "Volatility", fieldsOften);
		DistanceVariant variantVolatility3 = new DistanceVariant("Rarely", "Volatility", fieldsOften);
		variants.add(variantVolatility);
		variants.add(variantVolatility2);
		variants.add(variantVolatility3);
		ObjectMapper mapper = new ObjectMapper();
		System.out.println(mapper.writeValueAsString(variants));
	}

}
