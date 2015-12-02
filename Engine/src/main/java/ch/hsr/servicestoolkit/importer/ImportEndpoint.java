package ch.hsr.servicestoolkit.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

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
import ch.hsr.servicestoolkit.importer.api.DistanceVariant;
import ch.hsr.servicestoolkit.importer.api.DomainModel;
import ch.hsr.servicestoolkit.importer.api.Entity;
import ch.hsr.servicestoolkit.importer.api.EntityAttribute;
import ch.hsr.servicestoolkit.importer.api.EntityRelation;
import ch.hsr.servicestoolkit.importer.api.EntityRelation.RelationType;
import ch.hsr.servicestoolkit.importer.api.SeparationCriterion;
import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingCriterionFactory;
import ch.hsr.servicestoolkit.model.DualCouplingInstance;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.model.NanoEntity;
import ch.hsr.servicestoolkit.repository.DataFieldRepository;
import ch.hsr.servicestoolkit.repository.ModelRepository;
import ch.hsr.servicestoolkit.repository.MonoCouplingInstanceRepository;

@Component
@Path("/engine/import")
public class ImportEndpoint {

	private final Logger log = LoggerFactory.getLogger(ImportEndpoint.class);
	private final ModelRepository modelRepository;
	private DataFieldRepository dataFieldRepository;
	private CouplingCriterionFactory couplingCriterionFactory;
	private MonoCouplingInstanceRepository monoCouplingInstanceRepository;
	private ModelCompleter modelCompleter;

	@Autowired
	public ImportEndpoint(final ModelRepository modelRepository, final DataFieldRepository dataFieldRepository, final CouplingCriterionFactory couplingCriterionFactory,
			final MonoCouplingInstanceRepository monoCouplingInstanceRepository, final ModelCompleter modelCompleter) {
		this.modelRepository = modelRepository;
		this.dataFieldRepository = dataFieldRepository;
		this.couplingCriterionFactory = couplingCriterionFactory;
		this.monoCouplingInstanceRepository = monoCouplingInstanceRepository;
		this.modelCompleter = modelCompleter;

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

		Map<EntityAttribute, String> entityAttributes = new HashMap<>();
		for (Entity entity : domainModel.getEntities()) {
			for (EntityAttribute attribute : entity.getAttributes()) {
				entityAttributes.put(attribute, entity.getName());
			}

		}
		// entities
		CouplingCriteriaVariant sameEntityVariant = couplingCriterionFactory.findVariant(CouplingCriterion.IDENTITY_LIFECYCLE, CouplingCriteriaVariant.SAME_ENTITY);
		for (Entry<String, List<EntityAttribute>> entity : findRealEntities(domainModel).entrySet()) {
			MonoCouplingInstance couplingInstance = sameEntityVariant.createInstance();
			monoCouplingInstanceRepository.save(couplingInstance);
			String entityName = entity.getKey();
			couplingInstance.setName(entityName);
			couplingInstance.setModel(model);
			for (EntityAttribute entityAttribute : entity.getValue()) {
				NanoEntity dataField = new NanoEntity();
				dataField.setName(entityAttribute.getName());
				dataField.setContext(entityAttributes.get(entityAttribute));
				model.addDataField(dataField);
				dataFieldRepository.save(dataField);
				couplingInstance.addDataField(dataField);
				log.info("Import data field {}", dataField.getContextName());
			}
		}

		// Aggregations
		CouplingCriteriaVariant aggregationVariant = couplingCriterionFactory.findVariant(CouplingCriterion.SEMANTIC_PROXIMITY, CouplingCriteriaVariant.AGGREGATION);
		for (EntityRelation relation : domainModel.getRelations()) {
			if (RelationType.AGGREGATION.equals(relation.getType())) {
				DualCouplingInstance instance = (DualCouplingInstance) aggregationVariant.createInstance();

				monoCouplingInstanceRepository.save(instance);
				List<NanoEntity> originFields = relation.getOrigin().getAttributes().stream()
						.map(attr -> dataFieldRepository.findByContextAndNameAndModel(relation.getOrigin().getName(), attr.getName(), model)).collect(Collectors.toList());
				List<NanoEntity> destinationFields = relation.getDestination().getAttributes().stream()
						.map(attr -> dataFieldRepository.findByContextAndNameAndModel(relation.getDestination().getName(), attr.getName(), model)).collect(Collectors.toList());
				instance.setDataFields(originFields);
				instance.setSecondDataFields(destinationFields);
				instance.setModel(model);
				instance.setName(relation.getOrigin().getName() + "." + relation.getDestination().getName());

				log.info("Import aggregation on {} and {}", instance.getDataFields(), instance.getSecondDataFields());
			}
		}
		// TODO: remove return value and set location header to URL of generated
		// model
		Map<String, Object> result = new HashMap<>();
		result.put("message", "model " + model.getId() + " has been created");
		result.put("id", model.getId());
		return result;
	}

	private Map<String, List<EntityAttribute>> findRealEntities(final DomainModel domainModel) {
		Map<String, List<EntityAttribute>> realEntities = new HashMap<>();
		for (Entity entity : domainModel.getEntities()) {
			realEntities.put(entity.getName(), entity.getAttributes());
		}

		// Merge composition and inheritance UML-entities together to real
		// entities
		// TODO support second grade compositions (A->B->C)
		for (EntityRelation relation : domainModel.getRelations()) {
			if (RelationType.COMPOSITION.equals(relation.getType())) {
				List<EntityAttribute> list = realEntities.get(relation.getOrigin().getName());
				if (list != null) {
					list.addAll(relation.getDestination().getAttributes());
				} else {
					log.error("ignore relation {}", relation);
				}
				realEntities.remove(relation.getDestination().getName());
			} else if (RelationType.INHERITANCE.equals(relation.getType())) {
				realEntities.get(relation.getDestination().getName()).addAll(relation.getOrigin().getAttributes());
				realEntities.remove(relation.getOrigin().getName());
			}
		}

		return realEntities;
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
		CouplingCriteriaVariant aggregationVariant = couplingCriterionFactory.findVariant(CouplingCriterion.SEMANTIC_PROXIMITY, CouplingCriteriaVariant.SHARED_FIELD_ACCESS);
		for (BusinessTransaction transaction : transactions) {
			DualCouplingInstance instance = (DualCouplingInstance) aggregationVariant.createInstance();
			monoCouplingInstanceRepository.save(instance);
			instance.setName(transaction.getName());
			instance.setModel(model);
			instance.setFrequency(transaction.getFrequency());
			instance.setDataFields(loadDataFields(transaction.getFieldsRead(), model));
			instance.setSecondDataFields(loadDataFields(transaction.getFieldsWritten(), model));
			log.info("Import business transactions {} with frequency {}, fieldsWritten {} and fieldsRead {}", transaction.getName(), transaction.getFrequency(),
					transaction.getFieldsWritten(), transaction.getFieldsRead());
		}
	}

	@POST
	@Path("/{modelId}/entities/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void importEntities(@PathParam("modelId") final Long modelId, final List<Entity> entities) {
		Model model = modelRepository.findOne(modelId);
		if (model == null) {
			throw new InvalidRestParam();
		}
		CouplingCriteriaVariant variant = couplingCriterionFactory.findVariant(CouplingCriterion.IDENTITY_LIFECYCLE, CouplingCriteriaVariant.SAME_ENTITY);
		for (Entity entity : entities) {
			MonoCouplingInstance instance = variant.createInstance();
			monoCouplingInstanceRepository.save(instance);
			instance.setName(entity.getName());
			instance.setModel(model);
			instance.setDataFields(loadDataFields(entity.getAttributes().stream().map(EntityAttribute::getName).collect(Collectors.toList()), model));
		}
	}

	@POST
	@Path("/{modelId}/distanceVariants/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void importDistanceCriterionInstance(@PathParam("modelId") final Long modelId, final List<DistanceVariant> inputVariants) {
		Model model = modelRepository.findOne(modelId);
		if (model == null || inputVariants == null) {
			throw new InvalidRestParam();
		}
		for (DistanceVariant inputVariant : inputVariants) {
			CouplingCriteriaVariant variant = couplingCriterionFactory.findVariant(inputVariant.getCouplingCriterionName(), inputVariant.getVariantName());
			if (variant == null) {
				log.error("variant {} not known! ignore", inputVariant);
				continue;
			}
			Set<MonoCouplingInstance> instance = monoCouplingInstanceRepository.findByModelAndVariant(model, variant);

			if (instance == null || instance.isEmpty()) {
				MonoCouplingInstance newInstance = variant.createInstance();
				monoCouplingInstanceRepository.save(newInstance);
				newInstance.setName(inputVariant.getCouplingCriterionName());
				newInstance.setModel(model);
				newInstance.setDataFields(loadDataFields(inputVariant.getFields(), model));
				log.info("Import distance variant {}-{} with fields {}", inputVariant.getCouplingCriterionName(), inputVariant.getVariantName(), newInstance.getAllFields());
			} else {
				log.error("enhancing variants not yet implemented. criterion: {}, variant: {}", inputVariant.getCouplingCriterionName(), inputVariant.getVariantName());
				throw new InvalidRestParam();
			}
		}
		modelCompleter.completeModelWithDefaultsForDistance(model);
	}

	@POST
	@Path("/{modelId}/separationCriteria/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void importSeparationCriterionInstance(@PathParam("modelId") final Long modelId, final List<SeparationCriterion> criteria) {
		Model model = modelRepository.findOne(modelId);
		if (model == null || criteria == null) {
			throw new InvalidRestParam();
		}
		for (SeparationCriterion inputCriterion : criteria) {
			CouplingCriteriaVariant variant = couplingCriterionFactory.findVariant(inputCriterion.getCouplingCriterionName(), inputCriterion.getVariantName());
			if (variant == null) {
				log.error("variant {} not known! ignore", inputCriterion);
				continue;
			}
			Set<MonoCouplingInstance> instance = monoCouplingInstanceRepository.findByModelAndVariant(model, variant);

			if (instance == null || instance.isEmpty()) {
				DualCouplingInstance newInstance = (DualCouplingInstance) variant.createInstance();

				monoCouplingInstanceRepository.save(newInstance);
				newInstance.setName(inputCriterion.getCouplingCriterionName());
				newInstance.setModel(model);
				newInstance.setDataFields(loadDataFields(inputCriterion.getGroupAFields(), model));
				newInstance.setSecondDataFields(loadDataFields(inputCriterion.getGroupBFields(), model));
				log.info("Import separation constraint {} on fields {} and {}", inputCriterion.getCouplingCriterionName(), newInstance.getDataFields(),
						newInstance.getSecondDataFields());
			} else {
				log.error("enhancing variants not yet implemented. criterion: {}, variant: {}", inputCriterion.getCouplingCriterionName(), inputCriterion.getVariantName());
				throw new InvalidRestParam();
			}
		}
	}

	List<NanoEntity> loadDataFields(final List<String> fields, final Model model) {
		List<NanoEntity> dataFields = new ArrayList<>();
		for (String fieldName : fields) {
			NanoEntity dataField;
			if (fieldName.contains(".")) {
				String[] splittedName = fieldName.split("\\.");
				dataField = dataFieldRepository.findByContextAndNameAndModel(splittedName[0], splittedName[1], model);
			} else {
				dataField = dataFieldRepository.findByNameAndModel(fieldName, model);
			}

			if (dataField != null) {
				dataFields.add(dataField);
			} else {
				log.warn("Ignoring field with name {}", fieldName);
			}
		}
		return dataFields;
	}

}
