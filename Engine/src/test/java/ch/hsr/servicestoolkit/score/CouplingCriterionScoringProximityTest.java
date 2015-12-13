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

public class CouplingCriterionScoringProximityTest {

	private static final int WEIGHT_INHERITANCE = 2;
	private static final int WEIGHT_ENTITY = 5;
	private Nanoentity nanoentityIsin;
	private Nanoentity nanoentityName;
	private Nanoentity nanoentityDatetime;
	private Nanoentity nanoentityAmount;
	private Nanoentity nanoentityIssuer;
	private Nanoentity nanoentityYield;
	private Model model;
	private CouplingContext couplingContext;
	private CouplingCriterion identityAndLifecycle;
	private CouplingCriterionCharacteristic sameEntity;
	private CouplingCriterionCharacteristic inheritance;
	private CouplingCriterionScoring couplingCriterionScoring = new CouplingCriterionScoring();
	private CouplingInstance entityStock;
	private CouplingInstance entityPrice;
	private CouplingInstance entityBond;
	private CouplingInstance inheritanceBondStock;
	private Long id;

	/**
	 * Stock (ISIN, Name), Bond (Issuer, Yield), Price (Datetime, Amount)
	 */
	@Before
	public void setup() {
		id = 0l;
		// nanoentities & model
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
		identityAndLifecycle = new CouplingCriterion();
		identityAndLifecycle.setType(CouplingType.COHESIVENESS);
		identityAndLifecycle.setName(CouplingCriterion.IDENTITY_LIFECYCLE);
		sameEntity = createCharacteristic(identityAndLifecycle, WEIGHT_ENTITY, "Same Entity");
		inheritance = createCharacteristic(identityAndLifecycle, WEIGHT_INHERITANCE, "Inheritance");
		// coupling instances
		entityStock = createCouplingInstance(sameEntity, nanoentityIsin, nanoentityName);
		entityPrice = createCouplingInstance(sameEntity, nanoentityDatetime, nanoentityAmount);
		entityBond = createCouplingInstance(sameEntity, nanoentityIssuer, nanoentityYield);
		inheritanceBondStock = createCouplingInstance(inheritance, new Nanoentity[] {nanoentityIsin, nanoentityName}, new Nanoentity[] {nanoentityIssuer, nanoentityYield});
		// context
		couplingContext = new CouplingContext(model, Arrays.asList(entityStock, entityPrice, entityBond, inheritanceBondStock));

	}

	private Nanoentity createNanoentity(final String name) {
		Nanoentity nanoentity = new Nanoentity(name);
		nanoentity.setId(id++);
		return nanoentity;
	}

	@Test
	public void sameEntityCoupling() {
		// only consider "same entity" coupling
		couplingContext = new CouplingContext(model, Arrays.asList(entityStock, entityPrice, entityBond));

		ServiceCut perfectCut = new ServiceCut();
		perfectCut.addService(nanoentityIsin, nanoentityName);
		perfectCut.addService(nanoentityDatetime, nanoentityAmount);
		perfectCut.addService(nanoentityIssuer, nanoentityYield);
		ServiceCut worstCut = new ServiceCut();
		worstCut.addService(nanoentityDatetime, nanoentityName);
		worstCut.addService(nanoentityIsin, nanoentityYield);
		worstCut.addService(nanoentityIssuer, nanoentityAmount);
		ServiceCut averageCut = new ServiceCut();
		averageCut.addService(nanoentityIsin, nanoentityName, nanoentityDatetime);
		averageCut.addService(nanoentityAmount);
		perfectCut.addService(nanoentityIssuer, nanoentityYield);

		double score = couplingCriterionScoring.calculateScore(perfectCut, identityAndLifecycle, couplingContext);
		assertThat(score, is(10.0));
		score = couplingCriterionScoring.calculateScore(worstCut, identityAndLifecycle, couplingContext);
		assertThat(score, is(0.0));
		score = couplingCriterionScoring.calculateScore(averageCut, identityAndLifecycle, couplingContext);
		assertThat(score, closeTo(6.66, 0.01));
	}

	@Test
	public void twoCharacteristics() {
		ServiceCut perfectCut = new ServiceCut();
		perfectCut.addService(nanoentityIsin, nanoentityName, nanoentityIssuer, nanoentityYield);
		perfectCut.addService(nanoentityDatetime, nanoentityAmount);
		ServiceCut goodCut = new ServiceCut();
		goodCut.addService(nanoentityIsin, nanoentityName);
		// inheritance coupling is broken!
		goodCut.addService(nanoentityIssuer, nanoentityYield);
		goodCut.addService(nanoentityDatetime, nanoentityAmount);

		double score = couplingCriterionScoring.calculateScore(perfectCut, identityAndLifecycle, couplingContext);
		assertThat(score, is(10.0));
		score = couplingCriterionScoring.calculateScore(goodCut, identityAndLifecycle, couplingContext);
		// Same Entity / Stock - match - 10*5
		// Same Entity / Price - match - 10*5
		// Same Entity / Bond - match - 10*5
		// Inheritance / Bond-Stock - no match - 0*2
		// result: (50+50+50+0) / (5+5+5+2)
		assertThat(score, closeTo(8.82, 0.01));
	}

}
