package ch.hsr.servicestoolkit.score;

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

public class CouplingCriterionScoringTest {

	private DataField fieldIsin;
	private DataField fieldName;
	private DataField fieldDatetime;
	private DataField fieldAmount;
	private CouplingCriterion identityAndLifecycle;
	private CouplingCriteriaVariant sameEntity;
	private CouplingCriterionScoring couplingCriterionScoring = new CouplingCriterionScoring();
	private Model model;
	private MonoCouplingInstance entityStock;
	private MonoCouplingInstance entityPrice;
	private CouplingContext couplingContext;

	/**
	 * Stock (ISIN, Name), Price (Datetime, Amount)
	 */
	@Before
	public void setup() {
		// fields & model
		fieldIsin = new DataField("ISIN");
		fieldName = new DataField("Name");
		fieldDatetime = new DataField("Datetime");
		fieldAmount = new DataField("Amount");
		model = new Model();
		model.addDataField(fieldIsin);
		model.addDataField(fieldName);
		model.addDataField(fieldDatetime);
		model.addDataField(fieldAmount);
		// coupling
		identityAndLifecycle = new CouplingCriterion();
		identityAndLifecycle.setType(CouplingType.PROXIMITY);
		identityAndLifecycle.setName(CouplingCriterion.IDENTITY_LIFECYCLE);
		sameEntity = createVariant(identityAndLifecycle, 5);
		// coupling instances
		entityStock = createCouplingInstance(sameEntity, fieldIsin, fieldName);
		entityPrice = createCouplingInstance(sameEntity, fieldDatetime, fieldAmount);
		// context
		couplingContext = new CouplingContext(model, Arrays.asList(entityStock, entityPrice));

	}

	@Test
	public void sameEntityCoupling() {
		ServiceCut perfectCut = new ServiceCut();
		perfectCut.addService(fieldIsin, fieldName);
		perfectCut.addService(fieldDatetime, fieldAmount);
		ServiceCut worstCut = new ServiceCut();
		worstCut.addService(fieldDatetime, fieldName);
		worstCut.addService(fieldIsin, fieldAmount);
		ServiceCut averageCut = new ServiceCut();
		averageCut.addService(fieldIsin, fieldName, fieldDatetime);
		averageCut.addService(fieldAmount);

		double score = couplingCriterionScoring.calculateScore(perfectCut, identityAndLifecycle, couplingContext);
		assertThat(score, is(10.0));
		score = couplingCriterionScoring.calculateScore(worstCut, identityAndLifecycle, couplingContext);
		assertThat(score, is(0.0));
		score = couplingCriterionScoring.calculateScore(averageCut, identityAndLifecycle, couplingContext);
		assertThat(score, is(5.0));
	}

	private MonoCouplingInstance createCouplingInstance(CouplingCriteriaVariant variant, DataField... fields) {
		MonoCouplingInstance result = new MonoCouplingInstance();
		for (DataField dataField : fields) {
			result.addDataField(dataField);
		}
		result.setCouplingCriteriaVariant(variant);
		return result;
	}

	CouplingCriteriaVariant createVariant(CouplingCriterion criterion, int weight) {
		CouplingCriteriaVariant variant = new CouplingCriteriaVariant();
		variant.setCouplingCriterion(criterion);
		variant.setWeight(weight);
		return variant;
	}

}
