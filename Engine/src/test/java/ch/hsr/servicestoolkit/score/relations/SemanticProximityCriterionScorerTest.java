package ch.hsr.servicestoolkit.score.relations;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import ch.hsr.servicestoolkit.model.DataField;

public class SemanticProximityCriterionScorerTest {

	private static long idGen = 0;

	@Test
	public void testNormalizationOrdering() {
		SemanticProximityCriterionScorer sut = new SemanticProximityCriterionScorer();
		Map<FieldTuple, Double> input = new HashMap<>();

		Random rand = new Random();

		input.put(new FieldTuple(createDataField("A"), createDataField("1")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("B"), createDataField("2")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("C"), createDataField("3")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("D"), createDataField("4")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("E"), createDataField("5")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("F"), createDataField("6")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("G"), createDataField("7")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("H"), createDataField("8")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("I"), createDataField("9")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("J"), createDataField("10")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("K"), createDataField("11")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("L"), createDataField("12")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("M"), createDataField("13")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("N"), createDataField("14")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("O"), createDataField("15")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("P"), createDataField("16")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("Q"), createDataField("17")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("R"), createDataField("18")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("S"), createDataField("19")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("T"), createDataField("20")), (double) rand.nextInt(100));
		sut.normalizeResult(input);

		assertEquals(2, input.values().stream().filter(d -> d >= 10d).count());

	}

	public void testNormalizationOrderingSmall() {
		SemanticProximityCriterionScorer sut = new SemanticProximityCriterionScorer();
		Map<FieldTuple, Double> input = new HashMap<>();

		Random rand = new Random();

		input.put(new FieldTuple(createDataField("A"), createDataField("1")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("B"), createDataField("2")), (double) rand.nextInt(100));
		input.put(new FieldTuple(createDataField("C"), createDataField("3")), (double) rand.nextInt(100));
		sut.normalizeResult(input);

		assertEquals(1, input.values().stream().filter(d -> d >= 10d).count());

	}

	private DataField createDataField(final String string) {
		DataField field = new DataField(string);
		field.setId(idGen++);
		return field;
	}

}
