package ch.hsr.servicestoolkit.score.relations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.model.NanoEntity;

public class ExclusiveGroupCriteriaScorerTest {

	private static final double PREMIUM = 10d;
	private static final double PENALTY = -5d;
	static long idGen = 0;

	@Test
	public void testExclusiveGroups() {
		MonoCouplingInstance group1 = new MonoCouplingInstance();
		MonoCouplingInstance group2 = new MonoCouplingInstance();
		MonoCouplingInstance group3 = new MonoCouplingInstance();

		NanoEntity nanoentity1a = createNanoEntity("1A");
		NanoEntity nanoentity1b = createNanoEntity("1B");

		group1.setDataFields(Arrays.asList(nanoentity1a, nanoentity1b));

		NanoEntity nanoentity2a = createNanoEntity("2A");
		NanoEntity nanoentity2b = createNanoEntity("2B");

		group2.setDataFields(Arrays.asList(nanoentity2a, nanoentity2b));

		NanoEntity nanoentity3a = createNanoEntity("3A");
		NanoEntity nanoentity3b = createNanoEntity("3B");

		group3.setDataFields(Arrays.asList(nanoentity3a, nanoentity3b));

		NanoEntity other = createNanoEntity("other");

		ExclusiveGroupCriteriaScorer scorer = new ExclusiveGroupCriteriaScorer(PENALTY, PREMIUM,
				Arrays.asList(nanoentity1a, nanoentity1b, nanoentity2a, nanoentity2b, nanoentity3a, nanoentity3b, other));
		Map<EntityPair, Double> scores = scorer.getScores(new HashSet<MonoCouplingInstance>(Arrays.asList(group1, group2, group3)));

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

	private NanoEntity createNanoEntity(final String name) {
		NanoEntity nanoEntity = new NanoEntity(name);
		nanoEntity.setId(idGen++);
		return nanoEntity;
	}
}