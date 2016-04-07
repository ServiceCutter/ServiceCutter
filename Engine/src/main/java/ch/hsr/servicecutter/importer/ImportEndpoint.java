package ch.hsr.servicecutter.importer;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
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
import org.springframework.util.StringUtils;

import ch.hsr.servicecutter.importer.api.Characteristic;
import ch.hsr.servicecutter.importer.api.Compatibilities;
import ch.hsr.servicecutter.importer.api.Entity;
import ch.hsr.servicecutter.importer.api.EntityRelation;
import ch.hsr.servicecutter.importer.api.EntityRelation.RelationType;
import ch.hsr.servicecutter.importer.api.EntityRelationDiagram;
import ch.hsr.servicecutter.importer.api.NanoentitiesImport;
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
import ch.hsr.servicecutter.model.usersystem.CouplingInstance;
import ch.hsr.servicecutter.model.usersystem.InstanceType;
import ch.hsr.servicecutter.model.usersystem.Nanoentity;
import ch.hsr.servicecutter.model.usersystem.UserSystem;
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
	public ImportEndpoint(final UserSystemRepository userSystemRepository, final NanoentityRepository nanoentityRepository, final CouplingInstanceRepository couplingInstanceRepository,
			final UserSystemCompleter systemCompleter, final CouplingCriterionRepository couplingCriterionRepository,
			final CouplingCriterionCharacteristicRepository couplingCriteriaCharacteristicRepository) {
		this.userSystemRepository = userSystemRepository;
		this.nanoentityRepository = nanoentityRepository;
		this.couplingInstanceRepository = couplingInstanceRepository;
		this.systemCompleter = systemCompleter;
		this.couplingCriterionRepository = couplingCriterionRepository;
		this.couplingCriteriaCharacteristicRepository = couplingCriteriaCharacteristicRepository;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public ImportResult importERD(final EntityRelationDiagram erd) {
		ImportResult result = new ImportResult();
		if (erd == null) {
			throw new InvalidRestParam();
		}
		List<String> warnings = new ArrayList<>();
		result.setWarnings(warnings);

		UserSystem system = new UserSystem();
		userSystemRepository.save(system);
		String name = erd.getName();
		if (!StringUtils.hasLength(name)) {
			name = "imported " + new Date().toString();
		}
		system.setName(name);

		// TODO: handle case if two nanoentities of consolidated entities have
		// the same name
		Map<String, String> inputEntityAttributes = new HashMap<>();
		for (Entity entity : erd.getEntities()) {
			for (String attribute : entity.getNanoentities()) {
				inputEntityAttributes.put(attribute, entity.getName());
			}
		}

		// entities
		CouplingCriterion criterion = couplingCriterionRepository.readByName(CouplingCriterion.IDENTITY_LIFECYCLE);
		for (Entry<String, List<String>> entityAndNanoentities : findRealEntities(erd).entrySet()) {
			CouplingInstance entityInstance = new CouplingInstance(criterion, InstanceType.SAME_ENTITY);
			system.addCouplingInstance(entityInstance);
			couplingInstanceRepository.save(entityInstance);
			String entityName = entityAndNanoentities.getKey();
			entityInstance.setName(entityName);
			entityInstance.setSystem(system);
			log.info("store entity with attributes {}", entityAndNanoentities.getValue());
			for (String entityAttribute : entityAndNanoentities.getValue()) {
				Nanoentity nanoentity = persistNanoentity(system, inputEntityAttributes.get(entityAttribute), entityAttribute);
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
						.map(attr -> nanoentityRepository.findByContextAndNameAndUserSystem(relation.getOrigin().getName(), attr, system)).collect(Collectors.toList());
				List<Nanoentity> destinationNanoentities = relation.getDestination().getNanoentities().stream()
						.map(attr -> nanoentityRepository.findByContextAndNameAndUserSystem(relation.getDestination().getName(), attr, system)).collect(Collectors.toList());
				instance.setNanoentities(originNanoentities);
				instance.setSecondNanoentities(destinationNanoentities);
				instance.setSystem(system);
				instance.setName(relation.getOrigin().getName() + "." + relation.getDestination().getName());

				log.info("Import aggregation on {} and {}", instance.getNanoentities(), instance.getSecondNanoentities());
			}
		}
		result.setMessage("userSystem " + system.getId() + " has been created");
		result.setId(system.getId());
		return result;
	}

	private Map<String, List<String>> findRealEntities(final EntityRelationDiagram erd) {
		Map<String, List<String>> realEntities = new HashMap<>();
		for (Entity entity : erd.getEntities()) {
			realEntities.put(entity.getName(), entity.getNanoentities());
		}
		List<EntityRelation> currentRelations = new ArrayList<>(erd.getRelations());

		// does this work with cycles?
		List<EntityRelation> relationsToEdgeEntities = getRelationsToEdgeEntities(currentRelations, erd.getEntities());
		while (!relationsToEdgeEntities.isEmpty()) {
			log.info("Entity reduction iteration, reduce relations {}", relationsToEdgeEntities);
			for (EntityRelation relation : relationsToEdgeEntities) {
				if (RelationType.COMPOSITION.equals(relation.getType())) {
					List<String> list = realEntities.get(relation.getOrigin().getName());
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
				.filter(entity -> currentRelations.stream().filter(
						r2 -> (r2.getDestination().equals(entity) && r2.getType().equals(RelationType.INHERITANCE)) || (r2.getOrigin().equals(entity) && r2.getType().equals(RelationType.COMPOSITION)))
				.collect(Collectors.toList()).isEmpty()).collect(Collectors.toList());

		// get all relations that will merge the reducableEntities into
		// another entity
		List<EntityRelation> relationsToEdgeEntities = currentRelations.stream().filter(r -> (reducableEntities.contains(r.getOrigin()) && r.getType().equals(RelationType.INHERITANCE))
				|| (reducableEntities.contains(r.getDestination()) && r.getType().equals(RelationType.COMPOSITION))).collect(Collectors.toList());
		return relationsToEdgeEntities;
	}

	@POST
	@Path("/{systemId}/userrepresentations/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public ImportResult importUserRepresentations(@PathParam("systemId") final Long systemId, final UserRepresentationContainer userRepresentations) {
		ImportResult result = new ImportResult();
		List<String> warnings = new ArrayList<>();
		result.setWarnings(warnings);
		result.setId(systemId);

		UserSystem system = userSystemRepository.findOne(systemId);
		if (system == null) {
			warnings.add("system not defined!");
			return result;
		}

		Predicate<CouplingInstance> notImportedWithERD = i -> notAggregation(i) && notEntity(i);
		List<CouplingInstance> existingInstances = couplingInstanceRepository.findByUserSystem(system).stream().filter(notImportedWithERD).collect(toList());
		boolean replaced = false;
		if (!existingInstances.isEmpty()) {
			replaced = true;
			couplingInstanceRepository.delete(existingInstances);
			log.info("Deleted {} existing coupling instances.", existingInstances.size());
		}

		persistUseCases(system, userRepresentations.getUseCases(), warnings);
		Compatibilities compatibilities = userRepresentations.getCompatibilities();
		if (compatibilities != null) {
			persistCharacteristics(system, compatibilities.getStructuralVolatility(), CouplingCriterion.STRUCTURAL_VOLATILITY, warnings);
			persistCharacteristics(system, compatibilities.getConsistencyCriticality(), CouplingCriterion.CONSISTENCY, warnings);
			persistCharacteristics(system, compatibilities.getSecurityCriticality(), CouplingCriterion.SECURITY_CRITICALITY, warnings);
			persistCharacteristics(system, compatibilities.getStorageSimilarity(), CouplingCriterion.STORAGE_SIMILARITY, warnings);
			persistCharacteristics(system, compatibilities.getContentVolatility(), CouplingCriterion.CONTENT_VOLATILITY, warnings);
			persistCharacteristics(system, compatibilities.getAvailabilityCriticality(), CouplingCriterion.AVAILABILITY, warnings);
		}
		persistRelatedGroups(system, userRepresentations.getAggregates(), CouplingCriterion.CONSISTENCY_CONSTRAINT, warnings);
		persistRelatedGroups(system, userRepresentations.getEntities(), CouplingCriterion.IDENTITY_LIFECYCLE, warnings);
		persistRelatedGroups(system, userRepresentations.getPredefinedServices(), CouplingCriterion.PREDEFINED_SERVICE, warnings);
		persistRelatedGroups(system, userRepresentations.getSecurityAccessGroups(), CouplingCriterion.SECURITY_CONTEXUALITY, warnings);
		persistRelatedGroups(system, userRepresentations.getSeparatedSecurityZones(), CouplingCriterion.SECURITY_CONSTRAINT, warnings);
		persistRelatedGroups(system, userRepresentations.getSharedOwnerGroups(), CouplingCriterion.SHARED_OWNER, warnings);

		log.info("Imported user representations");
		result.setMessage(replaced ? "Replaced all existing User Representations." : "Imported all user representations.");
		return result;
	}

	private boolean notEntity(CouplingInstance i) {
		return !(i.getCouplingCriterion().is(CouplingCriterion.IDENTITY_LIFECYCLE) && i.getType().equals(InstanceType.SAME_ENTITY));
	}

	private boolean notAggregation(CouplingInstance i) {
		return !(i.getCouplingCriterion().is(CouplingCriterion.SEMANTIC_PROXIMITY) && i.getType().equals(InstanceType.AGGREGATION));
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
	@Path("/{systemId}/nanoentities/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public UserSystem importNanoentities(@PathParam("systemId") final Long systemId, final NanoentitiesImport nanoentities) {
		UserSystem system = userSystemRepository.findOne(systemId);
		if (system == null) {
			throw new InvalidRestParam();
		}
		nanoentities.getNanoentities().forEach((nanoentity) -> {
			persistNanoentity(system, nanoentity.getContext(), nanoentity.getName());
		});
		return system;
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

}
