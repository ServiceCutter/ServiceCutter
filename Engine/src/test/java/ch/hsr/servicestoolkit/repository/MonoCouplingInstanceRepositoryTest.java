package ch.hsr.servicestoolkit.repository;

import static org.hamcrest.Matchers.hasSize;
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

	Model addModel(final CouplingCriteriaVariant variant) {
		Model model = new Model();
		modelRepository.save(model);
		MonoCouplingInstance instance = variant.createInstance();
		monoCouplingInstanceRepository.save(instance);
		model.addCouplingInstance(instance);
		return model;
	}

}
