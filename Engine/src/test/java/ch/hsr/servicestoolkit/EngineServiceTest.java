package ch.hsr.servicestoolkit;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
@IntegrationTest("server.port=0")
@WebAppConfiguration
public class EngineServiceTest {

	@Value("${local.server.port}")
	private int port;
	private RestTemplate restTemplate = new TestRestTemplate();

	@Before
	public void setUp() {
		restTemplate.setMessageConverters(Arrays.asList(new MappingJackson2HttpMessageConverter(), new StringHttpMessageConverter()));
	}

	@Test
	public void statusCheck() {
		ResponseEntity<EngineState> entity = this.restTemplate.getForEntity("http://localhost:" + this.port + "/engine", EngineState.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		assertEquals("Engine is up and running.", entity.getBody().getDescription());
	}

	@Test
	public void startProcessing() {
		String[] sampleData = new String[] {"foo", "bar"};
		HttpEntity<String[]> request = createJsonHttpRequest(sampleData);
		ResponseEntity<String> entity = this.restTemplate.exchange("http://localhost:" + this.port + "/engine", HttpMethod.PUT, request, String.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		assertEquals("started!", entity.getBody());
	}

	private HttpEntity<String[]> createJsonHttpRequest(String[] sampleData) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String[]> request = new HttpEntity<String[]>(sampleData, headers);
		return request;
	}

}
