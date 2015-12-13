package ch.hsr.servicestoolkit.score;

import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic;
import ch.hsr.servicestoolkit.model.CouplingInstance;
import ch.hsr.servicestoolkit.model.Nanoentity;

public class TestDataHelper {

	public static CouplingInstance createCouplingInstance(CouplingCriterionCharacteristic characteristic, Nanoentity... fields) {
		CouplingInstance result = new CouplingInstance();
		for (Nanoentity nanoentity : fields) {
			result.addNanoentity(nanoentity);
		}
		result.setCharacteristic(characteristic);
		return result;
	}

	public static CouplingInstance createCouplingInstance(CouplingCriterionCharacteristic characteristic, Nanoentity[] fields, Nanoentity[] otherFields) {
		CouplingInstance result = new CouplingInstance();
		for (Nanoentity nanoentity : fields) {
			result.addNanoentity(nanoentity);
		}
		for (Nanoentity nanoentity : otherFields) {
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
