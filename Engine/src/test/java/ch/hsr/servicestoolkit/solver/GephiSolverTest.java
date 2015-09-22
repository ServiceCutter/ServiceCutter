package ch.hsr.servicestoolkit.solver;

import static org.junit.Assert.assertEquals;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.hsr.servicestoolkit.model.CriterionType;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.CouplingCriterion;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SolverConfiguration.class })
public class GephiSolverTest {

	private SolverConfiguration config;
	private AtomicLong idGenerator = new AtomicLong(10);

	@Before
	public void setup() {
		config = new SolverConfiguration();
		Map<CriterionType, Double> weights = new HashMap<>();
		weights.put(CriterionType.AGGREGATED_ENTITY, 0.1d);
		weights.put(CriterionType.SAME_ENTITIY, 0.5d);
		weights.put(CriterionType.COMPOSITION_ENTITY, 0.2d);
		config.setWeights(weights);
	}

	@Test(expected = InvalidParameterException.class)
	public void testEmptyModel() {
		new GephiSolver(new Model(), config);
	}

	@Ignore
	@Test
	public void testSimpleModelNoEdges() {
		Model model = new Model();
		model.addDataField(createDataField("field1"));
		model.addDataField(createDataField("field2"));
		model.addDataField(createDataField("field3"));
		model.addDataField(createDataField("field4"));
		model.addDataField(createDataField("field5"));
		model.addDataField(createDataField("field6"));
		GephiSolver solver = new GephiSolver(model, config);
		List<BoundedContext> result1 = solver.solve(3);
		assertEquals(3, result1.size());
		for (BoundedContext context : result1) {
			assertEquals(2, context.getDataFields().size());
		}

		List<BoundedContext> result2 = solver.solve(2);
		assertEquals(2, result2.size());
		for (BoundedContext context : result2) {
			assertEquals(3, context.getDataFields().size());
		}
	}

	@Ignore
	@Test
	public void testSimpleModelSomeEdges() {
		Model model = new Model();
		model.addDataField(createDataField("field1"));
		model.addDataField(createDataField("field2"));

		addCriterionToAllFields(model, CriterionType.COMPOSITION_ENTITY);

		model.addDataField(createDataField("field3"));

		addCriterionToAllFields(model, CriterionType.SAME_ENTITIY);

		model.addDataField(createDataField("field4"));
		model.addDataField(createDataField("field5"));
		model.addDataField(createDataField("field6"));

		addCriterionToAllFields(model, CriterionType.AGGREGATED_ENTITY);

		model.addDataField(createDataField("field7"));
		model.addDataField(createDataField("field8"));
		model.addDataField(createDataField("field9"));
		model.addDataField(createDataField("field10"));
		model.addDataField(createDataField("field11"));
		model.addDataField(createDataField("field12"));

		GephiSolver solver = new GephiSolver(model, config);
		List<BoundedContext> result = solver.solve(3);
		assertEquals(1, result.size());
		assertEquals(3, result.get(0).getDataFields().size());
	}

	private void addCriterionToAllFields(Model model, CriterionType compositionEntity) {
		CouplingCriterion criterion = new CouplingCriterion();
		criterion.setDataFields(model.getDataFields());
		criterion.setCriterionType(compositionEntity);
		criterion.setId(idGenerator.incrementAndGet());

		for (DataField dataField : model.getDataFields()) {
			dataField.addCouplingCriterion(criterion);
		}
	}

	private DataField createDataField(String field) {
		DataField dataField = new DataField();
		dataField.setName(field);
		return dataField;
	}

}
