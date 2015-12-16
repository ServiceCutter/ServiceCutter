package ch.hsr.servicecutter.model;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import ch.hsr.servicecutter.model.criteria.CouplingCriterion;

public class CouplingCriterionTest {

	@Test
	public void testEquals() {
		CouplingCriterion cc = new CouplingCriterion();
		assertThat(cc.equals(null), is(Boolean.FALSE));
		assertThat(cc.equals("foo"), is(Boolean.FALSE));
		// empy objects are equal
		CouplingCriterion other = new CouplingCriterion();
		assertThat(cc.equals(other), is(Boolean.TRUE));
		// id is different
		cc.setId(42l);
		assertThat(cc.equals(other), is(Boolean.FALSE));
		// id is same
		other.setId(42l);
		assertThat(cc.equals(other), is(Boolean.TRUE));
		// other fields are different
		assertThat(cc.equals(other), is(Boolean.TRUE));
	}

}
