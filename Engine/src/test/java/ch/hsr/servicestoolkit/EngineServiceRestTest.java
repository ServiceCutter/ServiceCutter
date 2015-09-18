package ch.hsr.servicestoolkit;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.EngineState;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.QualityAttribute;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
@IntegrationTest("server.port=0")
@WebAppConfiguration
public class EngineServiceRestTest {

	@Value("${local.server.port}")
	private int port;
	private RestTemplate restTemplate = new TestRestTemplate();

	@Before
	public void setUp() {
		restTemplate.setMessageConverters(
				Arrays.asList(new MappingJackson2HttpMessageConverter(), new StringHttpMessageConverter()));
	}

	@Test
	public void statusCheck() {
		ResponseEntity<EngineState> entity = this.restTemplate.getForEntity("http://localhost:" + this.port + "/engine",
				EngineState.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		assertEquals("Engine is up and running.", entity.getBody().getDescription());
	}

	@Test
	public void createEmptyModel() {

		HttpEntity<String[]> request = createJsonProvidingHttpRequest(null);
		ResponseEntity<String> requestResult = this.restTemplate.exchange(
				"http://localhost:" + this.port + "/engine/models/testModel", HttpMethod.PUT, request, String.class);
		assertEquals(HttpStatus.NO_CONTENT, requestResult.getStatusCode());

		ResponseEntity<Model> assertResult = this.restTemplate.exchange(
				"http://localhost:" + this.port + "/engine/models/testModel", HttpMethod.GET, createEmptyHttpRequest(),
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
	public void addQualityAttributesToExistingModel() {
		String model = createModelOnApi();

		List<QualityAttribute> input = createQualityAttributes();

		HttpEntity<List<QualityAttribute>> request = createJsonProvidingHttpRequest(input);
		// write quality attributes
		ResponseEntity<String> entity = this.restTemplate.exchange(
				"http://localhost:" + this.port + "/engine/models/" + model + "/qualityattributes", HttpMethod.PUT,
				request, String.class);
		assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());

		// read written quality attributes
		ResponseEntity<Set<QualityAttribute>> assertResult = this.restTemplate.exchange(
				"http://localhost:" + this.port + "/engine/models/" + model + "/qualityattributes", HttpMethod.GET,
				createEmptyHttpRequest(), new ParameterizedTypeReference<Set<QualityAttribute>>() {
				});
		assertEquals(1, assertResult.getBody().size());

	}

	private List<QualityAttribute> createQualityAttributes() {
		QualityAttribute attr1 = new QualityAttribute();
		DataField dataField = new DataField();
		dataField.setName("firstField");
		DataField dataField2 = new DataField();
		dataField2.setName("secondField");

		attr1.getDataFields().add(dataField);
		attr1.getDataFields().add(dataField2);

		List<QualityAttribute> input = new ArrayList<>();
		input.add(attr1);
		return input;
	}

	private String createModelOnApi() {
		Model model = createModel();
		HttpEntity<Model> request = createJsonProvidingHttpRequest(model);
		ResponseEntity<String> entity = this.restTemplate.exchange(
				"http://localhost:" + this.port + "/engine/models/" + model.getName(), HttpMethod.PUT, request,
				String.class);
		assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
		return model.getName();
	}

	private int countModels() {
		@SuppressWarnings("rawtypes")
		ResponseEntity<List> response = restTemplate.getForEntity("http://localhost:" + this.port + "/engine/models",
				List.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		int models = response.getBody().size();
		return models;
	}

	private <T> HttpEntity<T> createJsonProvidingHttpRequest(T model) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<T>(model, headers);
	}

	private HttpEntity<Object> createEmptyHttpRequest() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<Object>(headers);
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
