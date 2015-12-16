package ch.hsr.servicecutter.importer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

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

import ch.hsr.servicecutter.importer.api.Characteristic;
import ch.hsr.servicecutter.importer.api.Compatibilities;
import ch.hsr.servicecutter.importer.api.Entity;
import ch.hsr.servicecutter.importer.api.EntityRelation;
import ch.hsr.servicecutter.importer.api.EntityRelation.RelationType;
import ch.hsr.servicecutter.importer.api.EntityRelationDiagram;
import ch.hsr.servicecutter.importer.api.ImportNanoentity;
import ch.hsr.servicecutter.importer.api.RelatedGroup;
import ch.hsr.servicecutter.importer.api.UseCase;
import ch.hsr.servicecutter.importer.api.UserRepresentationContainer;
import ch.hsr.servicecutter.model.criteria.CouplingCriterion;
import ch.hsr.servicecutter.model.criteria.CouplingCriterionCharacteristic;
import ch.hsr.servicecutter.model.repository.CouplingCriterionCharacteristicRepository;
import ch.hsr.servicecutter.model.repository.CouplingCriterionRepository;
import ch.hsr.servicecutter.model.repository.CouplingInstanceRepository;
import ch.hsr.servicecutter.model.repository.NanoentityRepository;
import ch.hsr.servicecutter.model.repository.UserSystemRepository;
import ch.hsr.servicecutter.model.userdata.CouplingInstance;
import ch.hsr.servicecutter.model.userdata.InstanceType;
import ch.hsr.servicecutter.model.userdata.Nanoentity;
import ch.hsr.servicecutter.model.userdata.UserSystem;
import ch.hsr.servicecutter.rest.InvalidRestParam;

@Component
@Path("/engine/import")
public class ImportEndpoint {

	private final Logger log = LoggerFactory.getLogger(ImportEndpoint.class);
	private final UserSystemRepository userSystemRepository;
	private final NanoentityRepository nanoentityRepository;
	private final CouplingCriterionRepository couplingCriterionRepository;
	private final CouplingCriterionCharacteristicRepository couplingCriteriaCharacteristicRepository;
	private final CouplingInstanceRepository couplingInstanceRepository;
	private final UserSystemCompleter systemCompleter;
	//

	@Autowired
	public ImportEndpoint(final UserSystemRepository userSystemRepository, final NanoentityRepository nanoentityRepository,
			final CouplingInstanceRepository couplingInstanceRepository, final UserSystemCompleter modelCompleter, final CouplingCriterionRepository couplingCriterionRepository,
			final CouplingCriterionCharacteristicRepository couplingCriteriaCharacteristicRepository) {
		this.userSystemRepository = userSystemRepository;
		this.nanoentityRepository = nanoentityRepository;
		this.couplingInstanceRepository = couplingInstanceRepository;
		this.systemCompleter = modelCompleter;
		this.couplingCriterionRepository = couplingCriterionRepository;
		this.couplingCriteriaCharacteristicRepository = couplingCriteriaCharacteristicRepository;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Map<String, Object> importERD(final EntityRelationDiagram erd) {
		if (erd == null) {
			throw new InvalidRestParam();
		}
		Map<String, Object> result = new HashMap<>();

		List<String> warnings = new ArrayList<>();
		result.put("warnings", warnings);

		UserSystem system = new UserSystem();
		userSystemRepository.save(system);
		String name = erd.getName();
		if (!StringUtils.hasLength(name)) {
			name = "imported " + new Date().toString();
		}
		system.setName(name);

		Map<ImportNanoentity, String> entityAttributes = new HashMap<>();
		for (Entity entity : erd.getEntities()) {
			for (ImportNanoentity attribute : entity.getNanoentities()) {
				entityAttributes.put(attribute, entity.getName());
			}

		}

		// entities
		CouplingCriterion criterion = couplingCriterionRepository.readByName(CouplingCriterion.IDENTITY_LIFECYCLE);
		for (Entry<String, List<ImportNanoentity>> entityAndNanoentities : findRealEntities(erd).entrySet()) {
			CouplingInstance entityInstance = new CouplingInstance(criterion, InstanceType.SAME_ENTITY);
			system.addCouplingInstance(entityInstance);
			couplingInstanceRepository.save(entityInstance);
			String entityName = entityAndNanoentities.getKey();
			entityInstance.setName(entityName);
			entityInstance.setSystem(system);
			log.info("store entity with attributes {}", entityAndNanoentities.getValue());
			for (ImportNanoentity entityAttribute : entityAndNanoentities.getValue()) {
				Nanoentity nanoentity = persistNanoentity(system, entityAttributes.get(entityAttribute), entityAttribute.getName());
				entityInstance.addNanoentity(nanoentity);
				log.info("Import nanoentity {}", nanoentity.getContextName());
			}
		}

		// Aggregations
		CouplingCriterion semanticProximity = couplingCriterionRepository.readByName(CouplingCriterion.SEMANTIC_PROXIMITY);
		for (EntityRelation relation : erd.getRelations()) {
			if (RelationType.AGGREGATION.equals(relation.getType())) {
				CouplingInstance instance = new CouplingInstance(semanticProximity, InstanceType.AGGREGATION);
				couplingInstanceRepository.save(instance);
				List<Nanoentity> originNanoentities = relation.getOrigin().getNanoentities().stream()
						.map(attr -> nanoentityRepository.findByContextAndNameAndUserSystem(relation.getOrigin().getName(), attr.getName(), system)).collect(Collectors.toList());
				List<Nanoentity> destinationNanoentities = relation.getDestination().getNanoentities().stream()
						.map(attr -> nanoentityRepository.findByContextAndNameAndUserSystem(relation.getDestination().getName(), attr.getName(), system))
						.collect(Collectors.toList());
				instance.setNanoentities(originNanoentities);
				instance.setSecondNanoentities(destinationNanoentities);
				instance.setSystem(system);
				instance.setName(relation.getOrigin().getName() + "." + relation.getDestination().getName());

				log.info("Import aggregation on {} and {}", instance.getNanoentities(), instance.getSecondNanoentities());
			}
		}
		// TODO: remove return value and set location header to URL of generated
		// model
		result.put("message", "userSystem " + system.getId() + " has been created");
		result.put("id", system.getId());
		return result;
	}

	private Map<String, List<ImportNanoentity>> findRealEntities(final EntityRelationDiagram erd) {
		Map<String, List<ImportNanoentity>> realEntities = new HashMap<>();
		for (Entity entity : erd.getEntities()) {
			realEntities.put(entity.getName(), entity.getNanoentities());
		}
		List<EntityRelation> currentRelations = new ArrayList<>(erd.getRelations());

		// Merge composition and inheritance UML-entities together to real
		// entities
		// TODO throw error if Composition/inheritance relations contain cycles!
		List<EntityRelation> relationsToEdgeEntities = getRelationsToEdgeEntities(currentRelations, erd.getEntities());
		while (!relationsToEdgeEntities.isEmpty()) {
			log.info("Entity reduction iteration, reduce relations {}", relationsToEdgeEntities);
			for (EntityRelation relation : relationsToEdgeEntities) {
				if (RelationType.COMPOSITION.equals(relation.getType())) {
					List<ImportNanoentity> list = realEntities.get(relation.getOrigin().getName());
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
				relationsToEdgeEntities = getRelationsToEdgeEntities(currentRelations, erd.getEntities());
			}
		}

		return realEntities;
	}

	// returns a list of relations where the destination has no outgoing
	// COMPOSITION or INHERITANCE relations
	private List<EntityRelation> getRelationsToEdgeEntities(final List<EntityRelation> currentRelations, final List<Entity> inputEntites) {

		// get all entites that will have no other entities merged into them
		List<Entity> reducableEntities = inputEntites.stream()
				.filter(entity -> currentRelations.stream()
						.filter(r2 -> (r2.getDestination().equals(entity) && r2.getType().equals(RelationType.INHERITANCE))
								|| (r2.getOrigin().equals(entity) && r2.getType().equals(RelationType.COMPOSITION)))
						.collect(Collectors.toList()).isEmpty())
				.collect(Collectors.toList());

		// get all relations that will merge the reducableEntities into
		// another
		// entity
		List<EntityRelation> relationsToEdgeEntities = currentRelations.stream()
				.filter(r -> (reducableEntities.contains(r.getOrigin()) && r.getType().equals(RelationType.INHERITANCE))
						|| (reducableEntities.contains(r.getDestination()) && r.getType().equals(RelationType.COMPOSITION)))
				.collect(Collectors.toList());
		return relationsToEdgeEntities;
	}

	@POST
	@Path("/{modelId}/userrepresentations/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Map<String, Object> importUserRepresentations(@PathParam("modelId") final Long modelId, final UserRepresentationContainer userRepresentations) {
		Map<String, Object> result = new HashMap<>();
		List<String> warnings = new ArrayList<>();
		result.put("warnings", warnings);

		UserSystem system = userSystemRepository.findOne(modelId);
		if (system == null) {
			warnings.add("system not defined!");
			return result;
		}

		// TODO create better check if userRepts are already loaded
		long count = couplingInstanceRepository.findByUserSystem(system).stream().filter(i -> !i.getCouplingCriterion().getName().equals(CouplingCriterion.SEMANTIC_PROXIMITY)
				&& !i.getCouplingCriterion().getName().equals(CouplingCriterion.IDENTITY_LIFECYCLE)).count();
		if (count != 0) {
			warnings.add("Enhancing an already specified system is not yet implemented!");
			return result;
		}

		persistUseCases(system, userRepresentations.getUseCases(), warnings);
		Compatibilities compatibilities = userRepresentations.getCompatibilities();
		if (compatibilities != null) {
			persistCharacteristics(system, compatibilities.getStructuralVolatility(), CouplingCriterion.STRUCTURAL_VOLATILITY, warnings);
			persistCharacteristics(system, compatibilities.getConsistency(), CouplingCriterion.CONSISTENCY, warnings);
			persistCharacteristics(system, compatibilities.getSecurityCriticality(), CouplingCriterion.SECURITY_CRITICALITY, warnings);
			persistCharacteristics(system, compatibilities.getStorageSimilarity(), CouplingCriterion.STORAGE_SIMILARITY, warnings);
			persistCharacteristics(system, compatibilities.getContentVolatility(), CouplingCriterion.CONTENT_VOLATILITY, warnings);
			persistCharacteristics(system, compatibilities.getAvailability(), CouplingCriterion.AVAILABILITY, warnings);
		}
		persistRelatedGroups(system, userRepresentations.getAggregates(), CouplingCriterion.CONSISTENCY_CONSTRAINT, warnings);
		persistRelatedGroups(system, userRepresentations.getEntities(), CouplingCriterion.IDENTITY_LIFECYCLE, warnings);
		persistRelatedGroups(system, userRepresentations.getPredefinedServices(), CouplingCriterion.PREDEFINED_SERVICE, warnings);
		persistRelatedGroups(system, userRepresentations.getSecurityAccessGroups(), CouplingCriterion.SECURITY_CONTEXUALITY, warnings);
		persistRelatedGroups(system, userRepresentations.getSeparatedSecurityZones(), CouplingCriterion.SECURITY_CONSTRAINT, warnings);
		persistRelatedGroups(system, userRepresentations.getSharedOwnerGroups(), CouplingCriterion.SHARED_OWNER, warnings);

		log.info("Imported user representations");
		return result;
	}

	private void persistUseCases(final UserSystem system, final List<UseCase> useCases, final List<String> warnings) {
		CouplingCriterion semanticProximity = couplingCriterionRepository.readByName(CouplingCriterion.SEMANTIC_PROXIMITY);
		for (UseCase usecase : useCases) {
			InstanceType type;
			type = usecase.isLatencyCritical() ? InstanceType.LATENCY_USE_CASE : InstanceType.USE_CASE;
			CouplingInstance instance = new CouplingInstance(semanticProximity, type);
			system.addCouplingInstance(instance);
			couplingInstanceRepository.save(instance);
			instance.setName(usecase.getName());
			instance.setSystem(system);
			instance.setNanoentities(loadNanoentities(usecase.getNanoentitiesRead(), system, warnings));
			instance.setSecondNanoentities(loadNanoentities(usecase.getNanoentitiesWritten(), system, warnings));
			log.info("Import use cases {} with fields written {} and fields read {}", usecase.getName(), usecase.getNanoentitiesWritten(), usecase.getNanoentitiesRead());
		}
	}

	@POST
	@Path("/{modelId}/nanoentities/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void importNanoentities(@PathParam("modelId") final Long modelId, final List<ImportNanoentity> nanoentities) {
		UserSystem system = userSystemRepository.findOne(modelId);
		if (system == null) {
			throw new InvalidRestParam();
		}
		nanoentities.forEach((nanoentity) -> {
			persistNanoentity(system, nanoentity.getContext(), nanoentity.getName());
		});
	}

	private Nanoentity persistNanoentity(final UserSystem system, final String context, final String name) {
		Nanoentity nanoentity = new Nanoentity();
		nanoentity.setName(name);
		nanoentity.setContext(context);
		system.addNanoentity(nanoentity);
		nanoentityRepository.save(nanoentity);
		return nanoentity;
	}

	private void persistCharacteristics(final UserSystem system, final List<Characteristic> characteristics, final String criterionName, final List<String> warnings) {
		if (characteristics == null || characteristics.isEmpty()) {
			return;
		}
		for (Characteristic inputCharacteristic : characteristics) {
			CouplingCriterionCharacteristic characteristic = findCharacteristic(criterionName, inputCharacteristic.getCharacteristic());
			if (characteristic == null) {
				log.error("characteristic {} not known! ignoring...", inputCharacteristic);
				warnings.add("characteristic " + inputCharacteristic + " not known!");
				continue;
			}
			Set<CouplingInstance> instance = couplingInstanceRepository.findByUserSystemAndCharacteristic(system, characteristic);

			if (instance == null || instance.isEmpty()) {
				CouplingInstance newInstance = new CouplingInstance(characteristic, InstanceType.CHARACTERISTIC);
				system.addCouplingInstance(newInstance);
				couplingInstanceRepository.save(newInstance);
				newInstance.setName(criterionName);
				newInstance.setSystem(system);
				newInstance.setNanoentities(loadNanoentities(inputCharacteristic.getNanoentities(), system, warnings));
				log.info("Import distance characteristic {}-{} with nanoentities {}", criterionName, inputCharacteristic.getCharacteristic(), newInstance.getAllNanoentities());
			} else {
				log.error("enhancing characteristics not yet implemented. criterion: {}, characteristic: {}", criterionName, inputCharacteristic.getCharacteristic());
			}
		}
		systemCompleter.completeSystemWithDefaultsForDistance(system);
	}

	private void persistRelatedGroups(final UserSystem system, final List<RelatedGroup> listOfGroups, final String couplingCriterionName, final List<String> warnings) {
		if (listOfGroups == null || listOfGroups.isEmpty()) {
			return;
		}
		CouplingCriterion couplingCriterion = couplingCriterionRepository.readByName(couplingCriterionName);
		for (RelatedGroup relatedGroup : listOfGroups) {
			CouplingInstance instance = new CouplingInstance(couplingCriterion, InstanceType.RELATED_GROUP);
			system.addCouplingInstance(instance);
			couplingInstanceRepository.save(instance);
			String name = relatedGroup.getName();
			instance.setName(StringUtils.hasLength(name) ? name : couplingCriterionName);
			instance.setSystem(system);
			instance.setNanoentities(loadNanoentities(relatedGroup.getNanoentities(), system, warnings));
			log.info("Import related group {} on nanoentities {} ", name, instance.getNanoentities());
		}
	}

	private List<Nanoentity> loadNanoentities(final List<String> names, final UserSystem system, final List<String> warnings) {
		List<Nanoentity> nanoentities = new ArrayList<>();
		for (String nanoentityName : names) {
			Nanoentity nanoentity;
			if (nanoentityName.contains(".")) {
				String[] splittedName = nanoentityName.split("\\.");
				nanoentity = nanoentityRepository.findByContextAndNameAndUserSystem(splittedName[0], splittedName[1], system);
			} else {
				nanoentity = nanoentityRepository.findByNameAndUserSystem(nanoentityName, system);
			}

			if (nanoentity != null) {
				nanoentities.add(nanoentity);
			} else {
				log.warn("nanoentity with name {} not known!", nanoentityName);
				warnings.add("nanoentity with name " + nanoentityName + " not known!");
			}
		}
		return nanoentities;
	}

	private CouplingCriterionCharacteristic findCharacteristic(final String coupling, final String characteristic) {
		CouplingCriterion couplingCriterion = couplingCriterionRepository.readByName(coupling);
		CouplingCriterionCharacteristic result = couplingCriteriaCharacteristicRepository.readByNameAndCouplingCriterion(characteristic, couplingCriterion);
		return result;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("//{id}/couplingcriteria")
	@Transactional
	public List<CouplingInstance> getSystemCoupling(@PathParam("id") final Long id) {
		List<CouplingInstance> result = new ArrayList<>();
		UserSystem system = userSystemRepository.findOne(id);
		Set<CouplingInstance> instances = couplingInstanceRepository.findByUserSystem(system);
		result.addAll(instances);
		for (CouplingInstance couplingInstance : instances) {
			// init lazy collection, otherwise you'll get a serialization
			// exception as the transaction is already closed
			couplingInstance.getAllNanoentities().size();
		}
		log.debug("return criteria for model {}: {}", system.getName(), result.toString());
		Collections.sort(result);
		return result;
	}

}
