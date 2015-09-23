package ch.hsr.servicestoolkit.solver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import ch.hsr.servicestoolkit.EngineServiceAppication;

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
		SolverConfiguration config = new SolverConfiguration();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<SolverConfiguration> request = new HttpEntity<SolverConfiguration>(config, headers);

		ResponseEntity<Set<BoundedContext>> entity = this.restTemplate.exchange(
				"http://localhost:" + this.port + "/engine/solver/234", HttpMethod.POST, request,
				new ParameterizedTypeReference<Set<BoundedContext>>() {
				});
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		assertTrue(entity.getBody().isEmpty());
	}
}
