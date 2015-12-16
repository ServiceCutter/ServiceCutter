package ch.hsr.servicecutter.model.repository;

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

import ch.hsr.servicecutter.EngineServiceAppication;
import ch.hsr.servicecutter.model.criteria.CouplingCriterion;
import ch.hsr.servicecutter.model.criteria.CouplingCriterionCharacteristic;
import ch.hsr.servicecutter.model.repository.CouplingCriterionCharacteristicRepository;
import ch.hsr.servicecutter.model.repository.CouplingCriterionRepository;
import ch.hsr.servicecutter.model.repository.CouplingInstanceRepository;
import ch.hsr.servicecutter.model.repository.ModelRepository;
import ch.hsr.servicecutter.model.repository.NanoentityRepository;
import ch.hsr.servicecutter.model.systemdata.CouplingInstance;
import ch.hsr.servicecutter.model.systemdata.InstanceType;
import ch.hsr.servicecutter.model.systemdata.Model;
import ch.hsr.servicecutter.model.systemdata.Nanoentity;

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
	private CouplingCriterionRepository couplingCriterionRepository;
	@Autowired
	private NanoentityRepository nanoentityRepository;
	@PersistenceContext
	EntityManager em;

	@Test
	@Transactional
	public void testFindByModel() {
		CouplingCriterionCharacteristic characteristic = createCharacteristic();
		addModel(characteristic);
		Model model = addModel(characteristic);
		//
		em.flush();
		Set<CouplingInstance> list = couplingInstanceRepository.findByModel(model);
		assertThat(list, hasSize(1));
	}

	@Test
	@Transactional
	public void testDualCouplingPersistence() {
		CouplingCriterionCharacteristic characteristic = createCharacteristic();
		Model model = new Model();
		modelRepository.save(model);

		Nanoentity nanoentity1 = createNanoentity(model, "nanoentity1");
		Nanoentity nanoentity2 = createNanoentity(model, "nanoentity2");
		Nanoentity nanoentity3 = createNanoentity(model, "nanoentity3");

		CouplingInstance instance = new CouplingInstance(characteristic, InstanceType.CHARACTERISTIC);
		model.addCouplingInstance(instance);
		couplingInstanceRepository.save(instance);
		instance.addNanoentity(nanoentity1);
		instance.addNanoentity(nanoentity2);
		instance.addSecondNanoentity(nanoentity3);
		model.addCouplingInstance(instance);
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

	private CouplingCriterionCharacteristic createCharacteristic() {
		CouplingCriterionCharacteristic characteristic = new CouplingCriterionCharacteristic();
		characteristic.setName("coupling");
		CouplingCriterion criterion = new CouplingCriterion();
		couplingCriterionRepository.save(criterion);
		characteristic.setCouplingCriterion(criterion);
		couplingCriteriaCharacteristicRepository.save(characteristic);

		return characteristic;
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
		CouplingInstance instance = new CouplingInstance(characteristic, InstanceType.CHARACTERISTIC);
		couplingInstanceRepository.save(instance);
		model.addCouplingInstance(instance);
		return model;
	}

}
