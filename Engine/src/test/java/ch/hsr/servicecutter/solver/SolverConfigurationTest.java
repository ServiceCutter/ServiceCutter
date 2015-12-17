package ch.hsr.servicecutter.solver;

import static org.junit.Assert.assertEquals;

import java.security.InvalidParameterException;
import java.util.HashMap;

import org.junit.Test;

public class SolverConfigurationTest {

	@Test(expected = InvalidParameterException.class)
	public void testNullConfiguration() {
		SolverConfiguration config = new SolverConfiguration();
		config.setPriorities(null);
	}

	@Test
	public void testEmptyConfig() {
		SolverConfiguration config = new SolverConfiguration();
		assertEquals(new Double(1.0d), config.getPriorityForCouplingCriterion("sameEntity"));
	}

	@Test
	public void testRealConfig() {
		HashMap<String, Double> weights = new HashMap<String, Double>();
		weights.put("sameEntity", 2.4d);
		SolverConfiguration config = new SolverConfiguration();
		config.setPriorities(weights);
		assertEquals(new Double(2.4d), config.getPriorityForCouplingCriterion("sameEntity"));
	}

}
