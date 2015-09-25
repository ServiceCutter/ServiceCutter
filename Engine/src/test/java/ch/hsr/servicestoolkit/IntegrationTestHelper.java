package ch.hsr.servicestoolkit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.hsr.servicestoolkit.importer.api.DomainModel;
import ch.hsr.servicestoolkit.model.CriterionType;
import ch.hsr.servicestoolkit.solver.SolverConfiguration;

public class IntegrationTestHelper {

	public static DomainModel readDomainModelFromFile(final String file) throws URISyntaxException, UnsupportedEncodingException, IOException {
		URL url = IntegrationTestHelper.class.getClassLoader().getResource(file);
		Path resPath = java.nio.file.Paths.get(url.toURI());
		String input = new String(java.nio.file.Files.readAllBytes(resPath), Charset.defaultCharset().name());
		ObjectMapper mapper = new ObjectMapperContextResolver().getContext(null);
		DomainModel domainModel = mapper.readValue(input, DomainModel.class);
		return domainModel;
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

	public static SolverConfiguration createSolverConfiguration() {
		SolverConfiguration config = new SolverConfiguration();
		Map<CriterionType, Double> weights = new HashMap<>();
		for (CriterionType type : CriterionType.values()) {
			switch (type) {
			case AGGREGATED_ENTITY:
				weights.put(type, 0.0);
				break;
			case COMPOSITION_ENTITY:
				weights.put(type, 0.0);
				break;
			case SAME_ENTITIY:
				weights.put(type, 0.2);
				break;
			}
		}
		config.setWeights(weights);

		return config;
	}

}
