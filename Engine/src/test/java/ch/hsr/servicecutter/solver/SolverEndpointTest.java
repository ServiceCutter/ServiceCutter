package ch.hsr.servicecutter.solver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
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

import ch.hsr.servicecutter.EngineServiceAppication;
import ch.hsr.servicecutter.IntegrationTestHelper;
import ch.hsr.servicecutter.importer.api.DomainModel;
import ch.hsr.servicecutter.solver.Service;
import ch.hsr.servicecutter.solver.SolverResult;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
@IntegrationTest("server.port=0")
@WebAppConfiguration
public class SolverEndpointTest {

	@Value("${local.server.port}")
	private int port;
	private RestTemplate restTemplate = new TestRestTemplate();

	@Test
	public void testNonExstingModel() {

		ResponseEntity<SolverResult> entity = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/solver/234", HttpMethod.POST,
				IntegrationTestHelper.createEmptyHttpRequest(), new ParameterizedTypeReference<SolverResult>() {
				});
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		assertTrue(entity.getBody().getServices().isEmpty());
	}

	@Test
	@Ignore
	public void testExampleModelFromFile() throws UnsupportedEncodingException, URISyntaxException, IOException {

		DomainModel model = IntegrationTestHelper.readFromFile("test_domain_model.json", DomainModel.class);

		HttpEntity<DomainModel> request = IntegrationTestHelper.createHttpRequestWithPostObj(model);
		ResponseEntity<Map<String, Object>> modelResponse = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/import", HttpMethod.POST, request,
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		Integer modelId = (Integer) modelResponse.getBody().get("id");
		ResponseEntity<Set<Service>> solverResponse = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/solver/" + modelId, HttpMethod.POST,
				IntegrationTestHelper.createEmptyHttpRequest(), new ParameterizedTypeReference<Set<Service>>() {
				});

		assertEquals(HttpStatus.OK, solverResponse.getStatusCode());
		assertEquals(9, solverResponse.getBody().size()); // 6 entities defined

	}

}
