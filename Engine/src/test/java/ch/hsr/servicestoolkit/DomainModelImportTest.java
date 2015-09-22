package ch.hsr.servicestoolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.hsr.servicestoolkit.importer.api.DomainModel;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
@IntegrationTest("server.port=0")
@WebAppConfiguration
public class DomainModelImportTest {
	@Value("${local.server.port}")
	private int port;
	private RestTemplate restTemplate = new TestRestTemplate();

	@Before
	public void setUp() {
		restTemplate.setMessageConverters(Arrays.asList(new MappingJackson2HttpMessageConverter(), new StringHttpMessageConverter()));
	}

	@Test
	public void modelImport() throws IOException, URISyntaxException {
		DomainModel input = readDomainModelFromFile("test_domain_model.json");

		HttpEntity<DomainModel> request = createJsonHttpEntityFromObject(input);
		ResponseEntity<String> entity = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/import", HttpMethod.PUT, request, String.class);

		assertEquals(HttpStatus.OK, entity.getStatusCode());
		// model 1319051726 has been created
		assertTrue(entity.getBody().startsWith("model "));
	}

	private DomainModel readDomainModelFromFile(String file) throws URISyntaxException, UnsupportedEncodingException, IOException {
		URL url = this.getClass().getClassLoader().getResource(file);
		Path resPath = java.nio.file.Paths.get(url.toURI());
		String input = new String(java.nio.file.Files.readAllBytes(resPath), Charset.defaultCharset().name());
		ObjectMapper mapper = new ObjectMapperContextResolver().getContext(null);
		DomainModel domainModel = mapper.readValue(input, DomainModel.class);
		return domainModel;
	}

	private <T> HttpEntity<T> createJsonHttpEntityFromObject(T obj) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<T>(obj, headers);
	}

}
