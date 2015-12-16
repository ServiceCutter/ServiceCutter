package ch.hsr.servicestoolkit;

import java.util.List;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

import ch.hsr.servicestoolkit.model.EngineState;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.repository.ModelRepository;

@Component
@Path("/engine")
public class EngineService {

	private Logger log = LoggerFactory.getLogger(EngineService.class);
	private ModelRepository modelRepository;

	@Autowired
	public EngineService(final ModelRepository modelRepository) {
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

	@GET
	@Path("/models/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Model getModel(@PathParam("id") final Long id) {
		return modelRepository.findOne(id);
	}

	@POST
	@Path("/models")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Model createModel(Model model, @PathParam("modelName") final String modelName) {
		final String finalModelName = getNameForModel(model, modelName);
		if (model == null) {
			model = new Model();
		}
		model.setName(finalModelName);

		log.info("created model {} containing {} nanoentities.", finalModelName, model.getNanoentities().size());
		modelRepository.save(model);
		return model;
	}

	private String getNameForModel(final Model model, final String name) {
		String modelName = (model == null || StringUtils.isEmpty(model.getName())) ? null : model.getName();
		if (modelName == null && StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("no name defined for model");
		} else if (modelName == null && !StringUtils.isEmpty(name)) {
			return name;
		} else if (modelName != null && StringUtils.isEmpty(name)) {
			return modelName;
		} else if (modelName.equals(name)) {
			return modelName;
		} else {
			throw new IllegalArgumentException("inconsistent model name in URI and body object");
		}
	}

}
