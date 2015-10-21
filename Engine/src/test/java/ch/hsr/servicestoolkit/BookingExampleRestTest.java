package ch.hsr.servicestoolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import ch.hsr.servicestoolkit.importer.api.BusinessTransaction;
import ch.hsr.servicestoolkit.importer.api.DomainModel;
import ch.hsr.servicestoolkit.solver.BoundedContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
@IntegrationTest("server.port=0")
@WebAppConfiguration
public class BookingExampleRestTest {

	private static final String BOOKING_EXAMPLE_JSON = "booking_example.json";
	private static final String BOOKING_EXAMPLE_BUSINESS_TRANSACTION = "booking_example_business_transactions.json";
	@Value("${local.server.port}")
	private int port;
	private RestTemplate restTemplate = new TestRestTemplate();
	private Logger log = LoggerFactory.getLogger(BookingExampleRestTest.class);

	@Test
	public void bookingExample() throws UnsupportedEncodingException, URISyntaxException, IOException {
		Integer modelId = createModelOnApi();

		loadBusinessTransactionOnModel(modelId);

		// TODO re-enable
		// solveModel(modelId);
	}

	private void solveModel(final Integer modelId) {
		ResponseEntity<Set<BoundedContext>> solverResponse = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/solver/" + modelId, HttpMethod.POST,
				IntegrationTestHelper.createEmptyHttpRequest(), new ParameterizedTypeReference<Set<BoundedContext>>() {
				});

		assertEquals(HttpStatus.OK, solverResponse.getStatusCode());

		log.info("found services {}", solverResponse.getBody());
	}

	private void loadBusinessTransactionOnModel(final Integer modelId) throws UnsupportedEncodingException, URISyntaxException, IOException {
		List<BusinessTransaction> transactions = IntegrationTestHelper.readBusinessTransactionsFromFile(BOOKING_EXAMPLE_BUSINESS_TRANSACTION);

		log.info("read business Transactions: {}", transactions);

		HttpEntity<List<BusinessTransaction>> request = IntegrationTestHelper.createHttpRequestWithPostObj(transactions);
		String path = "http://localhost:" + this.port + "/engine/import/" + modelId.toString() + "/businessTransactions/";
		log.info("store business transactions on {}", path);

		ResponseEntity<Void> entity = this.restTemplate.exchange(path, HttpMethod.POST, request, new ParameterizedTypeReference<Void>() {
		});

		assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
	}

	private Integer createModelOnApi() throws URISyntaxException, UnsupportedEncodingException, IOException {
		DomainModel input = IntegrationTestHelper.readDomainModelFromFile(BOOKING_EXAMPLE_JSON);

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
