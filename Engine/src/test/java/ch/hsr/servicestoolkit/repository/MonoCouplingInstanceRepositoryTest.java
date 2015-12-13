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
import ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic;
import ch.hsr.servicestoolkit.model.NanoEntity;
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
		CouplingCriterionCharacteristic variant = new CouplingCriterionCharacteristic();
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
		CouplingCriterionCharacteristic variant = new CouplingCriterionCharacteristic();
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
		CouplingCriterionCharacteristic variant = new CouplingCriterionCharacteristic();
		variant.setMonoCoupling(false);
		variant.setName("monoCoupling");
		couplingCriteriaVariantRepository.save(variant);
		Model model = new Model();
		modelRepository.save(model);

		NanoEntity dataField1 = createDataField(model, "field1");
		NanoEntity dataField2 = createDataField(model, "field2");
		NanoEntity dataField3 = createDataField(model, "field3");

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

	private NanoEntity createDataField(final Model model, final String name) {
		NanoEntity dataField = new NanoEntity(name);
		dataFieldRepository.save(dataField);
		dataField.setModel(model);
		model.addDataField(dataField);
		return dataField;
	}

	Model addModel(final CouplingCriterionCharacteristic variant) {
		Model model = new Model();
		modelRepository.save(model);
		MonoCouplingInstance instance = variant.createInstance();
		monoCouplingInstanceRepository.save(instance);
		model.addCouplingInstance(instance);
		return model;
	}

}
