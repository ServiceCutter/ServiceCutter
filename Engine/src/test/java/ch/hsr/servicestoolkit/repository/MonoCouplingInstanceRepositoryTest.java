package ch.hsr.servicestoolkit.repository;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.hsr.servicestoolkit.EngineServiceAppication;
import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.DualCouplingInstance;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
public class MonoCouplingInstanceRepositoryTest {

	@Autowired
	private ModelRepository modelRepository;
	@Autowired
	private MonoCouplingInstanceRepository monoCouplingInstanceRepository;
	@Autowired
	private CouplingCriteriaVariantRepository couplingCriteriaVariantRepository;
	@Autowired
	private DataFieldRepository dataFieldRepository;
	@PersistenceContext
	EntityManager em;

	@Test
	@Transactional
	public void testFindByModel() {
		CouplingCriteriaVariant variant = new CouplingCriteriaVariant();
		couplingCriteriaVariantRepository.save(variant);
		addModel(variant);
		Model model = addModel(variant);
		//
		em.flush();
		Set<MonoCouplingInstance> list = monoCouplingInstanceRepository.findByModel(model);
		assertThat(list, hasSize(1));
	}

	@Test
	@Transactional
	public void testFindByModelTESTdual() {
		CouplingCriteriaVariant variant = new CouplingCriteriaVariant();
		couplingCriteriaVariantRepository.save(variant);
		addModel(variant);
		Model model = addModel(variant);
		//
		em.flush();
		Set<MonoCouplingInstance> list = monoCouplingInstanceRepository.findByModel(model);
		assertThat(list, hasSize(1));
	}

	@Test
	@Transactional
	public void testDualCouplingPersistence() {
		CouplingCriteriaVariant variant = new CouplingCriteriaVariant();
		variant.setMonoCoupling(false);
		variant.setName("monoCoupling");
		couplingCriteriaVariantRepository.save(variant);
		Model model = new Model();
		modelRepository.save(model);

		DataField dataField1 = createDataField(model, "field1");
		DataField dataField2 = createDataField(model, "field2");
		DataField dataField3 = createDataField(model, "field3");

		DualCouplingInstance dualInstance = (DualCouplingInstance) variant.createInstance();
		monoCouplingInstanceRepository.save(dualInstance);
		dualInstance.addDataField(dataField1);
		dualInstance.addDataField(dataField2);
		dualInstance.addSecondDataField(dataField3);
		model.addCouplingInstance(dualInstance);
		//
		em.flush();
		em.clear();
		Set<MonoCouplingInstance> list = monoCouplingInstanceRepository.findByModel(model);

		assertThat(list, hasSize(1));
		DualCouplingInstance persistedInstance = (DualCouplingInstance) list.iterator().next();
		assertThat(persistedInstance.getDataFields(), hasSize(2));
		assertThat(persistedInstance.getSecondDataFields(), hasSize(1));
		assertThat(persistedInstance.getSecondDataFields().get(0).getId(), is(dataField3.getId()));

	}

	private DataField createDataField(final Model model, final String name) {
		DataField dataField = new DataField(name);
		dataFieldRepository.save(dataField);
		dataField.setModel(model);
		model.addDataField(dataField);
		return dataField;
	}

	Model addModel(final CouplingCriteriaVariant variant) {
		Model model = new Model();
		modelRepository.save(model);
		MonoCouplingInstance instance = variant.createInstance();
		monoCouplingInstanceRepository.save(instance);
		model.addCouplingInstance(instance);
		return model;
	}

}
