package ch.hsr.servicestoolkit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CriterionType;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.EngineState;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.repository.CouplingCriterionRepository;
import ch.hsr.servicestoolkit.repository.DataFieldRepository;
import ch.hsr.servicestoolkit.repository.ModelRepository;
import jersey.repackaged.com.google.common.collect.Lists;

@Component
@Path("/engine")
public class EngineService {

	private Logger log = LoggerFactory.getLogger(EngineService.class);
	private ModelRepository modelRepository;
	private DataFieldRepository dataRepository;
	private CouplingCriterionRepository couplingCriterionRepository;

	@Autowired
	public EngineService(ModelRepository modelRepository, DataFieldRepository dataRepository, CouplingCriterionRepository couplingCriterionRepository) {
		this.modelRepository = modelRepository;
		this.dataRepository = dataRepository;
		this.couplingCriterionRepository = couplingCriterionRepository;
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
	public Model getModel(@PathParam("id") Long id) {
		Model result = modelRepository.findOne(id);
		result.getDataFields().size(); // init lazy collection
		return result;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/models/{id}/couplingcriteria")
	@Transactional
	public Set<CouplingCriterion> getCouplingCriteria(@PathParam("id") Long id, @QueryParam("type") String type) {
		Set<CouplingCriterion> result = new HashSet<>();
		Model model = modelRepository.findOne(id);
		CriterionType sameEntitiy = StringUtils.hasText(type) ? CriterionType.valueOf(type) : null;
		for (DataField dataField : model.getDataFields()) {
			for (CouplingCriterion qa : dataField.getCouplingCriteria()) {
				if (sameEntitiy == null || sameEntitiy.equals(qa.getCriterionType())) {
					result.add(qa);
				}
			}
		}
		log.debug("return criteria for model {}: {}", model.getName(), result.toString());
		return result;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/models/{id}/couplingcriteria")
	@Transactional
	public void storeCouplingCriteria(Set<CouplingCriterion> criteria, @PathParam("id") Long id) {
		Model model = modelRepository.findOne(id);
		// TODO: refactor
		for (CouplingCriterion inputCriterion : criteria) {
			CouplingCriterion mappedСriterion = new CouplingCriterion();
			for (DataField inputDataField : inputCriterion.getDataFields()) {
				DataField dbDataField = findDbDataField(model.getDataFields(), inputDataField.getName());
				if (dbDataField == null) {
					throw new IllegalArgumentException("referenced data field not existing: " + model.getName() + ":" + inputDataField.getName());
				}
				mappedСriterion.getDataFields().add(dbDataField);
				mappedСriterion.setCriterionType(inputCriterion.getCriterionType());
				dbDataField.getCouplingCriteria().add(inputCriterion);
				couplingCriterionRepository.save(mappedСriterion);
				dataRepository.save(dbDataField);
				log.debug("added coupling criterion {} to model {}", inputCriterion.getId(), model.getName());
			}
		}
	}

	@PUT
	@Path("/models/{modelName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Model createModel(Model model, @PathParam("modelName") String modelName) {
		final String finalModelName = getNameForModel(model, modelName);
		if (model == null) {
			model = new Model();
		}
		model.setName(finalModelName);

		log.info("created model {} containing {} datafields.", finalModelName, model.getDataFields().size());
		modelRepository.save(model);
		return model;
	}

	// TODO: put this in a repository
	private DataField findDbDataField(List<DataField> dataFields, String name) {
		for (DataField field : dataFields) {
			if (field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}

	private String getNameForModel(Model model, String name) {
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
