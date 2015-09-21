package ch.hsr.servicestoolkit.editor.web.rest;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import ch.hsr.servicestoolkit.editor.security.AuthoritiesConstants;

@RestController
@RequestMapping(value = "/editor")
@Secured(AuthoritiesConstants.USER)
public class EditorController {

	private final Logger log = LoggerFactory.getLogger(EditorController.class);
	private final RestTemplate rest = new RestTemplate();

	@RequestMapping(value = "/upload", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
	public String test(@RequestParam("file") MultipartFile file) {
		String result = "";
		try {
			String theString = IOUtils.toString(file.getInputStream());
			log.trace("file content:{}", theString);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> requestEntity = new HttpEntity<Object>(theString, headers);
			ResponseEntity<String> responseEntity = rest.exchange("http://localhost:8090/engine/import", HttpMethod.PUT, requestEntity, String.class);
			result = responseEntity.getBody();
			log.debug("importer response: {}", result);
		} catch (IOException e) {
			log.error("", e);
		}
		return result;
	}

}
