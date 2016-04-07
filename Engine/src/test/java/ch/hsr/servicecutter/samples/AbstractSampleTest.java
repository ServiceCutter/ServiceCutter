package ch.hsr.servicecutter.samples;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

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

import ch.hsr.servicecutter.EngineServiceAppication;
import ch.hsr.servicecutter.IntegrationTestHelper;
import ch.hsr.servicecutter.UrlHelper;
import ch.hsr.servicecutter.importer.ImportResult;
import ch.hsr.servicecutter.importer.api.EntityRelationDiagram;
import ch.hsr.servicecutter.importer.api.UserRepresentationContainer;
import ch.hsr.servicecutter.model.solver.SolverResult;
import ch.hsr.servicecutter.solver.SolverConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
@IntegrationTest("server.port=0")
@WebAppConfiguration
public abstract class AbstractSampleTest {

	@Value("${local.server.port}")
	private int port;
	private RestTemplate restTemplate = new TestRestTemplate();
	private Logger log = LoggerFactory.getLogger(AbstractSampleTest.class);

	@Test
	public void runTest() throws UnsupportedEncodingException, URISyntaxException, IOException {
		Long modelId = createModelOnApi();

		uploadUserRepresentations(modelId);

		solveModel(modelId);
	}

	private void uploadUserRepresentations(final Long modelId) throws URISyntaxException, UnsupportedEncodingException, IOException {
		UserRepresentationContainer userRepContainer = IntegrationTestHelper.readFromFile(getRepresentationsFile(), UserRepresentationContainer.class);

		log.info("read user Representations: {}", userRepContainer);

		HttpEntity<UserRepresentationContainer> request = IntegrationTestHelper.createHttpRequestWithPostObj(userRepContainer);
		String path = UrlHelper.userRepresentations(modelId, port);
		log.info("store user representations on {}", path);

		ResponseEntity<ImportResult> entity = this.restTemplate.exchange(path, HttpMethod.POST, request, new ParameterizedTypeReference<ImportResult>() {
		});

		assertThat((entity.getBody().getWarnings()), empty());
		assertEquals(HttpStatus.OK, entity.getStatusCode());
	}

	private void solveModel(final Long modelId) {
		SolverConfiguration config = new SolverConfiguration();
		HttpEntity<SolverConfiguration> request = IntegrationTestHelper.createHttpRequestWithPostObj(config);
		ResponseEntity<SolverResult> solverResponse = this.restTemplate.exchange(UrlHelper.solve(modelId, port), HttpMethod.POST, request, new ParameterizedTypeReference<SolverResult>() {
		});

		assertEquals(HttpStatus.OK, solverResponse.getStatusCode());

		log.info("found services {}", solverResponse.getBody().getServices());
	}

	private Long createModelOnApi() throws URISyntaxException, UnsupportedEncodingException, IOException {
		EntityRelationDiagram input = IntegrationTestHelper.readFromFile(getModelFile(), EntityRelationDiagram.class);

		HttpEntity<EntityRelationDiagram> request = IntegrationTestHelper.createHttpRequestWithPostObj(input);
		ResponseEntity<ImportResult> entity = this.restTemplate.exchange(UrlHelper.importDomain(port), HttpMethod.POST, request, new ParameterizedTypeReference<ImportResult>() {
		});

		assertEquals(HttpStatus.OK, entity.getStatusCode());
		Long modelId = entity.getBody().getId();
		assertNotNull(modelId);
		assertTrue(entity.getBody().getMessage().startsWith("userSystem "));
		return modelId;
	}

	protected abstract String getModelFile();

	protected abstract String getRepresentationsFile();

}
