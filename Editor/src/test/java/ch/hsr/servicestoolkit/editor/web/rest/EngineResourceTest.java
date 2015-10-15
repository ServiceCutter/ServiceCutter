package ch.hsr.servicestoolkit.editor.web.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class EngineResourceTest {

	@Test
	public void testMapRequestURI() {
		EngineResource sut = new EngineResource();
		String actual = sut.mapRequestURI("/api/engine/models");
		assertThat(actual, equalTo("/engine/models"));
	}

	@Test
	public void testMapRequestURINoMiddleReplacement() {
		EngineResource sut = new EngineResource();
		String actual = sut.mapRequestURI("/api/engine/api/models");
		assertThat(actual, equalTo("/engine/api/models"));
	}

}
