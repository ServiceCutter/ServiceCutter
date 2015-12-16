package ch.hsr.servicecutter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import ch.hsr.servicecutter.EngineServiceAppication;
import ch.hsr.servicecutter.model.userdata.UserSystem;
import ch.hsr.servicecutter.model.userdata.Nanoentity;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
@IntegrationTest("server.port=0")
@WebAppConfiguration
public class EngineServiceRestTest {

	@Value("${local.server.port}")
	private int port;
	private RestTemplate restTemplate = new TestRestTemplate();

	@Test
	public void createEmptyModel() {
		UserSystem model = new UserSystem();
		model.setName("testModel");
		HttpEntity<UserSystem> request = IntegrationTestHelper.createHttpRequestWithPostObj(model);
		ResponseEntity<UserSystem> requestResult = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/models", HttpMethod.POST, request, UserSystem.class);
		assertEquals(HttpStatus.OK, requestResult.getStatusCode());
		Long id = requestResult.getBody().getId();

		ResponseEntity<UserSystem> assertResult = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/models/" + id, HttpMethod.GET,
				IntegrationTestHelper.createEmptyHttpRequest(), UserSystem.class);
		assertEquals("testModel", assertResult.getBody().getName());
	}

	@Test
	public void createModelWithEntities() {
		int before = countModels();
		createModelOnApi();
		// test whether created model is visible
		assertEquals(before + 1, countModels());
	}

	private Long createModelOnApi() {
		UserSystem model = createModel();
		HttpEntity<UserSystem> request = IntegrationTestHelper.createHttpRequestWithPostObj(model);
		ResponseEntity<UserSystem> entity = restTemplate.exchange("http://localhost:" + this.port + "/engine/models/", HttpMethod.POST, request, UserSystem.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		return entity.getBody().getId();
	}

	private int countModels() {
		@SuppressWarnings("rawtypes")
		ResponseEntity<List> response = restTemplate.getForEntity("http://localhost:" + this.port + "/engine/models", List.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		int models = response.getBody().size();
		return models;
	}

	private UserSystem createModel() {
		UserSystem model = new UserSystem();
		model.setName("firstModel");
		Nanoentity nanoentity1 = new Nanoentity();
		nanoentity1.setName("firstNanoentity");
		Nanoentity nanoentity2 = new Nanoentity();
		nanoentity2.setName("secondNanoentity");
		model.addNanoentity(nanoentity1);
		model.addNanoentity(nanoentity2);
		return model;
	}

}
