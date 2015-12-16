package ch.hsr.servicecutter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.hsr.servicecutter.EngineService;
import ch.hsr.servicecutter.EngineServiceAppication;
import ch.hsr.servicecutter.model.usersystem.UserSystem;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
public class EngineServiceTest {

	@Autowired
	private EngineService service;

	@Test
	public void testCreateModel() {
		Long id = service.createUserSystem(new UserSystem(), "first").getId();
		UserSystem result = service.getSystem(id);
		assertEquals("first", result.getName());
	}

	@Test
	public void testCreateModelSameName() {
		UserSystem model = new UserSystem();
		model.setName("second");
		Long id = service.createUserSystem(model, "second").getId();
		UserSystem result = service.getSystem(id);
		assertEquals("second", result.getName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateModelDifferentName() {
		UserSystem model = new UserSystem();
		model.setName("whateverName");
		service.createUserSystem(model, "third");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateModelNoName() {
		UserSystem model = new UserSystem();
		service.createUserSystem(model, "");
	}

	@Test
	public void testCreateModelNoParamName() {
		UserSystem model = new UserSystem();
		model.setName("fourth");
		Long id = service.createUserSystem(model, "").getId();
		UserSystem result = service.getSystem(id);
		assertEquals("fourth", result.getName());
	}
}
