package ch.hsr.servicecutter.scorer.criterionScorer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

import ch.hsr.servicecutter.model.solver.EntityPair;
import ch.hsr.servicecutter.model.usersystem.CouplingInstance;
import ch.hsr.servicecutter.model.usersystem.Nanoentity;
import ch.hsr.servicecutter.scorer.criterionScorer.RelatedGroupCriteriaScorer;

public class RelatedGroupCriteriaScorerTest {

	private static final double PREMIUM = 10d;
	private static final double PENALTY = -5d;
	static long idGen = 0;

	@Test
	public void testRelatedGroups() {
		CouplingInstance group1 = new CouplingInstance();
		CouplingInstance group2 = new CouplingInstance();
		CouplingInstance group3 = new CouplingInstance();

		Nanoentity nanoentity1a = createNanoEntity("1A");
		Nanoentity nanoentity1b = createNanoEntity("1B");

		group1.setNanoentities(Arrays.asList(nanoentity1a, nanoentity1b));

		Nanoentity nanoentity2a = createNanoEntity("2A");
		Nanoentity nanoentity2b = createNanoEntity("2B");

		group2.setNanoentities(Arrays.asList(nanoentity2a, nanoentity2b));

		Nanoentity nanoentity3a = createNanoEntity("3A");
		Nanoentity nanoentity3b = createNanoEntity("3B");

		group3.setNanoentities(Arrays.asList(nanoentity3a, nanoentity3b));

		Nanoentity other = createNanoEntity("other");

		RelatedGroupCriteriaScorer scorer = new RelatedGroupCriteriaScorer(PENALTY, PREMIUM,
				Arrays.asList(nanoentity1a, nanoentity1b, nanoentity2a, nanoentity2b, nanoentity3a, nanoentity3b, other));
		Map<EntityPair, Double> scores = scorer.getScores(new HashSet<CouplingInstance>(Arrays.asList(group1, group2, group3)));

		// check premiums
		assertThat(scores.get(new EntityPair(nanoentity1a, nanoentity1b)), is(equalTo(PREMIUM)));
		assertThat(scores.get(new EntityPair(nanoentity2a, nanoentity2b)), is(equalTo(PREMIUM)));
		assertThat(scores.get(new EntityPair(nanoentity3a, nanoentity3b)), is(equalTo(PREMIUM)));

		// check penalties
		assertThat(scores.get(new EntityPair(nanoentity1a, nanoentity2b)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity1a, nanoentity2a)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity1a, nanoentity3b)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity1a, nanoentity3a)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity1b, nanoentity2a)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity1b, nanoentity2b)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity1b, nanoentity3a)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity1b, nanoentity3b)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity2a, nanoentity3a)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity2a, nanoentity3b)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity2b, nanoentity3a)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity2b, nanoentity3b)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity1a, other)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity1b, other)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity2a, other)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity2b, other)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity3a, other)), is(equalTo(PENALTY)));
		assertThat(scores.get(new EntityPair(nanoentity3b, other)), is(equalTo(PENALTY)));
	}

	private Nanoentity createNanoEntity(final String name) {
		Nanoentity nanoEntity = new Nanoentity(name);
		nanoEntity.setId(idGen++);
		return nanoEntity;
	}
}