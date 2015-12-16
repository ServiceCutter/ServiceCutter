package ch.hsr.servicecutter.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.hsr.servicecutter.EngineServiceAppication;
import ch.hsr.servicecutter.model.repository.ModelRepository;
import ch.hsr.servicecutter.model.repository.NanoentityRepository;
import ch.hsr.servicecutter.model.systemdata.Model;
import ch.hsr.servicecutter.model.systemdata.Nanoentity;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
public class NanoentityRepositoryTest {

	private static final String NANOENTITY_2 = "nanoentity2";

	private static final String NANOENTITY_1 = "nanoentity1";

	@Autowired
	NanoentityRepository dataRepo;

	@Autowired
	ModelRepository modelRepo;

	private Model model;

	@Before
	public void setUp() {
		model = new Model();
		model.setName("imported Thu Oct 15 09:28:12 CEST 2015");
		modelRepo.save(model);

		Model model2 = new Model();
		model2.setName("imported Thu Oct 15 09:28:11 CEST 2015");
		modelRepo.save(model2);

		Nanoentity nanoentity1 = new Nanoentity();
		nanoentity1.setName(NANOENTITY_1);
		model.addNanoentity(nanoentity1);

		Nanoentity nanoentity2 = new Nanoentity();
		nanoentity2.setName(NANOENTITY_2);

		dataRepo.save(nanoentity1);
		dataRepo.save(nanoentity2);
	}

	@Test
	public void testFindByName() {
		assertThat(dataRepo.findByNameAndModel(NANOENTITY_1, model), notNullValue());
		assertThat(dataRepo.findByNameAndModel(NANOENTITY_2, model), nullValue());
	}

}
