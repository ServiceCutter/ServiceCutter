package ch.hsr.servicestoolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CriterionType;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.EngineState;
import ch.hsr.servicestoolkit.model.Model;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
@IntegrationTest("server.port=0")
@WebAppConfiguration
public class EngineServiceRestTest {

	@Value("${local.server.port}")
	private int port;
	private RestTemplate restTemplate = new TestRestTemplate();

	@Test
	public void statusCheck() {
		ResponseEntity<EngineState> entity = this.restTemplate.getForEntity("http://localhost:" + this.port + "/engine", EngineState.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		assertEquals("Engine is up and running.", entity.getBody().getDescription());
	}

	@Test
	public void createEmptyModel() {
		Model model = new Model();
		model.setName("testModel");
		HttpEntity<Model> request = IntegrationTestHelper.createHttpRequestWithPostObj(model);
		ResponseEntity<Model> requestResult = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/models", HttpMethod.POST, request, Model.class);
		assertEquals(HttpStatus.OK, requestResult.getStatusCode());
		Long id = requestResult.getBody().getId();

		ResponseEntity<Model> assertResult = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/models/" + id, HttpMethod.GET, IntegrationTestHelper.createEmptyHttpRequest(),
				Model.class);
		assertEquals("testModel", assertResult.getBody().getName());
	}

	@Test
	public void createModelWithEntities() {
		int before = countModels();
		createModelOnApi();
		// test whether created model is visible
		assertEquals(before + 1, countModels());
	}

	@Test
	public void addCriterionToExistingModel() {
		Long modelId = createModelOnApi();

		List<CouplingCriterion> input = createCouplingCriteria();

		HttpEntity<List<CouplingCriterion>> request = IntegrationTestHelper.createHttpRequestWithPostObj(input);
		// write criteria
		ResponseEntity<String> entity = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/models/" + modelId + "/couplingcriteria", HttpMethod.PUT, request, String.class);
		assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());

		// read written criteria
		ResponseEntity<Set<CouplingCriterion>> assertResult = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/models/" + modelId + "/couplingcriteria", HttpMethod.GET,
				IntegrationTestHelper.createEmptyHttpRequest(), new ParameterizedTypeReference<Set<CouplingCriterion>>() {
				});
		assertEquals(1, assertResult.getBody().size());
		for (CouplingCriterion criterion : assertResult.getBody()) {
			assertNotNull(criterion.getCriterionType());
			assertNotNull(criterion.getId());
		}

	}

	private List<CouplingCriterion> createCouplingCriteria() {
		CouplingCriterion criterion = new CouplingCriterion();
		criterion.setCriterionType(CriterionType.AGGREGATED_ENTITY);
		DataField dataField = new DataField();
		dataField.setName("firstField");
		DataField dataField2 = new DataField();
		dataField2.setName("secondField");

		criterion.getDataFields().add(dataField);
		criterion.getDataFields().add(dataField2);

		List<CouplingCriterion> input = new ArrayList<>();
		input.add(criterion);
		return input;
	}

	private Long createModelOnApi() {
		Model model = createModel();
		HttpEntity<Model> request = IntegrationTestHelper.createHttpRequestWithPostObj(model);
		ResponseEntity<Model> entity = restTemplate.exchange("http://localhost:" + this.port + "/engine/models/", HttpMethod.POST, request, Model.class);
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

	private Model createModel() {
		Model model = new Model();
		model.setName("firstModel");
		DataField field1 = new DataField();
		field1.setName("firstField");
		DataField field2 = new DataField();
		field2.setName("secondField");
		model.getDataFields().add(field1);
		model.getDataFields().add(field2);
		return model;
	}

}
