package ch.hsr.servicestoolkit.repository;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

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
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
public class MonoCouplingInstanceRepositoryTest {

	@Autowired
	private ModelRepository modelRepository;
	@Autowired
	private DataFieldRepository dataFieldRepository;
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
		Long id = addModel(variant);
		//
		em.flush();
		List<MonoCouplingInstance> list = monoCouplingInstanceRepository.findByModel(id);
		assertThat(list, hasSize(1));
	}

	Long addModel(CouplingCriteriaVariant variant) {
		Model model = new Model();
		modelRepository.save(model);
		MonoCouplingInstance instance = variant.createInstance();
		monoCouplingInstanceRepository.save(instance);
		DataField dataField = addDataField(model, "field1");
		instance.addDataField(dataField);
		dataField = addDataField(model, "field2");
		instance.addDataField(dataField);
		return model.getId();
	}

	DataField addDataField(Model model, String name) {
		DataField dataField = new DataField();
		dataField.setName(name);
		model.addDataField(dataField);
		dataFieldRepository.save(dataField);
		return dataField;
	}

}
