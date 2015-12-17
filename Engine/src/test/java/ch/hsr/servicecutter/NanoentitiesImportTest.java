package ch.hsr.servicecutter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;

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

import ch.hsr.servicecutter.importer.api.NanoentitiesImport;
import ch.hsr.servicecutter.model.usersystem.UserSystem;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
@IntegrationTest("server.port=0")
@WebAppConfiguration
public class NanoentitiesImportTest {
	@Value("${local.server.port}")
	private int port;
	private RestTemplate restTemplate = new TestRestTemplate();

	@Test
	public void modelImport() throws IOException, URISyntaxException {
		Long systemId = createSystem("testSystem");

		NanoentitiesImport input = IntegrationTestHelper.readFromFile("nanoentitiesImport.json", NanoentitiesImport.class);

		HttpEntity<NanoentitiesImport> request = IntegrationTestHelper.createHttpRequestWithPostObj(input);
		ResponseEntity<UserSystem> entity = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/import/" + systemId + "/nanoentities", HttpMethod.POST, request,
				UserSystem.class);

		assertEquals(HttpStatus.OK, entity.getStatusCode());

		assertNotNull(entity.getBody().getId());
		assertEquals(entity.getBody().getNanoentities().size(), 3);
	}

	private Long createSystem(final String name) {

		HttpEntity<String> request = IntegrationTestHelper.createHttpRequestWithPostObj(name);
		ResponseEntity<UserSystem> system = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/systems/", HttpMethod.POST, request, UserSystem.class);

		assertEquals(HttpStatus.OK, system.getStatusCode());

		assertNotNull(system.getBody().getId());
		return system.getBody().getId();
	}
}
