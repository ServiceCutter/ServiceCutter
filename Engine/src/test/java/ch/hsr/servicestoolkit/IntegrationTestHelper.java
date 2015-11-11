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

public class IntegrationTestHelper {

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

	public static <T> List<T> readListFromFile(final String filepath, final Class<T> type) throws URISyntaxException, UnsupportedEncodingException, IOException {
		ObjectMapper mapper = new ObjectMapperContextResolver().getContext(null);
		List<T> result = mapper.readValue(readFromFile(filepath), new TypeReference<List<T>>() {
		});
		return result;
	}

	public static <T> T readFromFile(final String filepath, final Class<T> type) throws URISyntaxException, UnsupportedEncodingException, IOException {
		ObjectMapper mapper = new ObjectMapperContextResolver().getContext(null);
		T result = mapper.readValue(readFromFile(filepath), type);
		return result;
	}

	private static String readFromFile(final String filepath) throws URISyntaxException, UnsupportedEncodingException, IOException {
		URL url = IntegrationTestHelper.class.getClassLoader().getResource(filepath);
		Path resPath = java.nio.file.Paths.get(url.toURI());
		String input = new String(java.nio.file.Files.readAllBytes(resPath), Charset.defaultCharset().name());
		return input;
	}

}
