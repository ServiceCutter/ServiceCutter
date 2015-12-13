package ch.hsr.servicestoolkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic;
import ch.hsr.servicestoolkit.model.CouplingInstance;
import ch.hsr.servicestoolkit.model.CouplingType;
import ch.hsr.servicestoolkit.model.EngineState;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.repository.CouplingCriterionCharacteristicRepository;
import ch.hsr.servicestoolkit.model.repository.CouplingCriterionRepository;
import ch.hsr.servicestoolkit.model.repository.CouplingInstanceRepository;
import ch.hsr.servicestoolkit.model.repository.ModelRepository;

@Component
@Path("/engine")
public class EngineService {

	private Logger log = LoggerFactory.getLogger(EngineService.class);
	private ModelRepository modelRepository;
	private CouplingCriterionRepository couplingCriterionRepository;
	private CouplingCriterionCharacteristicRepository couplingCriteriaCharacteristicRepository;
	private CouplingInstanceRepository couplingInstanceRepository;

	@Autowired
	public EngineService(final ModelRepository modelRepository, final CouplingCriterionRepository couplingCriterionRepository, final CouplingInstanceRepository couplingInstanceRepository,
			final CouplingCriterionCharacteristicRepository couplingCriteriaCharacteristicRepository) {
		this.modelRepository = modelRepository;
		this.couplingCriterionRepository = couplingCriterionRepository;
		this.couplingInstanceRepository = couplingInstanceRepository;
		this.couplingCriteriaCharacteristicRepository = couplingCriteriaCharacteristicRepository;
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

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/models/{id}/couplingcriteria")
	@Transactional
	public List<CouplingInstance> getModelCoupling(@PathParam("id") final Long id) {
		List<CouplingInstance> result = new ArrayList<>();
		Model model = modelRepository.findOne(id);
		Set<CouplingInstance> instances = couplingInstanceRepository.findByModel(model);
		result.addAll(instances);
		for (CouplingInstance couplingInstance : instances) {
			// init lazy collection, otherwise you'll get a serialization
			// exception as the transaction is already closed
			couplingInstance.getAllNanoentities().size();
		}
		log.debug("return criteria for model {}: {}", model.getName(), result.toString());
		Collections.sort(result);
		return result;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/couplingcriteria")
	@Transactional
	public List<CouplingCriterionDTO> getCouplingCriteria() {
		Stream<CouplingCriterion> criteria = StreamSupport.stream(couplingCriterionRepository.findAll().spliterator(), false);
		List<CouplingCriterionDTO> result = criteria.map(criterion -> {
			return new CouplingCriterionDTO(criterion, couplingCriteriaCharacteristicRepository.readByCouplingCriterion(criterion));
		}).sorted().collect(Collectors.toList());
		return result;
	}

	public class CouplingCriterionDTO implements Comparable<CouplingCriterionDTO> {

		private final Long id;
		private final String code;
		private final String name;
		private final String description;
		private final List<CouplingCriterionCharacteristic> characteristics;
		private final String decompositionImpact;
		private final CouplingType type;

		public CouplingCriterionDTO(final CouplingCriterion couplingCriterion, final List<CouplingCriterionCharacteristic> characteristics) {
			this.characteristics = characteristics;
			this.name = couplingCriterion.getName();
			this.id = couplingCriterion.getId();
			this.description = couplingCriterion.getDescription();
			decompositionImpact = couplingCriterion.getDecompositionImpact();
			type = couplingCriterion.getType();
			code = couplingCriterion.getCode();
		}

		public Long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public List<CouplingCriterionCharacteristic> getCharacteristics() {
			return characteristics;
		}

		public String getDescription() {
			return description;
		}

		public String getDecompositionImpact() {
			return decompositionImpact;
		}

		public CouplingType getType() {
			return type;
		}

		public String getCode() {
			return code;
		}

		@Override
		public int compareTo(CouplingCriterionDTO o) {
			Integer thisNumber = new Integer(code.split("-")[1]);
			Integer otherNumber = new Integer(o.getCode().split("-")[1]);
			return thisNumber.compareTo(otherNumber);
		}

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
