package ch.hsr.servicestoolkit.scorer.criterionScorer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import ch.hsr.servicestoolkit.model.systemdata.Nanoentity;
import ch.hsr.servicestoolkit.scorer.EntityPair;

public class SemanticProximityCriterionScorerTest {

	private static long idGen = 0;

	@Test
	public void testNormalizationOrdering() {

		for (int i = 1; i <= 10; i++) {
			testNumberOfMaxScores(i, 1);
		}
		for (int i = 11; i <= 100; i++) {
			testNumberOfMaxScores(i, i / 10);
		}

	}

	private void testNumberOfMaxScores(final int numberOfEdges, final long numberOfExpectedMaxScores) {
		SemanticProximityCriterionScorer sut = new SemanticProximityCriterionScorer();
		Map<EntityPair, Double> input = new HashMap<>();

		Random rand = new Random();
		for (int i = 0; i < numberOfEdges; i++) {
			input.put(new EntityPair(createNanoentity("A"), createNanoentity(i + "")), (double) rand.nextInt(100));
		}
		sut.normalizeResult(input);
		assertThat(input.values().stream().filter(d -> d >= 10d).count(), greaterThanOrEqualTo(numberOfExpectedMaxScores));
	}

	private Nanoentity createNanoentity(final String string) {
		Nanoentity nanoentity = new Nanoentity(string);
		nanoentity.setId(idGen++);
		return nanoentity;
	}

}
