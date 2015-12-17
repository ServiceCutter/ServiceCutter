package ch.hsr.servicecutter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.hsr.servicecutter.model.usersystem.UserSystem;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
public class EngineServiceTest {

	@Autowired
	private EngineService service;

	@Test
	public void testCreateModel() {
		Long id = service.createUserSystem("first").getId();
		UserSystem result = service.getSystem(id);
		assertEquals("first", result.getName());
	}

	@Test(expected = Exception.class)
	public void testCreateModelNoName() {
		service.createUserSystem("");
	}

}
