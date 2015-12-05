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
import ch.hsr.servicestoolkit.model.DualCouplingInstance;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.model.NanoEntity;
import ch.hsr.servicestoolkit.model.service.ServiceCut;
import ch.hsr.servicestoolkit.score.cuts.CouplingContext;
import ch.hsr.servicestoolkit.score.cuts.CouplingCriterionScoring;

public class CouplingCriterionScoringProximityTest {

	private static final int WEIGHT_INHERITANCE = 2;
	private static final int WEIGHT_ENTITY = 5;
	private NanoEntity fieldIsin;
	private NanoEntity fieldName;
	private NanoEntity fieldDatetime;
	private NanoEntity fieldAmount;
	private NanoEntity fieldIssuer;
	private NanoEntity fieldYield;
	private Model model;
	private CouplingContext couplingContext;
	private CouplingCriterion identityAndLifecycle;
	private CouplingCriteriaVariant sameEntity;
	private CouplingCriteriaVariant inheritance;
	private CouplingCriterionScoring couplingCriterionScoring = new CouplingCriterionScoring();
	private MonoCouplingInstance entityStock;
	private MonoCouplingInstance entityPrice;
	private MonoCouplingInstance entityBond;
	private DualCouplingInstance inheritanceBondStock;
	private Long id;

	/**
	 * Stock (ISIN, Name), Bond (Issuer, Yield), Price (Datetime, Amount)
	 */
	@Before
	public void setup() {
		id = 0l;
		// fields & model
		fieldIsin = createDataField("ISIN");
		fieldName = createDataField("Name");
		fieldDatetime = createDataField("Datetime");
		fieldAmount = createDataField("Amount");
		fieldIssuer = createDataField("Issuer");
		fieldYield = createDataField("Yield");
		model = new Model();
		model.addDataField(fieldIsin);
		model.addDataField(fieldName);
		model.addDataField(fieldDatetime);
		model.addDataField(fieldAmount);
		model.addDataField(fieldIssuer);
		model.addDataField(fieldYield);
		// coupling
		identityAndLifecycle = new CouplingCriterion();
		identityAndLifecycle.setType(CouplingType.COHESIVENESS);
		identityAndLifecycle.setName(CouplingCriterion.IDENTITY_LIFECYCLE);
		sameEntity = createVariant(identityAndLifecycle, WEIGHT_ENTITY, "Same Entity");
		inheritance = createVariant(identityAndLifecycle, WEIGHT_INHERITANCE, "Inheritance");
		// coupling instances
		entityStock = createCouplingInstance(sameEntity, fieldIsin, fieldName);
		entityPrice = createCouplingInstance(sameEntity, fieldDatetime, fieldAmount);
		entityBond = createCouplingInstance(sameEntity, fieldIssuer, fieldYield);
		inheritanceBondStock = createCouplingInstance(inheritance, new NanoEntity[] { fieldIsin, fieldName }, new NanoEntity[] { fieldIssuer, fieldYield });
		// context
		couplingContext = new CouplingContext(model, Arrays.asList(entityStock, entityPrice, entityBond, inheritanceBondStock));

	}

	private NanoEntity createDataField(final String name) {
		NanoEntity dataField = new NanoEntity(name);
		dataField.setId(id++);
		return dataField;
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
	public void twoVariants() {
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
