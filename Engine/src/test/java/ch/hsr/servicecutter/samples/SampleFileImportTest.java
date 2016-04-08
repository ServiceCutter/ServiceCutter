package ch.hsr.servicecutter.samples;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import ch.hsr.servicecutter.EngineServiceAppication;
import ch.hsr.servicecutter.IntegrationTestHelper;
import ch.hsr.servicecutter.UrlHelper;
import ch.hsr.servicecutter.importer.ImportResult;
import ch.hsr.servicecutter.importer.api.EntityRelationDiagram;
import ch.hsr.servicecutter.importer.api.UserRepresentationContainer;
import ch.hsr.servicecutter.model.repository.CouplingInstanceRepository;
import ch.hsr.servicecutter.model.solver.SolverResult;
import ch.hsr.servicecutter.solver.SolverConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
@IntegrationTest("server.port=0")
@WebAppConfiguration
public class SampleFileImportTest {

	@Value("${local.server.port}")
	private int port;
	private RestTemplate restTemplate = new TestRestTemplate();
	@Autowired
	private CouplingInstanceRepository couplingInstanceRepository;
	private static final Logger LOG = LoggerFactory.getLogger(SampleFileImportTest.class);

	@Test
	public void testBookingSample() throws UnsupportedEncodingException, URISyntaxException, IOException {
		Long modelId = createModelOnApi("booking_1_model.json");
		uploadUserRepresentations(modelId, "booking_2_user_representations.json");
		solveModel(modelId);
	}

	@Test
	public void testTradingSystem() throws UnsupportedEncodingException, URISyntaxException, IOException {
		Long modelId = createModelOnApi("trading_1_model.json");

		long before = couplingInstanceRepository.count();
		uploadUserRepresentations(modelId, "trading_2_user_representations.json");
		long after = couplingInstanceRepository.count();
		assertThat(after, greaterThan(before));

		solveModel(modelId);
	}

	@Test
	public void testDDD() throws UnsupportedEncodingException, URISyntaxException, IOException {
		Long modelId = createModelOnApi("ddd_1_model.json");
		uploadUserRepresentations(modelId, "ddd_2_user_representations.json");
		solveModel(modelId);
	}

	private void uploadUserRepresentations(final Long modelId, String representationsFile) throws URISyntaxException, UnsupportedEncodingException, IOException {
		UserRepresentationContainer userRepContainer = IntegrationTestHelper.readFromFile(representationsFile, UserRepresentationContainer.class);

		LOG.info("read user Representations: {}", userRepContainer);

		HttpEntity<UserRepresentationContainer> request = IntegrationTestHelper.createHttpRequestWithPostObj(userRepContainer);
		String path = UrlHelper.userRepresentations(modelId, port);
		LOG.info("store user representations on {}", path);

		ResponseEntity<ImportResult> entity = this.restTemplate.exchange(path, HttpMethod.POST, request, new ParameterizedTypeReference<ImportResult>() {
		});

		assertThat((entity.getBody().getWarnings()), empty());
		assertThat(entity.getStatusCode(), is(HttpStatus.OK));
	}

	private void solveModel(final Long modelId) {
		SolverConfiguration config = new SolverConfiguration();
		HttpEntity<SolverConfiguration> request = IntegrationTestHelper.createHttpRequestWithPostObj(config);
		ResponseEntity<SolverResult> solverResponse = this.restTemplate.exchange(UrlHelper.solve(modelId, port), HttpMethod.POST, request, new ParameterizedTypeReference<SolverResult>() {
		});

		assertThat(solverResponse.getStatusCode(), is(HttpStatus.OK));

		LOG.info("found services {}", solverResponse.getBody().getServices());
	}

	private Long createModelOnApi(String modelFile) throws URISyntaxException, UnsupportedEncodingException, IOException {
		EntityRelationDiagram input = IntegrationTestHelper.readFromFile(modelFile, EntityRelationDiagram.class);

		HttpEntity<EntityRelationDiagram> request = IntegrationTestHelper.createHttpRequestWithPostObj(input);
		ResponseEntity<ImportResult> entity = this.restTemplate.exchange(UrlHelper.importDomain(port), HttpMethod.POST, request, new ParameterizedTypeReference<ImportResult>() {
		});

		assertThat(entity.getStatusCode(), is(HttpStatus.OK));
		Long modelId = entity.getBody().getId();
		assertThat(modelId, notNullValue());
		assertThat(entity.getBody().getMessage(), startsWith("userSystem "));
		return modelId;
	}

}
