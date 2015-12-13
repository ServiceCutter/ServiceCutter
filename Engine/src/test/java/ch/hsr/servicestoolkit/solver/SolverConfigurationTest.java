package ch.hsr.servicestoolkit.solver;

import static org.junit.Assert.assertEquals;

import java.security.InvalidParameterException;
import java.util.HashMap;

import org.junit.Test;

import ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic;

public class SolverConfigurationTest {

	@Test(expected = InvalidParameterException.class)
	public void testNullConfiguration() {
		SolverConfiguration config = new SolverConfiguration();
		config.setWeights(null);
	}

	@Test
	public void testEmptyConfig() {
		SolverConfiguration config = new SolverConfiguration();
		assertEquals(new Double(0d), config.getWeightForCharacteristic(CouplingCriterionCharacteristic.SAME_ENTITY));
	}

	@Test
	public void testRealConfig() {
		HashMap<String, Double> weights = new HashMap<String, Double>();
		weights.put(CouplingCriterionCharacteristic.SAME_ENTITY, 2.4d);
		SolverConfiguration config = new SolverConfiguration();
		config.setWeights(weights);
		assertEquals(new Double(2.4d), config.getWeightForCharacteristic(CouplingCriterionCharacteristic.SAME_ENTITY));
	}

}
