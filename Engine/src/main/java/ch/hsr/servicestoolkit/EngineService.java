package ch.hsr.servicestoolkit;

import java.util.List;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.hsr.servicestoolkit.model.EngineState;
import ch.hsr.servicestoolkit.model.Model;
import jersey.repackaged.com.google.common.collect.Lists;

@Component
@Path("/engine")
public class EngineService {

	private Logger log = LoggerFactory.getLogger(EngineService.class);
	private ModelRepository modelRepository;

	@Autowired
	public EngineService(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public EngineState message() {
		return new EngineState("Engine is up and running.");
	}

	@GET
	@Path("/models")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Model[] models() {
		List<Model> result = Lists.newArrayList(modelRepository.findAll());
		return result.toArray(new Model[result.size()]);
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public String createModel(Model model) {
		if (model != null) {
			log.info("created model containing {} entities.", model.getEntities().size());
		} else {
			model = new Model();
			log.info("created empty model");
		}
		modelRepository.save(model);
		return "started!";
	}

}
