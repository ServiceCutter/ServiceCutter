package ch.hsr.servicestoolkit.solver;

import static org.junit.Assert.assertEquals;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CriterionType;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.Model;

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
		Set<BoundedContext> result1 = solver.solveWithMarkov();
		assertEquals(3, result1.size());
		for (BoundedContext context : result1) {
			assertEquals(2, context.getDataFields().size());
		}

		Set<BoundedContext> result2 = solver.solveWithMarkov();
		assertEquals(2, result2.size());
		for (BoundedContext context : result2) {
			assertEquals(3, context.getDataFields().size());
		}
	}

	@Test
	public void testSimpleModelSomeEdges() {
		Model model = new Model();
		model.addDataField(createDataField("field1"));
		model.addDataField(createDataField("field2"));
		model.addDataField(createDataField("field3"));
		model.addDataField(createDataField("field4"));
		model.addDataField(createDataField("field5"));
		model.addDataField(createDataField("field6"));

		addCriterionFields(model, CriterionType.SAME_ENTITIY, new String[] { "field1", "field2", "field3" });
		addCriterionFields(model, CriterionType.SAME_ENTITIY, new String[] { "field4", "field5", "field6" });

		GephiSolver solver = new GephiSolver(model, config);
		Set<BoundedContext> result = solver.solveWithMarkov();

		assertEquals(2, result.size());
		for (BoundedContext context : result) {
			assertEquals(3, context.getDataFields().size());
		}
	}

	private void addCriterionFields(Model model, CriterionType compositionEntity, String[] fields) {
		CouplingCriterion criterion = new CouplingCriterion();
		List<String> fieldsFilter = Arrays.asList(fields);
		criterion.setDataFields(model.getDataFields().stream().filter(f -> fieldsFilter.contains(f.getName()))
				.collect(Collectors.toList()));
		criterion.setCriterionType(compositionEntity);
		criterion.setId(idGenerator.incrementAndGet());

		for (DataField dataField : model.getDataFields()) {
			if (fieldsFilter.contains(dataField.getName())) {
				dataField.addCouplingCriterion(criterion);
			}
		}
	}

	private DataField createDataField(String field) {
		DataField dataField = new DataField();
		dataField.setName(field);
		return dataField;
	}

}
