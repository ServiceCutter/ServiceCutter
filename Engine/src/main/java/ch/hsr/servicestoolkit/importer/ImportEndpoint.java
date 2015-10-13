package ch.hsr.servicestoolkit.importer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.hsr.servicestoolkit.importer.api.BusinessTransaction;
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
import ch.hsr.servicestoolkit.repository.DataFieldRepository;
import ch.hsr.servicestoolkit.repository.ModelRepository;

@Component
@Path("/engine/import")
public class ImportEndpoint {

	private final Logger log = LoggerFactory.getLogger(ImportEndpoint.class);
	private final ModelRepository modelRepository;
	private final CouplingCriterionRepository couplingCriterionRepository;
	private final Map<RelationType, CriterionType> typeMapping = new HashMap<>();
	private DataFieldRepository dataFieldRepository;

	@Autowired
	public ImportEndpoint(final ModelRepository modelRepository, final CouplingCriterionRepository couplingCriterionRepository, final DataFieldRepository dataFieldRepository) {
		this.modelRepository = modelRepository;
		this.couplingCriterionRepository = couplingCriterionRepository;
		this.dataFieldRepository = dataFieldRepository;
		typeMapping.put(RelationType.AGGREGATION, CriterionType.AGGREGATED_ENTITY);
		typeMapping.put(RelationType.COMPOSITION, CriterionType.COMPOSITION_ENTITY);
		typeMapping.put(RelationType.INHERITANCE, CriterionType.INHERITANCE);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Map<String, Object> importDomainModel(final DomainModel domainModel) {
		if (domainModel == null) {
			throw new InvalidRestParam();
		}
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

	@POST
	@Path("/{modelId}/businessTransactions/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void importBusinessTransaction(@PathParam("modelId") final Long modelId, final List<BusinessTransaction> transactions) {
		Model model = modelRepository.findOne(modelId);
		if (model == null) {
			throw new InvalidRestParam();
		}
		for (BusinessTransaction transaction : transactions) {
			List<String> fieldsRead = transaction.getFieldsRead();
			List<String> fieldsWritten = transaction.getFieldsWritten();

			for (int i = 0; i < fieldsRead.size(); i++) {
				for (int j = i + 1; j < fieldsRead.size(); j++) {
					saveCriterion(CriterionType.READ_BUSINESS_TRANSACTION, fieldsRead.get(i), fieldsRead.get(j));
				}
				for (int k = 0; k < fieldsWritten.size(); k++) {
					// it can be that a field is both defined in fieldsRead and
					// fieldsWritten. Don't add criterion on itself
					if (!fieldsRead.get(i).equals(fieldsWritten.get(k))) {
						saveCriterion(CriterionType.READ_WRITE_BUSINESS_TRANSACTION, fieldsRead.get(i), fieldsWritten.get(k));
					}
					for (int l = k + 1; l < fieldsWritten.size(); l++) {
						saveCriterion(CriterionType.WRITE_BUSINESS_TRANSACTION, fieldsWritten.get(k), fieldsWritten.get(l));
					}

				}
			}
		}

	}

	private void saveCriterion(final CriterionType type, final String... fields) {
		CouplingCriterion criterion = new CouplingCriterion();
		criterion.setCriterionType(type);
		for (String field : fields) {
			DataField dataField = dataFieldRepository.findByName(field);
			if (dataField == null) {
				log.error("DataField with name {} nod fount!", field);
				continue;
			}
			dataField.addCouplingCriterion(criterion);
			criterion.addDataField(dataField);
		}

		log.info("save coupling criterion of type {} on fiels {}", type.name(), Arrays.toString(fields));
		couplingCriterionRepository.save(criterion);
	}

}
