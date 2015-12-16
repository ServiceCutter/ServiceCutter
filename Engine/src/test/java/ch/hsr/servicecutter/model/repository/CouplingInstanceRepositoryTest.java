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
import ch.hsr.servicecutter.model.usersystem.CouplingInstance;
import ch.hsr.servicecutter.model.usersystem.InstanceType;
import ch.hsr.servicecutter.model.usersystem.Nanoentity;
import ch.hsr.servicecutter.model.usersystem.UserSystem;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
public class CouplingInstanceRepositoryTest {

	@Autowired
	private UserSystemRepository userSystemRepository;
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
	public void testFindBySystem() {
		CouplingCriterionCharacteristic characteristic = createCharacteristic();
		addSystem(characteristic);
		UserSystem userSystem = addSystem(characteristic);
		//
		em.flush();
		Set<CouplingInstance> list = couplingInstanceRepository.findByUserSystem(userSystem);
		assertThat(list, hasSize(1));
	}

	@Test
	@Transactional
	public void testDualCouplingPersistence() {
		CouplingCriterionCharacteristic characteristic = createCharacteristic();
		UserSystem userSystem = new UserSystem();
		userSystemRepository.save(userSystem);

		Nanoentity nanoentity1 = createNanoentity(userSystem, "nanoentity1");
		Nanoentity nanoentity2 = createNanoentity(userSystem, "nanoentity2");
		Nanoentity nanoentity3 = createNanoentity(userSystem, "nanoentity3");

		CouplingInstance instance = new CouplingInstance(characteristic, InstanceType.CHARACTERISTIC);
		userSystem.addCouplingInstance(instance);
		couplingInstanceRepository.save(instance);
		instance.addNanoentity(nanoentity1);
		instance.addNanoentity(nanoentity2);
		instance.addSecondNanoentity(nanoentity3);
		userSystem.addCouplingInstance(instance);
		//
		em.flush();
		em.clear();
		Set<CouplingInstance> list = couplingInstanceRepository.findByUserSystem(userSystem);

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

	private Nanoentity createNanoentity(final UserSystem userSystem, final String name) {
		Nanoentity nanoentity = new Nanoentity(name);
		nanoentityRepository.save(nanoentity);
		nanoentity.setUserSystem(userSystem);
		userSystem.addNanoentity(nanoentity);
		return nanoentity;
	}

	UserSystem addSystem(final CouplingCriterionCharacteristic characteristic) {
		UserSystem userSystem = new UserSystem();
		userSystemRepository.save(userSystem);
		CouplingInstance instance = new CouplingInstance(characteristic, InstanceType.CHARACTERISTIC);
		couplingInstanceRepository.save(instance);
		userSystem.addCouplingInstance(instance);
		return userSystem;
	}

}
