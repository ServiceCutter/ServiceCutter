package ch.hsr.servicestoolkit.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.hsr.servicestoolkit.EngineServiceAppication;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.Model;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
public class DataFieldRepositoryTest {

	private static final String FIELD_2 = "field2";

	private static final String FIELD_1 = "field1";

	@Autowired
	DataFieldRepository dataRepo;

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

		DataField field1 = new DataField();
		field1.setName(FIELD_1);
		field1.setModel(model);
		model.addDataField(field1);

		DataField field2 = new DataField();
		field2.setName(FIELD_2);

		dataRepo.save(field1);
		dataRepo.save(field2);
	}

	@Test
	public void testFindByName() {
		Assert.assertNotNull(dataRepo.findByName(FIELD_1));
		Assert.assertNotNull(dataRepo.findByName(FIELD_2));
	}

	@Test
	public void testFindByNameAndModel() {
		Assert.assertNotNull(dataRepo.findByNameAndModel(FIELD_1, model));
	}

}
