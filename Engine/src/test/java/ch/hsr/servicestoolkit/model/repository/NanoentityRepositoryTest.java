package ch.hsr.servicestoolkit.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.hsr.servicestoolkit.EngineServiceAppication;
import ch.hsr.servicestoolkit.model.Nanoentity;
import ch.hsr.servicestoolkit.model.repository.NanoentityRepository;
import ch.hsr.servicestoolkit.model.repository.ModelRepository;
import ch.hsr.servicestoolkit.model.Model;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
public class NanoentityRepositoryTest {

	private static final String FIELD_2 = "field2";

	private static final String FIELD_1 = "field1";

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

		Nanoentity field1 = new Nanoentity();
		field1.setName(FIELD_1);
		model.addNanoentity(field1);

		Nanoentity field2 = new Nanoentity();
		field2.setName(FIELD_2);

		dataRepo.save(field1);
		dataRepo.save(field2);
	}

	@Test
	public void testFindByName() {
		assertThat(dataRepo.findByNameAndModel(FIELD_1, model), notNullValue());
		assertThat(dataRepo.findByNameAndModel(FIELD_2, model), nullValue());
	}

}
