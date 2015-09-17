package ch.hsr.servicestoolkit;

import java.util.Arrays;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Path("/engine")
public class Endpoint {

	private Logger log = LoggerFactory.getLogger(Endpoint.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public EngineState message() {
		return new EngineState("Engine is up and running.");
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void startProcessing(String[] entities) {
		log.info("started process, consumed {} entities: {}", entities.length, Arrays.toString(entities));
	}

	public class EngineState {
		private String description;

		public EngineState(String description) {
			this.setDescription(description);
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

}
