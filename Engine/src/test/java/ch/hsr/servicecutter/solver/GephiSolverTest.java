package ch.hsr.servicecutter.solver;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.hsr.servicecutter.model.criteria.CouplingCriterion;
import ch.hsr.servicecutter.model.criteria.CouplingType;
import ch.hsr.servicecutter.model.repository.CouplingInstanceRepository;
import ch.hsr.servicecutter.model.repository.NanoentityRepository;
import ch.hsr.servicecutter.model.systemdata.CouplingInstance;
import ch.hsr.servicecutter.model.systemdata.InstanceType;
import ch.hsr.servicecutter.model.systemdata.Model;
import ch.hsr.servicecutter.model.systemdata.Nanoentity;
import ch.hsr.servicecutter.scorer.EntityPair;
import ch.hsr.servicecutter.scorer.Score;
import ch.hsr.servicecutter.scorer.Scorer;
import ch.hsr.servicecutter.solver.GephiSolver;
import ch.hsr.servicecutter.solver.Service;
import ch.hsr.servicecutter.solver.SolverConfiguration;
import ch.hsr.servicecutter.solver.SolverResult;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SolverConfiguration.class})
public class GephiSolverTest {

	private SolverConfiguration config;
	private AtomicLong idGenerator = new AtomicLong(10);
	private CouplingInstanceRepository couplingInstanceRepository;
	private NanoentityRepository nanoentityRepository;
	private CouplingCriterion identityCoupling;
	private CouplingCriterion semanticCoupling;

	@Before
	public void setup() {
		config = new SolverConfiguration();
		config.getAlgorithmParams().put("inflation", 2d);
		config.getAlgorithmParams().put("power", 1d);
		config.getAlgorithmParams().put("prune", 0.0);
		couplingInstanceRepository = mock(CouplingInstanceRepository.class);
		nanoentityRepository = mock(NanoentityRepository.class);
		identityCoupling = createCriterion(CouplingType.COHESIVENESS, CouplingCriterion.IDENTITY_LIFECYCLE);
		semanticCoupling = createCriterion(CouplingType.COHESIVENESS, CouplingCriterion.SEMANTIC_PROXIMITY);

	}

	private CouplingCriterion createCriterion(CouplingType type, String name) {
		CouplingCriterion criterion = new CouplingCriterion();
		criterion.setId(idGenerator.getAndIncrement());
		criterion.setType(type);
		criterion.setName(name);
		return criterion;
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
	public void testSimpleModelSomeEdges() {
		Model model = new Model();
		model.addNanoentity(createNanoentity("nanoentity1"));
		model.addNanoentity(createNanoentity("nanoentity2"));
		model.addNanoentity(createNanoentity("nanoentity3"));
		model.addNanoentity(createNanoentity("nanoentity4"));
		model.addNanoentity(createNanoentity("nanoentity5"));
		model.addNanoentity(createNanoentity("nanoentity6"));

		Set<CouplingInstance> entityCoupling = new HashSet<>();
		entityCoupling.add(createInstance(model, new String[] {"nanoentity1", "nanoentity2", "nanoentity3"}));
		entityCoupling.add(createInstance(model, new String[] {"nanoentity4", "nanoentity5", "nanoentity6"}));
		Set<CouplingInstance> relationshipCoupling = createRelationship(model);
		when(couplingInstanceRepository.findByModel(model)).thenReturn(entityCoupling);
		when(couplingInstanceRepository.findByModelAndCriterion(model, identityCoupling.getName())).thenReturn(entityCoupling);
		when(couplingInstanceRepository.findByModelAndCriterion(model, semanticCoupling.getName())).thenReturn(relationshipCoupling);

		final Scorer scorer = new Scorer(couplingInstanceRepository, nanoentityRepository);
		Map<EntityPair, Map<String, Score>> scores = scorer.getScores(model, (String key) -> {
			return config.getPriorityForCouplingCriterion(key);
		});
		GephiSolver solver = new GephiSolver(model, scores, null);
		SolverResult result = solver.solveWithGirvanNewman(2);

		assertThat(result.getServices(), hasSize(2));
		for (Service context : result.getServices()) {
			assertThat(context.getNanoentities(), hasSize(3));
		}
	}

	private Set<CouplingInstance> createRelationship(Model model) {
		Set<CouplingInstance> relationshipCoupling = new HashSet<>();
		CouplingInstance relationship = new CouplingInstance(semanticCoupling, InstanceType.AGGREGATION);
		relationship.addNanoentity(model.getNanoentities().get(0)); // nanoentity1
		relationship.addNanoentity(model.getNanoentities().get(3)); // nanoentity4
		relationshipCoupling.add(relationship);
		return relationshipCoupling;
	}

	private CouplingInstance createInstance(final Model model, final String[] nanoentities) {
		CouplingInstance instance = new CouplingInstance(identityCoupling, InstanceType.SAME_ENTITY);
		List<String> filter = Arrays.asList(nanoentities);
		instance.setNanoentities(model.getNanoentities().stream().filter(f -> filter.contains(f.getName())).collect(Collectors.toList()));
		instance.setId(idGenerator.incrementAndGet());
		return instance;
	}

	private Nanoentity createNanoentity(final String name) {
		Nanoentity nanoentity = new Nanoentity();
		nanoentity.setName(name);
		nanoentity.setId(idGenerator.incrementAndGet());
		return nanoentity;
	}

}
