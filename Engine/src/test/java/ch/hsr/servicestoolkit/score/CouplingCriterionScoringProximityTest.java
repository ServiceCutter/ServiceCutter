package ch.hsr.servicestoolkit.score;

import static ch.hsr.servicestoolkit.score.TestDataHelper.createCouplingInstance;
import static ch.hsr.servicestoolkit.score.TestDataHelper.createCharacteristic;
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
	private Nanoentity fieldIsin;
	private Nanoentity fieldName;
	private Nanoentity fieldDatetime;
	private Nanoentity fieldAmount;
	private Nanoentity fieldIssuer;
	private Nanoentity fieldYield;
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
		// fields & model
		fieldIsin = createNanoentity("ISIN");
		fieldName = createNanoentity("Name");
		fieldDatetime = createNanoentity("Datetime");
		fieldAmount = createNanoentity("Amount");
		fieldIssuer = createNanoentity("Issuer");
		fieldYield = createNanoentity("Yield");
		model = new Model();
		model.addNanoentity(fieldIsin);
		model.addNanoentity(fieldName);
		model.addNanoentity(fieldDatetime);
		model.addNanoentity(fieldAmount);
		model.addNanoentity(fieldIssuer);
		model.addNanoentity(fieldYield);
		// coupling
		identityAndLifecycle = new CouplingCriterion();
		identityAndLifecycle.setType(CouplingType.COHESIVENESS);
		identityAndLifecycle.setName(CouplingCriterion.IDENTITY_LIFECYCLE);
		sameEntity = createCharacteristic(identityAndLifecycle, WEIGHT_ENTITY, "Same Entity");
		inheritance = createCharacteristic(identityAndLifecycle, WEIGHT_INHERITANCE, "Inheritance");
		// coupling instances
		entityStock = createCouplingInstance(sameEntity, fieldIsin, fieldName);
		entityPrice = createCouplingInstance(sameEntity, fieldDatetime, fieldAmount);
		entityBond = createCouplingInstance(sameEntity, fieldIssuer, fieldYield);
		inheritanceBondStock = createCouplingInstance(inheritance, new Nanoentity[] {fieldIsin, fieldName}, new Nanoentity[] {fieldIssuer, fieldYield});
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
		perfectCut.addService(fieldIsin, fieldName);
		perfectCut.addService(fieldDatetime, fieldAmount);
		perfectCut.addService(fieldIssuer, fieldYield);
		ServiceCut worstCut = new ServiceCut();
		worstCut.addService(fieldDatetime, fieldName);
		worstCut.addService(fieldIsin, fieldYield);
		worstCut.addService(fieldIssuer, fieldAmount);
		ServiceCut averageCut = new ServiceCut();
		averageCut.addService(fieldIsin, fieldName, fieldDatetime);
		averageCut.addService(fieldAmount);
		perfectCut.addService(fieldIssuer, fieldYield);

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
		perfectCut.addService(fieldIsin, fieldName, fieldIssuer, fieldYield);
		perfectCut.addService(fieldDatetime, fieldAmount);
		ServiceCut goodCut = new ServiceCut();
		goodCut.addService(fieldIsin, fieldName);
		// inheritance coupling is broken!
		goodCut.addService(fieldIssuer, fieldYield);
		goodCut.addService(fieldDatetime, fieldAmount);

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
