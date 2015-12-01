package ch.hsr.servicestoolkit.score;

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.NanoEntity;
import ch.hsr.servicestoolkit.model.DualCouplingInstance;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;

public class TestDataHelper {

	public static MonoCouplingInstance createCouplingInstance(CouplingCriteriaVariant variant, NanoEntity... fields) {
		MonoCouplingInstance result = new MonoCouplingInstance();
		for (NanoEntity dataField : fields) {
			result.addDataField(dataField);
		}
		result.setVariant(variant);
		return result;
	}

	public static DualCouplingInstance createCouplingInstance(CouplingCriteriaVariant variant, NanoEntity[] fields, NanoEntity[] otherFields) {
		DualCouplingInstance result = new DualCouplingInstance();
		for (NanoEntity dataField : fields) {
			result.addDataField(dataField);
		}
		for (NanoEntity dataField : otherFields) {
			result.addSecondDataField(dataField);
		}
		result.setVariant(variant);
		return result;
	}

	public static CouplingCriteriaVariant createVariant(CouplingCriterion criterion, int weight, String name) {
		CouplingCriteriaVariant variant = new CouplingCriteriaVariant();
		variant.setCouplingCriterion(criterion);
		variant.setWeight(weight);
		variant.setName(name);
		return variant;
	}
}
