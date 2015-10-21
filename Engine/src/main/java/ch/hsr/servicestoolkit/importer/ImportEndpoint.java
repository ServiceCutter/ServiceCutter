package ch.hsr.servicestoolkit.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.CouplingCriterionFactory;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.DualCouplingInstance;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.repository.CouplingCriterionRepository;
import ch.hsr.servicestoolkit.repository.DataFieldRepository;
import ch.hsr.servicestoolkit.repository.ModelRepository;
import ch.hsr.servicestoolkit.repository.MonoCouplingInstanceRepository;

@Component
@Path("/engine/import")
public class ImportEndpoint {

	private final Logger log = LoggerFactory.getLogger(ImportEndpoint.class);
	private final ModelRepository modelRepository;
	private final CouplingCriterionRepository couplingCriterionRepository;
	private DataFieldRepository dataFieldRepository;
	private CouplingCriterionFactory couplingCriterionFactory;
	private MonoCouplingInstanceRepository monoCouplingInstanceRepository;

	@Autowired
	public ImportEndpoint(final ModelRepository modelRepository, final CouplingCriterionRepository couplingCriterionRepository, final DataFieldRepository dataFieldRepository,
			CouplingCriterionFactory couplingCriterionFactory, MonoCouplingInstanceRepository monoCouplingInstanceRepository) {
		this.modelRepository = modelRepository;
		this.couplingCriterionRepository = couplingCriterionRepository;
		this.dataFieldRepository = dataFieldRepository;
		this.couplingCriterionFactory = couplingCriterionFactory;
		this.monoCouplingInstanceRepository = monoCouplingInstanceRepository;
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
		modelRepository.save(model);
		model.setName("imported " + new Date().toString());
		Map<EntityModel, List<DataField>> fieldsByModel = new HashMap<>();

		CouplingCriteriaVariant sameEntityVariant = couplingCriterionFactory.findOrCreateVariant("Identity & Lifecycle", CouplingCriteriaVariant.SAME_ENTITY);

		for (EntityModel entityModel : domainModel.getEntities()) {
			MonoCouplingInstance couplingInstance = sameEntityVariant.createInstance();
			monoCouplingInstanceRepository.save(couplingInstance);
			String entityName = entityModel.getName();
			couplingInstance.setName(entityName);
			for (EntityAttribute entityAttribute : entityModel.getAttributes()) {
				DataField dataField = new DataField();
				dataField.setName(entityAttribute.getName());
				dataField.setContext(entityName);
				model.addDataField(dataField);
				dataFieldRepository.save(dataField);
				couplingInstance.addDataField(dataField);
			}
			fieldsByModel.put(entityModel, couplingInstance.getDataFields());
		}

		CouplingCriteriaVariant aggregationVariant = couplingCriterionFactory.findOrCreateVariant("Identity & Lifecycle", CouplingCriteriaVariant.AGGREGATION, false);
		CouplingCriteriaVariant compositionVariant = couplingCriterionFactory.findOrCreateVariant("Identity & Lifecycle", CouplingCriteriaVariant.COMPOSITION, false);
		CouplingCriteriaVariant inheritanceVariant = couplingCriterionFactory.findOrCreateVariant("Identity & Lifecycle", CouplingCriteriaVariant.INHERITANCE, false);

		for (EntityRelation relation : domainModel.getRelations()) {
			DualCouplingInstance instance = null;
			if (RelationType.AGGREGATION.equals(relation.getType())) {
				instance = (DualCouplingInstance) aggregationVariant.createInstance();
			} else if (RelationType.COMPOSITION.equals(relation.getType())) {
				instance = (DualCouplingInstance) compositionVariant.createInstance();
			} else {
				instance = (DualCouplingInstance) inheritanceVariant.createInstance();
			}
			monoCouplingInstanceRepository.save(instance);
			instance.setDataFields(fieldsByModel.get(relation.getOrigin()));
			instance.setSecondDataFields(fieldsByModel.get(relation.getDestination()));
			instance.setName(relation.getOrigin().getName() + "." + relation.getDestination().getName());
		}
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
		// TODO add support for read/write/mixed
		CouplingCriteriaVariant aggregationVariant = couplingCriterionFactory.findOrCreateVariant("Business Transaction", "Mixed", true);
		for (BusinessTransaction transaction : transactions) {
			MonoCouplingInstance instance = aggregationVariant.createInstance();
			monoCouplingInstanceRepository.save(instance);
			instance.setName(transaction.getName());
			Set<DataField> dataFields = new HashSet<>();
			dataFields.addAll(loadDataFields(transaction.getFieldsRead()));
			dataFields.addAll(loadDataFields(transaction.getFieldsWritten()));
			instance.setDataFields(dataFields);
		}

	}

	List<DataField> loadDataFields(List<String> fields) {
		List<DataField> dataFields = new ArrayList<>();
		for (String fieldName : fields) {
			DataField dataField = dataFieldRepository.findByName(fieldName);
			if (dataField != null) {
				dataFields.add(dataField);
			} else {
				log.warn("Ignoring field with name {}", fieldName);
			}
		}
		return dataFields;
	}

}
