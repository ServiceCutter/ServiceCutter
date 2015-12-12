package ch.hsr.servicestoolkit.solver;

import static ch.hsr.servicestoolkit.model.CouplingCriteriaVariant.AGGREGATION;
import static ch.hsr.servicestoolkit.model.CouplingCriteriaVariant.COMPOSITION;
import static ch.hsr.servicestoolkit.model.CouplingCriteriaVariant.SAME_ENTITY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingType;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.model.NanoEntity;
import ch.hsr.servicestoolkit.repository.DataFieldRepository;
import ch.hsr.servicestoolkit.repository.MonoCouplingInstanceRepository;
import ch.hsr.servicestoolkit.score.relations.EntityPair;
import ch.hsr.servicestoolkit.score.relations.Score;
import ch.hsr.servicestoolkit.score.relations.Scorer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SolverConfiguration.class})
public class GephiSolverTest {

	private SolverConfiguration config;
	private AtomicLong idGenerator = new AtomicLong(10);
	private MonoCouplingInstanceRepository monoCouplingInstanceRepository;
	private DataFieldRepository nanoentityRepository;

	@Before
	public void setup() {
		config = new SolverConfiguration();
		Map<String, Double> weights = new HashMap<>();
		weights.put(AGGREGATION, 0.1d);
		weights.put(SAME_ENTITY, 0.5d);
		weights.put(COMPOSITION, 0.2d);
		config.setWeights(weights);
		config.getAlgorithmParams().put("inflation", 2d);
		config.getAlgorithmParams().put("power", 1d);
		config.getAlgorithmParams().put("prune", 0.0);
		monoCouplingInstanceRepository = mock(MonoCouplingInstanceRepository.class);
		nanoentityRepository = mock(DataFieldRepository.class);
	}

	@Test(expected = InvalidParameterException.class)
	public void testEmptyModel() {
		final Scorer scorer = new Scorer(monoCouplingInstanceRepository, nanoentityRepository);
		final Model model = new Model();
		final Map<EntityPair, Map<String, Score>> scores = scorer.getScores(model, (String key) -> {
			return config.getPriorityForCouplingCriterion(key);
		});
		new GephiSolver(model, scores, 3);
	}

	@Test
	@Ignore // TODO: Girvan Newman can't work with disconnected subgraphs
	public void testSimpleModelSomeEdges() {
		Model model = new Model();
		model.addDataField(createDataField("field1"));
		model.addDataField(createDataField("field2"));
		model.addDataField(createDataField("field3"));
		model.addDataField(createDataField("field4"));
		model.addDataField(createDataField("field5"));
		model.addDataField(createDataField("field6"));

		Set<MonoCouplingInstance> instances = new HashSet<>();
		instances.add(addCriterionFields(model, SAME_ENTITY, new String[] {"field1", "field2", "field3"}));
		instances.add(addCriterionFields(model, SAME_ENTITY, new String[] {"field4", "field5", "field6"}));
		when(monoCouplingInstanceRepository.findByModel(model)).thenReturn(instances);

		final Scorer scorer = new Scorer(monoCouplingInstanceRepository, nanoentityRepository);
		Map<EntityPair, Map<String, Score>> scores = scorer.getScores(model, (String key) -> {
			return config.getPriorityForCouplingCriterion(key);
		});
		GephiSolver solver = new GephiSolver(model, scores, null);
		SolverResult result = solver.solveWithGirvanNewman(2);

		assertEquals(2, result.getServices().size());
		for (Service context : result.getServices()) {
			assertEquals(3, context.getDataFields().size());
		}
	}

	private MonoCouplingInstance addCriterionFields(final Model model, final String variantName, final String[] fields) {
		MonoCouplingInstance instance = new MonoCouplingInstance();
		List<String> fieldsFilter = Arrays.asList(fields);
		instance.setDataFields(model.getDataFields().stream().filter(f -> fieldsFilter.contains(f.getName())).collect(Collectors.toList()));
		createVariant(variantName, instance);
		instance.setId(idGenerator.incrementAndGet());
		return instance;
	}

	void createVariant(final String variantName, final MonoCouplingInstance instance) {
		CouplingCriteriaVariant variant = new CouplingCriteriaVariant();
		variant.setName(variantName);
		CouplingCriterion couplingCriterion = new CouplingCriterion();
		couplingCriterion.setId(idGenerator.getAndIncrement());
		couplingCriterion.setType(CouplingType.COMPATIBILITY);
		couplingCriterion.setName("criterionName");
		variant.setCouplingCriterion(couplingCriterion);
		variant.setWeight(6);
		instance.setVariant(variant);
	}

	private NanoEntity createDataField(final String field) {
		NanoEntity dataField = new NanoEntity();
		dataField.setName(field);
		return dataField;
	}

}
