package ch.hsr.servicestoolkit.solver;

import static ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic.AGGREGATION;
import static ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic.COMPOSITION;
import static ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic.SAME_ENTITY;
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

import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic;
import ch.hsr.servicestoolkit.model.CouplingInstance;
import ch.hsr.servicestoolkit.model.CouplingType;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.Nanoentity;
import ch.hsr.servicestoolkit.model.repository.NanoentityRepository;
import ch.hsr.servicestoolkit.model.repository.CouplingInstanceRepository;
import ch.hsr.servicestoolkit.score.relations.EntityPair;
import ch.hsr.servicestoolkit.score.relations.Score;
import ch.hsr.servicestoolkit.score.relations.Scorer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SolverConfiguration.class})
public class GephiSolverTest {

	private SolverConfiguration config;
	private AtomicLong idGenerator = new AtomicLong(10);
	private CouplingInstanceRepository couplingInstanceRepository;
	private NanoentityRepository nanoentityRepository;

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
		couplingInstanceRepository = mock(CouplingInstanceRepository.class);
		nanoentityRepository = mock(NanoentityRepository.class);
	}

	@Test(expected = InvalidParameterException.class)
	public void testEmptyModel() {
		final Scorer scorer = new Scorer(couplingInstanceRepository, nanoentityRepository);
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
		model.addNanoentity(createNanoentity("field1"));
		model.addNanoentity(createNanoentity("field2"));
		model.addNanoentity(createNanoentity("field3"));
		model.addNanoentity(createNanoentity("field4"));
		model.addNanoentity(createNanoentity("field5"));
		model.addNanoentity(createNanoentity("field6"));

		Set<CouplingInstance> instances = new HashSet<>();
		instances.add(addCriterionFields(model, SAME_ENTITY, new String[] {"field1", "field2", "field3"}));
		instances.add(addCriterionFields(model, SAME_ENTITY, new String[] {"field4", "field5", "field6"}));
		when(couplingInstanceRepository.findByModel(model)).thenReturn(instances);

		final Scorer scorer = new Scorer(couplingInstanceRepository, nanoentityRepository);
		Map<EntityPair, Map<String, Score>> scores = scorer.getScores(model, (String key) -> {
			return config.getPriorityForCouplingCriterion(key);
		});
		GephiSolver solver = new GephiSolver(model, scores, null);
		SolverResult result = solver.solveWithGirvanNewman(2);

		assertEquals(2, result.getServices().size());
		for (Service context : result.getServices()) {
			assertEquals(3, context.getNanoentities().size());
		}
	}

	private CouplingInstance addCriterionFields(final Model model, final String characteristicName, final String[] fields) {
		CouplingInstance instance = new CouplingInstance();
		List<String> fieldsFilter = Arrays.asList(fields);
		instance.setNanoentities(model.getNanoentities().stream().filter(f -> fieldsFilter.contains(f.getName())).collect(Collectors.toList()));
		createCharacteristic(characteristicName, instance);
		instance.setId(idGenerator.incrementAndGet());
		return instance;
	}

	void createCharacteristic(final String characteristicName, final CouplingInstance instance) {
		CouplingCriterionCharacteristic characteristic = new CouplingCriterionCharacteristic();
		characteristic.setName(characteristicName);
		CouplingCriterion couplingCriterion = new CouplingCriterion();
		couplingCriterion.setId(idGenerator.getAndIncrement());
		couplingCriterion.setType(CouplingType.COMPATIBILITY);
		couplingCriterion.setName("criterionName");
		characteristic.setCouplingCriterion(couplingCriterion);
		characteristic.setWeight(6);
		instance.setCharacteristic(characteristic);
	}

	private Nanoentity createNanoentity(final String field) {
		Nanoentity nanoentity = new Nanoentity();
		nanoentity.setName(field);
		return nanoentity;
	}

}
