package ch.hsr.servicestoolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import ch.hsr.servicestoolkit.importer.api.CohesiveGroups;
import ch.hsr.servicestoolkit.importer.api.DistanceCharacteristic;
import ch.hsr.servicestoolkit.importer.api.DomainModel;
import ch.hsr.servicestoolkit.importer.api.UseCase;
import ch.hsr.servicestoolkit.solver.SolverConfiguration;
import ch.hsr.servicestoolkit.solver.SolverResult;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
@IntegrationTest("server.port=0")
@WebAppConfiguration
public class DDDSampleRestTest {

	private static final String DDD_EXAMPLE_FILE = "ddd_1_model.json";
	private static final String DDD_USE_CASES_FILE = "ddd_2_use_cases.json";
	private static final String DDD_CHARACTERISTICS_FILE = "ddd_3_characteristics.json";
	private static final String DDD_RESPONSIBILITES_FILE = "ddd_4_cohesive_groups_responsibility.json";
	@Value("${local.server.port}")
	private int port;
	private RestTemplate restTemplate = new TestRestTemplate();
	private Logger log = LoggerFactory.getLogger(DDDSampleRestTest.class);

	@Test
	public void dddExample() throws UnsupportedEncodingException, URISyntaxException, IOException {
		Integer modelId = createModelOnApi();

		loadBusinessTransactionOnModel(modelId);
		loadDistanceCharacteristicsOnModel(modelId);
		loadCohesiveGroupsCriteriaOnModel(modelId);

		solveModel(modelId);
	}

	private void solveModel(final Integer modelId) {
		SolverConfiguration config = new SolverConfiguration();
		HttpEntity<SolverConfiguration> request = IntegrationTestHelper.createHttpRequestWithPostObj(config);
		ResponseEntity<SolverResult> solverResponse = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/solver/" + modelId, HttpMethod.POST, request,
				new ParameterizedTypeReference<SolverResult>() {
				});

		assertEquals(HttpStatus.OK, solverResponse.getStatusCode());

		log.info("found services {}", solverResponse.getBody().getServices());
	}

	private void loadBusinessTransactionOnModel(final Integer modelId) throws UnsupportedEncodingException, URISyntaxException, IOException {
		List<UseCase> transactions = IntegrationTestHelper.readListFromFile(DDD_USE_CASES_FILE, UseCase.class);

		log.info("read Use Casess: {}", transactions);

		HttpEntity<List<UseCase>> request = IntegrationTestHelper.createHttpRequestWithPostObj(transactions);
		String path = "http://localhost:" + this.port + "/engine/import/" + modelId.toString() + "/businessTransactions/";
		log.info("store business transactions on {}", path);

		ResponseEntity<Void> entity = this.restTemplate.exchange(path, HttpMethod.POST, request, new ParameterizedTypeReference<Void>() {
		});

		assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
	}

	private void loadDistanceCharacteristicsOnModel(final Integer modelId) throws UnsupportedEncodingException, URISyntaxException, IOException {
		List<DistanceCharacteristic> characteristics = IntegrationTestHelper.readListFromFile(DDD_CHARACTERISTICS_FILE, DistanceCharacteristic.class);

		log.info("read distance characteristics: {}", characteristics);

		HttpEntity<List<DistanceCharacteristic>> request = IntegrationTestHelper.createHttpRequestWithPostObj(characteristics);
		String path = "http://localhost:" + this.port + "/engine/import/" + modelId.toString() + "/distanceCharacteristics/";
		log.info("store distance characteristics on {}", path);

		ResponseEntity<Void> entity = this.restTemplate.exchange(path, HttpMethod.POST, request, new ParameterizedTypeReference<Void>() {
		});

		assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
	}

	private void loadCohesiveGroupsCriteriaOnModel(final Integer modelId) throws UnsupportedEncodingException, URISyntaxException, IOException {
		List<CohesiveGroups> criteria = IntegrationTestHelper.readListFromFile(DDD_RESPONSIBILITES_FILE, CohesiveGroups.class);

		log.info("read separation criteria: {}", criteria);

		HttpEntity<List<CohesiveGroups>> request = IntegrationTestHelper.createHttpRequestWithPostObj(criteria);
		String path = "http://localhost:" + this.port + "/engine/import/" + modelId.toString() + "/cohesiveGroups/";
		log.info("store cohesive groups criteria on {}", path);

		ResponseEntity<Void> entity = this.restTemplate.exchange(path, HttpMethod.POST, request, new ParameterizedTypeReference<Void>() {
		});

		assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
	}

	private Integer createModelOnApi() throws URISyntaxException, UnsupportedEncodingException, IOException {
		DomainModel input = IntegrationTestHelper.readFromFile(DDD_EXAMPLE_FILE, DomainModel.class);

		HttpEntity<DomainModel> request = IntegrationTestHelper.createHttpRequestWithPostObj(input);
		ResponseEntity<Map<String, Object>> entity = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/import", HttpMethod.POST, request,
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		assertEquals(HttpStatus.OK, entity.getStatusCode());
		Integer modelId = (Integer) entity.getBody().get("id");
		assertNotNull(modelId);
		assertTrue(((String) entity.getBody().get("message")).startsWith("model "));
		return modelId;
	}

}
