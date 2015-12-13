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
import org.springframework.util.Assert;

import ch.hsr.servicestoolkit.importer.api.CohesiveGroup;
import ch.hsr.servicestoolkit.importer.api.CohesiveGroups;
import ch.hsr.servicestoolkit.importer.api.DistanceVariant;
import ch.hsr.servicestoolkit.importer.api.DomainModel;
import ch.hsr.servicestoolkit.importer.api.Entity;
import ch.hsr.servicestoolkit.importer.api.EntityRelation;
import ch.hsr.servicestoolkit.importer.api.EntityRelation.RelationType;
import ch.hsr.servicestoolkit.importer.api.NanoEntityInput;
import ch.hsr.servicestoolkit.importer.api.SeparationCriterion;
import ch.hsr.servicestoolkit.importer.api.UseCase;
import ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic;
import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.DualCouplingInstance;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.model.NanoEntity;
import ch.hsr.servicestoolkit.model.repository.CouplingCriteriaVariantRepository;
import ch.hsr.servicestoolkit.model.repository.CouplingCriterionRepository;
import ch.hsr.servicestoolkit.model.repository.DataFieldRepository;
import ch.hsr.servicestoolkit.model.repository.ModelRepository;
import ch.hsr.servicestoolkit.model.repository.MonoCouplingInstanceRepository;
import ch.hsr.servicestoolkit.rest.InvalidRestParam;

@Component
@Path("/engine/import")
public class ImportEndpoint {

	private final Logger log = LoggerFactory.getLogger(ImportEndpoint.class);
	private final ModelRepository modelRepository;
	private final DataFieldRepository dataFieldRepository;
	private final CouplingCriterionRepository couplingCriterionRepository;
	private final CouplingCriteriaVariantRepository couplingCriteriaVariantRepository;
	private final MonoCouplingInstanceRepository monoCouplingInstanceRepository;
	private ModelCompleter modelCompleter;

	@Autowired
	public ImportEndpoint(final ModelRepository modelRepository, final DataFieldRepository dataFieldRepository, final MonoCouplingInstanceRepository monoCouplingInstanceRepository,
			final ModelCompleter modelCompleter, CouplingCriterionRepository couplingCriterionRepository, CouplingCriteriaVariantRepository couplingCriteriaVariantRepository) {
		this.modelRepository = modelRepository;
		this.dataFieldRepository = dataFieldRepository;
		this.monoCouplingInstanceRepository = monoCouplingInstanceRepository;
		this.modelCompleter = modelCompleter;
		this.couplingCriterionRepository = couplingCriterionRepository;
		this.couplingCriteriaVariantRepository = couplingCriteriaVariantRepository;

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

		Map<NanoEntityInput, String> entityAttributes = new HashMap<>();
		for (Entity entity : domainModel.getEntities()) {
			for (NanoEntityInput attribute : entity.getNanoentities()) {
				entityAttributes.put(attribute, entity.getName());
			}

		}
		// entities
		CouplingCriterionCharacteristic sameEntityVariant = findVariant(CouplingCriterion.IDENTITY_LIFECYCLE, CouplingCriterionCharacteristic.SAME_ENTITY);
		for (Entry<String, List<NanoEntityInput>> entity : findRealEntities(domainModel).entrySet()) {
			MonoCouplingInstance couplingInstance = sameEntityVariant.createInstance();
			monoCouplingInstanceRepository.save(couplingInstance);
			String entityName = entity.getKey();
			couplingInstance.setName(entityName);
			couplingInstance.setModel(model);
			log.info("store entity with attributes {}", entity.getValue());
			for (NanoEntityInput entityAttribute : entity.getValue()) {
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
		CouplingCriterionCharacteristic aggregationVariant = findVariant(CouplingCriterion.SEMANTIC_PROXIMITY, CouplingCriterionCharacteristic.AGGREGATION);
		for (EntityRelation relation : domainModel.getRelations()) {
			if (RelationType.AGGREGATION.equals(relation.getType())) {
				DualCouplingInstance instance = (DualCouplingInstance) aggregationVariant.createInstance();

				monoCouplingInstanceRepository.save(instance);
				List<NanoEntity> originFields = relation.getOrigin().getNanoentities().stream()
						.map(attr -> dataFieldRepository.findByContextAndNameAndModel(relation.getOrigin().getName(), attr.getName(), model)).collect(Collectors.toList());
				List<NanoEntity> destinationFields = relation.getDestination().getNanoentities().stream()
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

	private Map<String, List<NanoEntityInput>> findRealEntities(final DomainModel domainModel) {
		Map<String, List<NanoEntityInput>> realEntities = new HashMap<>();
		for (Entity entity : domainModel.getEntities()) {
			realEntities.put(entity.getName(), entity.getNanoentities());
		}
		List<EntityRelation> currentRelations = new ArrayList<>(domainModel.getRelations());

		// Merge composition and inheritance UML-entities together to real
		// entities
		// TODO throw error if Composition/inheritance relations contain cycles!
		List<EntityRelation> relationsToEdgeEntities = getRelationsToEdgeEntities(currentRelations, domainModel.getEntities());
		while (!relationsToEdgeEntities.isEmpty()) {
			log.info("Entity reduction iteration, reduce relations {}", relationsToEdgeEntities);
			for (EntityRelation relation : relationsToEdgeEntities) {
				if (RelationType.COMPOSITION.equals(relation.getType())) {
					List<NanoEntityInput> list = realEntities.get(relation.getOrigin().getName());
					if (list != null) {
						list.addAll(relation.getDestination().getNanoentities());
					} else {
						log.error("ignore relation {}", relation);
					}
					realEntities.remove(relation.getDestination().getName());
				} else if (RelationType.INHERITANCE.equals(relation.getType())) {
					realEntities.get(relation.getDestination().getName()).addAll(relation.getOrigin().getNanoentities());
					realEntities.remove(relation.getOrigin().getName());
				}

				currentRelations.remove(relation);
				relationsToEdgeEntities = getRelationsToEdgeEntities(currentRelations, domainModel.getEntities());
			}
		}

		return realEntities;
	}

	// returns a list of relations where the destination has no outgoing
	// COMPOSITION or INHERITANCE relations
	private List<EntityRelation> getRelationsToEdgeEntities(final List<EntityRelation> currentRelations, final List<Entity> inputEntites) {

		// get all entites that will have no other entities merged into them
		List<Entity> reducableEntities = inputEntites.stream()
				.filter(entity -> currentRelations.stream().filter(
						r2 -> (r2.getDestination().equals(entity) && r2.getType().equals(RelationType.INHERITANCE)) || (r2.getOrigin().equals(entity) && r2.getType().equals(RelationType.COMPOSITION)))
				.collect(Collectors.toList()).isEmpty()).collect(Collectors.toList());

		// get all relations that will merge the reducableEntities into another
		// entity
		List<EntityRelation> relationsToEdgeEntities = currentRelations.stream().filter(r -> (reducableEntities.contains(r.getOrigin()) && r.getType().equals(RelationType.INHERITANCE))
				|| (reducableEntities.contains(r.getDestination()) && r.getType().equals(RelationType.COMPOSITION))).collect(Collectors.toList());

		return relationsToEdgeEntities;
	}

	@POST
	@Path("/{modelId}/businessTransactions/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void importBusinessTransaction(@PathParam("modelId") final Long modelId, final List<UseCase> transactions) {
		Model model = modelRepository.findOne(modelId);
		if (model == null) {
			throw new InvalidRestParam();
		}
		CouplingCriterionCharacteristic aggregationVariant = findVariant(CouplingCriterion.SEMANTIC_PROXIMITY, CouplingCriterionCharacteristic.SHARED_FIELD_ACCESS);
		for (UseCase transaction : transactions) {
			DualCouplingInstance instance = (DualCouplingInstance) aggregationVariant.createInstance();
			monoCouplingInstanceRepository.save(instance);
			instance.setName(transaction.getName());
			instance.setModel(model);
			instance.setDataFields(loadDataFields(transaction.getNanoentitiesRead(), model));
			instance.setSecondDataFields(loadDataFields(transaction.getNanoentitiesWritten(), model));
			log.info("Import business transactions {} with fieldsWritten {} and fieldsRead {}", transaction.getName(), transaction.getNanoentitiesWritten(), transaction.getNanoentitiesRead());
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
		CouplingCriterionCharacteristic variant = findVariant(CouplingCriterion.IDENTITY_LIFECYCLE, CouplingCriterionCharacteristic.SAME_ENTITY);
		for (Entity entity : entities) {
			MonoCouplingInstance instance = variant.createInstance();
			monoCouplingInstanceRepository.save(instance);
			instance.setName(entity.getName());
			instance.setModel(model);
			instance.setDataFields(loadDataFields(entity.getNanoentities().stream().map(NanoEntityInput::getName).collect(Collectors.toList()), model));
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
			CouplingCriterionCharacteristic variant = findVariant(inputVariant.getCouplingCriterionName(), inputVariant.getVariantName());
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
				newInstance.setDataFields(loadDataFields(inputVariant.getNanoentities(), model));
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
			CouplingCriterionCharacteristic variant = findVariant(inputCriterion.getCouplingCriterionName(), inputCriterion.getVariantName());
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
				newInstance.setDataFields(loadDataFields(inputCriterion.getGroupAnanoentities(), model));
				newInstance.setSecondDataFields(loadDataFields(inputCriterion.getGroupBnanoentities(), model));
				log.info("Import separation constraint {} on fields {} and {}", inputCriterion.getCouplingCriterionName(), newInstance.getDataFields(), newInstance.getSecondDataFields());
			} else {
				log.error("enhancing variants not yet implemented. criterion: {}, variant: {}", inputCriterion.getCouplingCriterionName(), inputCriterion.getVariantName());
				throw new InvalidRestParam();
			}
		}
	}

	@POST
	@Path("/{modelId}/cohesiveGroups/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void importCohesiveGroups(@PathParam("modelId") final Long modelId, final List<CohesiveGroups> listOfGroups) {
		Model model = modelRepository.findOne(modelId);
		if (model == null || listOfGroups == null) {
			throw new InvalidRestParam();
		}

		for (CohesiveGroups groups : listOfGroups) {
			CouplingCriterionCharacteristic variant = findVariant(groups.getCouplingCriterionName(), groups.getVariantName());
			if (variant == null) {
				log.error("variant {} not known! ignore", groups.getVariantName());
				continue;
			}

			for (CohesiveGroup cohesiveGroup : groups.getCohesiveGroups()) {
				MonoCouplingInstance instance = variant.createInstance();

				monoCouplingInstanceRepository.save(instance);
				instance.setName(groups.getCouplingCriterionName());
				instance.setModel(model);
				instance.setDataFields(loadDataFields(cohesiveGroup.getNanoentities(), model));
				log.info("Import cohesive group {} on fields {} ", cohesiveGroup.getName(), instance.getDataFields());
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
				log.warn("	 field with name {}", fieldName);
			}
		}
		return dataFields;
	}

	private CouplingCriterionCharacteristic findVariant(String coupling, String variant) {
		CouplingCriterion couplingCriterion = couplingCriterionRepository.readByName(coupling);
		Assert.notNull(couplingCriterion, "Coupling with name " + coupling + " not found!");
		CouplingCriterionCharacteristic result = couplingCriteriaVariantRepository.readByNameAndCouplingCriterion(variant, couplingCriterion);
		Assert.notNull(result, "Variant with name " + variant + " not found!");
		return result;
	}
}
