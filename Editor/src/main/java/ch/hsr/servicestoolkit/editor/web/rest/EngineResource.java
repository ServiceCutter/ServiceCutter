package ch.hsr.servicestoolkit.editor.web.rest;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/engine")
public class EngineResource {
	@Value("${application.links.engine.host}")
	private String host = null;
	@Value("${application.links.engine.port}")
	private Integer port = null;
	private RestTemplate restTemplate = new RestTemplate();

	/**
	 * a spring mvc forward proxy inspired by
	 * http://stackoverflow.com/a/23736527/2219787
	 */
	@RequestMapping("/**")
	@ResponseBody
	public Object mirrorRest(@RequestBody(required = false) Object body, HttpMethod method, HttpServletRequest request, HttpServletResponse response) throws URISyntaxException {
		String requestURI = mapRequestURI(request.getRequestURI());
		URI uri = new URI("http", null, host, port, requestURI, request.getQueryString(), null);
		ResponseEntity<Object> responseEntity = restTemplate.exchange(uri, method, new HttpEntity<Object>(body), Object.class);
		return responseEntity.getBody();
	}

	String mapRequestURI(String requestURI) {
		return requestURI.replaceFirst("^/api", "");
	}

}
