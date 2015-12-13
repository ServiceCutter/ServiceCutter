package ch.hsr.servicestoolkit.score;

import ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic;
import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.NanoEntity;
import ch.hsr.servicestoolkit.model.DualCouplingInstance;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;

public class TestDataHelper {

	public static MonoCouplingInstance createCouplingInstance(CouplingCriterionCharacteristic variant, NanoEntity... fields) {
		MonoCouplingInstance result = new MonoCouplingInstance();
		for (NanoEntity dataField : fields) {
			result.addDataField(dataField);
		}
		result.setVariant(variant);
		return result;
	}

	public static DualCouplingInstance createCouplingInstance(CouplingCriterionCharacteristic variant, NanoEntity[] fields, NanoEntity[] otherFields) {
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

	public static CouplingCriterionCharacteristic createVariant(CouplingCriterion criterion, int weight, String name) {
		CouplingCriterionCharacteristic variant = new CouplingCriterionCharacteristic();
		variant.setCouplingCriterion(criterion);
		variant.setWeight(weight);
		variant.setName(name);
		return variant;
	}
}
