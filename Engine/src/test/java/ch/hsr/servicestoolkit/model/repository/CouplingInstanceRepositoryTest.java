package ch.hsr.servicestoolkit.model.repository;

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
import ch.hsr.servicestoolkit.model.CouplingInstance;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.Nanoentity;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
public class CouplingInstanceRepositoryTest {

	@Autowired
	private ModelRepository modelRepository;
	@Autowired
	private CouplingInstanceRepository couplingInstanceRepository;
	@Autowired
	private CouplingCriterionCharacteristicRepository couplingCriteriaCharacteristicRepository;
	@Autowired
	private NanoentityRepository nanoentityRepository;
	@PersistenceContext
	EntityManager em;

	@Test
	@Transactional
	public void testFindByModel() {
		CouplingCriterionCharacteristic characteristic = new CouplingCriterionCharacteristic();
		couplingCriteriaCharacteristicRepository.save(characteristic);
		addModel(characteristic);
		Model model = addModel(characteristic);
		//
		em.flush();
		Set<CouplingInstance> list = couplingInstanceRepository.findByModel(model);
		assertThat(list, hasSize(1));
	}

	@Test
	@Transactional
	public void testFindByModelTESTdual() {
		CouplingCriterionCharacteristic characteristic = new CouplingCriterionCharacteristic();
		couplingCriteriaCharacteristicRepository.save(characteristic);
		addModel(characteristic);
		Model model = addModel(characteristic);
		//
		em.flush();
		Set<CouplingInstance> list = couplingInstanceRepository.findByModel(model);
		assertThat(list, hasSize(1));
	}

	// TODO verify test - still required?
	@Test
	@Transactional
	public void testDualCouplingPersistence() {
		CouplingCriterionCharacteristic characteristic = new CouplingCriterionCharacteristic();
		characteristic.setName("coupling");
		couplingCriteriaCharacteristicRepository.save(characteristic);
		Model model = new Model();
		modelRepository.save(model);

		Nanoentity nanoentity1 = createNanoentity(model, "nanoentity1");
		Nanoentity nanoentity2 = createNanoentity(model, "nanoentity2");
		Nanoentity nanoentity3 = createNanoentity(model, "nanoentity3");

		CouplingInstance dualInstance = new CouplingInstance(characteristic);
		model.addCouplingInstance(dualInstance);
		couplingInstanceRepository.save(dualInstance);
		dualInstance.addNanoentity(nanoentity1);
		dualInstance.addNanoentity(nanoentity2);
		dualInstance.addSecondNanoentity(nanoentity3);
		model.addCouplingInstance(dualInstance);
		//
		em.flush();
		em.clear();
		Set<CouplingInstance> list = couplingInstanceRepository.findByModel(model);

		assertThat(list, hasSize(1));
		CouplingInstance persistedInstance = list.iterator().next();
		assertThat(persistedInstance.getNanoentities(), hasSize(2));
		assertThat(persistedInstance.getSecondNanoentities(), hasSize(1));
		assertThat(persistedInstance.getSecondNanoentities().get(0).getId(), is(nanoentity3.getId()));

	}

	private Nanoentity createNanoentity(final Model model, final String name) {
		Nanoentity nanoentity = new Nanoentity(name);
		nanoentityRepository.save(nanoentity);
		nanoentity.setModel(model);
		model.addNanoentity(nanoentity);
		return nanoentity;
	}

	Model addModel(final CouplingCriterionCharacteristic characteristic) {
		Model model = new Model();
		modelRepository.save(model);
		CouplingInstance instance = new CouplingInstance(characteristic);
		couplingInstanceRepository.save(instance);
		model.addCouplingInstance(instance);
		return model;
	}

}
