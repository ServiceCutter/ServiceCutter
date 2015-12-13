package ch.hsr.servicestoolkit.score;

import static ch.hsr.servicestoolkit.score.TestDataHelper.createCharacteristic;
import static ch.hsr.servicestoolkit.score.TestDataHelper.createCouplingInstance;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic;
import ch.hsr.servicestoolkit.model.CouplingInstance;
import ch.hsr.servicestoolkit.model.CouplingType;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.Nanoentity;
import ch.hsr.servicestoolkit.model.service.ServiceCut;
import ch.hsr.servicestoolkit.score.cuts.CouplingContext;
import ch.hsr.servicestoolkit.score.cuts.CouplingCriterionScoring;

public class CouplingCriterionScoringDistanceTest {

	private Nanoentity nanoentityIsin;
	private Nanoentity nanoentityName;
	private Nanoentity nanoentityDatetime;
	private Nanoentity nanoentityAmount;
	private Nanoentity nanoentityIssuer;
	private Nanoentity nanoentityYield;
	private Model model;
	private CouplingContext couplingContext;
	private CouplingCriterion volatility;
	private CouplingCriterionCharacteristic often;
	private CouplingCriterionCharacteristic regularly;
	private CouplingCriterionCharacteristic rarely;
	private CouplingCriterionScoring couplingCriterionScoring = new CouplingCriterionScoring();
	private CouplingInstance rarelyCoupling;
	private CouplingInstance oftenCoupling;
	private CouplingInstance regularlyCoupling;
	private Long id;

	/**
	 * Stock (ISIN, Name), Bond (Issuer, Yield), Price (Datetime, Amount)
	 */
	@Before
	public void setup() {
		// nanoentities & model
		id = 0l;
		nanoentityIsin = createNanoentity("ISIN");
		nanoentityName = createNanoentity("Name");
		nanoentityDatetime = createNanoentity("Datetime");
		nanoentityAmount = createNanoentity("Amount");
		nanoentityIssuer = createNanoentity("Issuer");
		nanoentityYield = createNanoentity("Yield");
		model = new Model();
		model.addNanoentity(nanoentityIsin);
		model.addNanoentity(nanoentityName);
		model.addNanoentity(nanoentityDatetime);
		model.addNanoentity(nanoentityAmount);
		model.addNanoentity(nanoentityIssuer);
		model.addNanoentity(nanoentityYield);
		// coupling
		volatility = new CouplingCriterion();
		volatility.setType(CouplingType.COMPATIBILITY);
		volatility.setName(CouplingCriterion.VOLATILITY);
		often = createCharacteristic(volatility, 1, "Often");
		regularly = createCharacteristic(volatility, 5, "Regularly");
		rarely = createCharacteristic(volatility, 9, "Rarely");
		// coupling instances
		rarelyCoupling = createCouplingInstance(rarely, nanoentityIsin, nanoentityYield); // 9
		regularlyCoupling = createCouplingInstance(regularly, nanoentityName, nanoentityIssuer); // 5
		oftenCoupling = createCouplingInstance(often, nanoentityDatetime, nanoentityAmount); // 1
		// context
		couplingContext = new CouplingContext(model, Arrays.asList(rarelyCoupling, regularlyCoupling, oftenCoupling));
	}

	private Nanoentity createNanoentity(final String name) {
		Nanoentity nanoentity = new Nanoentity(name);
		nanoentity.setId(id++);
		return nanoentity;
	}

	@Test
	public void volatilityCoupling() {
		ServiceCut perfectCut = new ServiceCut();
		perfectCut.addService(nanoentityIsin, nanoentityYield);
		perfectCut.addService(nanoentityName, nanoentityIssuer);
		perfectCut.addService(nanoentityDatetime, nanoentityAmount);
		ServiceCut badCut = new ServiceCut();
		// weights: 9, 5, 1 // mean: 5 // deviation: 4+0+4
		// score: 10-(8*2)/3=4.66
		badCut.addService(nanoentityIsin, nanoentityName, nanoentityDatetime);
		// weights: 5, 9, 1 // mean: 5 // deviation: 0+4+4
		// score: 10-(8*2)/3=4.66
		badCut.addService(nanoentityIssuer, nanoentityYield, nanoentityAmount);
		ServiceCut averageCut = new ServiceCut();
		// weights: 9, 9, 5, 5 // mean: 7 // deviation: 2+2+2+2
		// score: 10-(8*2)/4=6
		averageCut.addService(nanoentityIsin, nanoentityYield, nanoentityName, nanoentityIssuer);
		// weights: 1, 1 // score: 10
		averageCut.addService(nanoentityDatetime, nanoentityAmount);

		double score = couplingCriterionScoring.calculateScore(perfectCut, volatility, couplingContext);
		assertThat(score, is(10.0));
		score = couplingCriterionScoring.calculateScore(badCut, volatility, couplingContext);
		assertThat(score, closeTo(4.66, 0.01));
		score = couplingCriterionScoring.calculateScore(averageCut, volatility, couplingContext);
		assertThat(score, is(8.0));
	}
}
