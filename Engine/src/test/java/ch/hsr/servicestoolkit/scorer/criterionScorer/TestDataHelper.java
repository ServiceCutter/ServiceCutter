package ch.hsr.servicestoolkit.scorer.criterionScorer;

import ch.hsr.servicestoolkit.model.criteria.CouplingCriterion;
import ch.hsr.servicestoolkit.model.criteria.CouplingCriterionCharacteristic;
import ch.hsr.servicestoolkit.model.systemdata.CouplingInstance;
import ch.hsr.servicestoolkit.model.systemdata.Nanoentity;

public class TestDataHelper {

	public static CouplingInstance createCouplingInstance(CouplingCriterionCharacteristic characteristic, Nanoentity... nanoentities) {
		CouplingInstance result = new CouplingInstance();
		for (Nanoentity nanoentity : nanoentities) {
			result.addNanoentity(nanoentity);
		}
		result.setCharacteristic(characteristic);
		return result;
	}

	public static CouplingInstance createCouplingInstance(CouplingCriterionCharacteristic characteristic, Nanoentity[] nanoentities, Nanoentity[] otherNanoentities) {
		CouplingInstance result = new CouplingInstance();
		for (Nanoentity nanoentity : nanoentities) {
			result.addNanoentity(nanoentity);
		}
		for (Nanoentity nanoentity : otherNanoentities) {
			result.addSecondNanoentity(nanoentity);
		}
		result.setCharacteristic(characteristic);
		return result;
	}

	public static CouplingCriterionCharacteristic createCharacteristic(CouplingCriterion criterion, int weight, String name) {
		CouplingCriterionCharacteristic characteristic = new CouplingCriterionCharacteristic();
		characteristic.setCouplingCriterion(criterion);
		characteristic.setWeight(weight);
		characteristic.setName(name);
		return characteristic;
	}
}
