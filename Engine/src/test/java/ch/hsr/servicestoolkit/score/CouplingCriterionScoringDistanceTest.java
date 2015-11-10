package ch.hsr.servicestoolkit.score;

import static ch.hsr.servicestoolkit.score.TestDataHelper.createCouplingInstance;
import static ch.hsr.servicestoolkit.score.TestDataHelper.createVariant;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingType;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.model.service.ServiceCut;

public class CouplingCriterionScoringDistanceTest {

	private DataField fieldIsin;
	private DataField fieldName;
	private DataField fieldDatetime;
	private DataField fieldAmount;
	private DataField fieldIssuer;
	private DataField fieldYield;
	private Model model;
	private CouplingContext couplingContext;
	private CouplingCriterion volatility;
	private CouplingCriteriaVariant often;
	private CouplingCriteriaVariant regularly;
	private CouplingCriteriaVariant rarely;
	private CouplingCriterionScoring couplingCriterionScoring = new CouplingCriterionScoring();
	private MonoCouplingInstance rarelyCoupling;
	private MonoCouplingInstance oftenCoupling;
	private MonoCouplingInstance regularlyCoupling;

	/**
	 * Stock (ISIN, Name), Bond (Issuer, Yield), Price (Datetime, Amount)
	 */
	@Before
	public void setup() {
		// fields & model
		fieldIsin = new DataField("ISIN");
		fieldName = new DataField("Name");
		fieldDatetime = new DataField("Datetime");
		fieldAmount = new DataField("Amount");
		fieldIssuer = new DataField("Issuer");
		fieldYield = new DataField("Yield");
		model = new Model();
		model.addDataField(fieldIsin);
		model.addDataField(fieldName);
		model.addDataField(fieldDatetime);
		model.addDataField(fieldAmount);
		model.addDataField(fieldIssuer);
		model.addDataField(fieldYield);
		// coupling
		volatility = new CouplingCriterion();
		volatility.setType(CouplingType.DISTANCE);
		volatility.setName(CouplingCriterion.VOLATILITY);
		often = createVariant(volatility, 1, "Often");
		regularly = createVariant(volatility, 5, "Regularly");
		rarely = createVariant(volatility, 9, "Rarely");
		// coupling instances
		rarelyCoupling = createCouplingInstance(rarely, fieldIsin, fieldYield); // 9
		regularlyCoupling = createCouplingInstance(regularly, fieldName, fieldIssuer); // 5
		oftenCoupling = createCouplingInstance(often, fieldDatetime, fieldAmount); // 1
		// context
		couplingContext = new CouplingContext(model, Arrays.asList(rarelyCoupling, regularlyCoupling, oftenCoupling));
	}

	@Test
	public void volatilityCoupling() {
		ServiceCut perfectCut = new ServiceCut();
		perfectCut.addService(fieldIsin, fieldYield);
		perfectCut.addService(fieldName, fieldIssuer);
		perfectCut.addService(fieldDatetime, fieldAmount);
		ServiceCut badCut = new ServiceCut();
		// weights: 9, 5, 1 // mean: 5 // deviation: 4+0+4
		// score: 10-(8*2)/3=4.66
		badCut.addService(fieldIsin, fieldName, fieldDatetime);
		// weights: 5, 9, 1 // mean: 5 // deviation: 0+4+4
		// score: 10-(8*2)/3=4.66
		badCut.addService(fieldIssuer, fieldYield, fieldAmount);
		ServiceCut averageCut = new ServiceCut();
		// weights: 9, 9, 5, 5 // mean: 7 // deviation: 2+2+2+2
		// score: 10-(8*2)/4=6
		averageCut.addService(fieldIsin, fieldYield, fieldName, fieldIssuer);
		// weights: 1, 1 // score: 10
		averageCut.addService(fieldDatetime, fieldAmount);

		double score = couplingCriterionScoring.calculateScore(perfectCut, volatility, couplingContext);
		assertThat(score, is(10.0));
		score = couplingCriterionScoring.calculateScore(badCut, volatility, couplingContext);
		assertThat(score, closeTo(4.66, 0.01));
		score = couplingCriterionScoring.calculateScore(averageCut, volatility, couplingContext);
		assertThat(score, is(8.0));
	}

}
