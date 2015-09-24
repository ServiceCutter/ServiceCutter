package ch.hsr.servicestoolkit.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import ch.hsr.servicestoolkit.importer.api.DomainModel;
import ch.hsr.servicestoolkit.importer.api.EntityAttribute;
import ch.hsr.servicestoolkit.importer.api.EntityModel;
import ch.hsr.servicestoolkit.importer.api.EntityRelation;
import ch.hsr.servicestoolkit.importer.api.EntityRelation.RelationType;
import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CriterionType;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.repository.CouplingCriterionRepository;
import ch.hsr.servicestoolkit.repository.ModelRepository;

@Component
@Path("/engine/import")
public class ImportEndpoint {

	private final Logger log = LoggerFactory.getLogger(ImportEndpoint.class);
	private final ModelRepository modelRepository;
	private final CouplingCriterionRepository couplingCriterionRepository;
	private final Map<RelationType, CriterionType> typeMapping = new HashMap<>();

	@Autowired
	public ImportEndpoint(final ModelRepository modelRepository, final CouplingCriterionRepository couplingCriterionRepository) {
		this.modelRepository = modelRepository;
		this.couplingCriterionRepository = couplingCriterionRepository;
		typeMapping.put(RelationType.AGGREGATION, CriterionType.AGGREGATED_ENTITY);
		typeMapping.put(RelationType.COMPOSITION, CriterionType.COMPOSITION_ENTITY);
		typeMapping.put(RelationType.INHERITANCE, CriterionType.INHERITANCE);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Map<String, Object> importDomainModel(final DomainModel domainModel) {
		Assert.notNull(domainModel);
		Model model = new Model();
		model.setName("imported " + new Date().toString());
		Map<EntityModel, List<DataField>> fieldsByModel = new HashMap<>();
		for (EntityModel entityModel : domainModel.getEntities()) {
			CouplingCriterion criterion = new CouplingCriterion();
			criterion.setName(entityModel.getName());
			criterion.setCriterionType(CriterionType.SAME_ENTITIY);
			couplingCriterionRepository.save(criterion);
			List<DataField> fields = new ArrayList<>();
			for (EntityAttribute entityAttribute : entityModel.getAttributes()) {
				DataField dataField = new DataField();
				dataField.setName(entityAttribute.getName());
				dataField.setContext(entityModel.getName());
				model.addDataField(dataField);
				log.info("added data field '{}' on entity '{}'", dataField.getName(), entityModel.getName());
				criterion.addDataField(dataField);
				fields.add(dataField);
			}
			fieldsByModel.put(entityModel, fields);
		}
		for (EntityRelation relation : domainModel.getRelations()) {
			CouplingCriterion criterion = new CouplingCriterion();
			criterion.setName(relation.getOrigin().getName() + "." + relation.getDestination().getName());
			CriterionType type = typeMapping.get(relation.getType());
			criterion.setCriterionType(type);
			List<DataField> fields = new ArrayList<>();
			fields.addAll(fieldsByModel.get(relation.getOrigin()));
			fields.addAll(fieldsByModel.get(relation.getDestination()));
			for (DataField dataField : fields) {
				criterion.addDataField(dataField);
			}
			couplingCriterionRepository.save(criterion);
		}
		modelRepository.save(model);
		// TODO: remove return value and set location header to URL of generated
		// model
		Map<String, Object> result = new HashMap<>();
		result.put("message", "model " + model.getId() + " has been created");
		result.put("id", model.getId());
		return result;
	}

}
