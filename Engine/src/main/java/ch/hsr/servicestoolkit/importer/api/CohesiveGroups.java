package ch.hsr.servicestoolkit.importer.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	public static void main(final String[] args) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		List<CohesiveGroups> list = new ArrayList<>();

		List<CohesiveGroup> groups = new ArrayList<>();

		List<String> list1 = new ArrayList<>();
		list1.add("field1");
		list1.add("field2");
		CohesiveGroup group1 = new CohesiveGroup(list1, "group1");

		groups.add(group1);

		List<String> list2 = new ArrayList<>();
		list2.add("field3");
		list2.add("field4");

		CohesiveGroup group2 = new CohesiveGroup(list2, "group2");

		groups.add(group2);

		CohesiveGroups cohesiveGroups = new CohesiveGroups("Responsibility Area", "Responsibility", groups);
		list.add(cohesiveGroups);
		System.out.println(mapper.writeValueAsString(list));

	}

}
