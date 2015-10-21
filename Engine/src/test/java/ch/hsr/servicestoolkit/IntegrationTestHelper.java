package ch.hsr.servicestoolkit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.hsr.servicestoolkit.importer.api.BusinessTransaction;
import ch.hsr.servicestoolkit.importer.api.DomainModel;

public class IntegrationTestHelper {

	public static DomainModel readDomainModelFromFile(final String file) throws URISyntaxException, UnsupportedEncodingException, IOException {
		URL url = IntegrationTestHelper.class.getClassLoader().getResource(file);
		Path resPath = java.nio.file.Paths.get(url.toURI());
		String input = new String(java.nio.file.Files.readAllBytes(resPath), Charset.defaultCharset().name());
		ObjectMapper mapper = new ObjectMapperContextResolver().getContext(null);
		DomainModel domainModel = mapper.readValue(input, DomainModel.class);
		return domainModel;
	}

	public static List<BusinessTransaction> readBusinessTransactionsFromFile(final String file) throws URISyntaxException, UnsupportedEncodingException, IOException {
		URL url = IntegrationTestHelper.class.getClassLoader().getResource(file);
		Path resPath = java.nio.file.Paths.get(url.toURI());
		String input = new String(java.nio.file.Files.readAllBytes(resPath), Charset.defaultCharset().name());
		ObjectMapper mapper = new ObjectMapperContextResolver().getContext(null);
		List<BusinessTransaction> transactions = mapper.readValue(input, new TypeReference<List<BusinessTransaction>>() {
		});
		return transactions;
	}

	public static <T> HttpEntity<T> createHttpRequestWithPostObj(final T obj) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		return new HttpEntity<T>(obj, headers);
	}

	public static HttpEntity<Object> createEmptyHttpRequest() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<Object>(headers);
	}

}
