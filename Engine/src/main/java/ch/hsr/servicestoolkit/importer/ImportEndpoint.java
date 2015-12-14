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
import ch.hsr.servicestoolkit.importer.api.DistanceCharacteristic;
import ch.hsr.servicestoolkit.importer.api.DomainModel;
import ch.hsr.servicestoolkit.importer.api.Entity;
import ch.hsr.servicestoolkit.importer.api.EntityRelation;
import ch.hsr.servicestoolkit.importer.api.EntityRelation.RelationType;
import ch.hsr.servicestoolkit.importer.api.ImportNanoentity;
import ch.hsr.servicestoolkit.importer.api.SeparationCriterion;
import ch.hsr.servicestoolkit.importer.api.UseCase;
import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic;
import ch.hsr.servicestoolkit.model.CouplingInstance;
import ch.hsr.servicestoolkit.model.CouplingType;
import ch.hsr.servicestoolkit.model.InstanceType;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.Nanoentity;
import ch.hsr.servicestoolkit.model.repository.CouplingCriterionCharacteristicRepository;
import ch.hsr.servicestoolkit.model.repository.CouplingCriterionRepository;
import ch.hsr.servicestoolkit.model.repository.CouplingInstanceRepository;
import ch.hsr.servicestoolkit.model.repository.ModelRepository;
import ch.hsr.servicestoolkit.model.repository.NanoentityRepository;
import ch.hsr.servicestoolkit.rest.InvalidRestParam;

@Component
@Path("/engine/import")
public class ImportEndpoint {

	private final Logger log = LoggerFactory.getLogger(ImportEndpoint.class);
	private final ModelRepository modelRepository;
	private final NanoentityRepository nanoentityRepository;
	private final CouplingCriterionRepository couplingCriterionRepository;
	private final CouplingCriterionCharacteristicRepository couplingCriteriaCharacteristicRepository;
	private final CouplingInstanceRepository couplingInstanceRepository;
	private final ModelCompleter modelCompleter;

	@Autowired
	public ImportEndpoint(final ModelRepository modelRepository, final NanoentityRepository nanoentityRepository, final CouplingInstanceRepository couplingInstanceRepository,
			final ModelCompleter modelCompleter, CouplingCriterionRepository couplingCriterionRepository, CouplingCriterionCharacteristicRepository couplingCriteriaCharacteristicRepository) {
		this.modelRepository = modelRepository;
		this.nanoentityRepository = nanoentityRepository;
		this.couplingInstanceRepository = couplingInstanceRepository;
		this.modelCompleter = modelCompleter;
		this.couplingCriterionRepository = couplingCriterionRepository;
		this.couplingCriteriaCharacteristicRepository = couplingCriteriaCharacteristicRepository;
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

		Map<ImportNanoentity, String> entityAttributes = new HashMap<>();
		for (Entity entity : domainModel.getEntities()) {
			for (ImportNanoentity attribute : entity.getNanoentities()) {
				entityAttributes.put(attribute, entity.getName());
			}

		}
		// entities
		CouplingCriterion criterion = couplingCriterionRepository.readByName(CouplingCriterion.IDENTITY_LIFECYCLE);
		for (Entry<String, List<ImportNanoentity>> entityAndNanoentities : findRealEntities(domainModel).entrySet()) {
			CouplingInstance entityInstance = new CouplingInstance(criterion, InstanceType.SAME_ENTITY);
			model.addCouplingInstance(entityInstance);
			couplingInstanceRepository.save(entityInstance);
			String entityName = entityAndNanoentities.getKey();
			entityInstance.setName(entityName);
			entityInstance.setModel(model);
			log.info("store entity with attributes {}", entityAndNanoentities.getValue());
			for (ImportNanoentity entityAttribute : entityAndNanoentities.getValue()) {
				Nanoentity nanoentity = persistNanoentity(model, entityAttributes.get(entityAttribute), entityAttribute.getName());
				entityInstance.addNanoentity(nanoentity);
				log.info("Import nanoentity {}", nanoentity.getContextName());
			}
		}

		// Aggregations
		CouplingCriterion semanticProximity = couplingCriterionRepository.readByName(CouplingCriterion.SEMANTIC_PROXIMITY);
		for (EntityRelation relation : domainModel.getRelations()) {
			if (RelationType.AGGREGATION.equals(relation.getType())) {
				CouplingInstance instance = new CouplingInstance(semanticProximity, InstanceType.AGGREGATION);
				couplingInstanceRepository.save(instance);
				List<Nanoentity> originNanoentities = relation.getOrigin().getNanoentities().stream()
						.map(attr -> nanoentityRepository.findByContextAndNameAndModel(relation.getOrigin().getName(), attr.getName(), model)).collect(Collectors.toList());
				List<Nanoentity> destinationNanoentities = relation.getDestination().getNanoentities().stream()
						.map(attr -> nanoentityRepository.findByContextAndNameAndModel(relation.getDestination().getName(), attr.getName(), model)).collect(Collectors.toList());
				instance.setNanoentities(originNanoentities);
				instance.setSecondNanoentities(destinationNanoentities);
				instance.setModel(model);
				instance.setName(relation.getOrigin().getName() + "." + relation.getDestination().getName());

				log.info("Import aggregation on {} and {}", instance.getNanoentities(), instance.getSecondNanoentities());
			}
		}
		// TODO: remove return value and set location header to URL of generated
		// model
		Map<String, Object> result = new HashMap<>();
		result.put("message", "model " + model.getId() + " has been created");
		result.put("id", model.getId());
		return result;
	}

	private Map<String, List<ImportNanoentity>> findRealEntities(final DomainModel domainModel) {
		Map<String, List<ImportNanoentity>> realEntities = new HashMap<>();
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

	public class UserRepresentationContainer {
		public List<UseCase> useCases;
		List<DistanceCharacteristic> characteristics;
		List<SeparationCriterion> separations;
		List<CohesiveGroups> cohesiveGroups;
	}

	@POST
	@Path("/{modelId}/userrepresentations/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void importUserRepresentations(@PathParam("modelId") final Long modelId, UserRepresentationContainer userRepresentations) {
		Model model = modelRepository.findOne(modelId);
		if (model == null) {
			throw new InvalidRestParam();
		}
		persistUseCases(model, userRepresentations.useCases);
		persistCharacteristics(model, userRepresentations.characteristics);
		persistSeparations(model, userRepresentations.separations);
		persistCohesiveGroups(model, userRepresentations.cohesiveGroups);
		log.info("Imported user representations");
	}

	@POST
	@Path("/{modelId}/usecases/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void importUseCases(@PathParam("modelId") final Long modelId, final List<UseCase> transactions) {
		Model model = modelRepository.findOne(modelId);
		if (model == null) {
			throw new InvalidRestParam();
		}
		persistUseCases(model, transactions);
	}

	private void persistUseCases(Model model, final List<UseCase> useCases) {
		CouplingCriterion semanticProximity = couplingCriterionRepository.readByName(CouplingCriterion.SEMANTIC_PROXIMITY);
		for (UseCase transaction : useCases) {
			CouplingInstance instance = new CouplingInstance(semanticProximity, InstanceType.USE_CASE);
			model.addCouplingInstance(instance);
			couplingInstanceRepository.save(instance);
			instance.setName(transaction.getName());
			instance.setModel(model);
			instance.setNanoentities(loadNanoentities(transaction.getNanoentitiesRead(), model));
			instance.setSecondNanoentities(loadNanoentities(transaction.getNanoentitiesWritten(), model));
			log.info("Import use cases {} with fields written {} and fields read {}", transaction.getName(), transaction.getNanoentitiesWritten(), transaction.getNanoentitiesRead());
		}
	}

	@POST
	@Path("/{modelId}/nanoentities/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void importNanoentities(@PathParam("modelId") final Long modelId, final List<ImportNanoentity> nanoentities) {
		Model model = modelRepository.findOne(modelId);
		if (model == null) {
			throw new InvalidRestParam();
		}
		nanoentities.forEach((nanoentity) -> {
			persistNanoentity(model, nanoentity.getContext(), nanoentity.getName());
		});
	}

	private Nanoentity persistNanoentity(Model model, String context, String name) {
		Nanoentity nanoentity = new Nanoentity();
		nanoentity.setName(name);
		nanoentity.setContext(context);
		model.addNanoentity(nanoentity);
		nanoentityRepository.save(nanoentity);
		return nanoentity;
	}

	@POST
	@Path("/{modelId}/characteristics/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void importCharacteristics(@PathParam("modelId") final Long modelId, final List<DistanceCharacteristic> characteristics) {
		Model model = modelRepository.findOne(modelId);
		if (model == null || characteristics == null) {
			throw new InvalidRestParam();
		}
		persistCharacteristics(model, characteristics);
	}

	private void persistCharacteristics(Model model, final List<DistanceCharacteristic> characteristics) {
		for (DistanceCharacteristic inputCharacteristic : characteristics) {
			CouplingCriterionCharacteristic characteristic = findCharacteristic(inputCharacteristic.getCouplingCriterionName(), inputCharacteristic.getCharacteristicName());
			if (characteristic == null) {
				log.error("characteristic {} not known! ignore", inputCharacteristic);
				continue;
			}
			Set<CouplingInstance> instance = couplingInstanceRepository.findByModelAndCharacteristic(model, characteristic);

			if (instance == null || instance.isEmpty()) {
				CouplingInstance newInstance = new CouplingInstance(characteristic, InstanceType.CHARACTERISTIC);
				model.addCouplingInstance(newInstance);
				couplingInstanceRepository.save(newInstance);
				newInstance.setName(inputCharacteristic.getCouplingCriterionName());
				newInstance.setModel(model);
				newInstance.setNanoentities(loadNanoentities(inputCharacteristic.getNanoentities(), model));
				log.info("Import distance characteristic {}-{} with nanoentities {}", inputCharacteristic.getCouplingCriterionName(), inputCharacteristic.getCharacteristicName(),
						newInstance.getAllNanoentities());
			} else {
				log.error("enhancing characteristics not yet implemented. criterion: {}, characteristic: {}", inputCharacteristic.getCouplingCriterionName(),
						inputCharacteristic.getCharacteristicName());
				throw new InvalidRestParam();
			}
		}
		modelCompleter.completeModelWithDefaultsForDistance(model);
	}

	@POST
	@Path("/{modelId}/separations/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void importSeparations(@PathParam("modelId") final Long modelId, final List<SeparationCriterion> separations) {
		Model model = modelRepository.findOne(modelId);
		if (model == null || separations == null) {
			throw new InvalidRestParam();
		}
		persistSeparations(model, separations);
	}

	private void persistSeparations(Model model, final List<SeparationCriterion> separations) {
		for (SeparationCriterion inputCriterion : separations) {
			CouplingCriterion couplingCriterion = couplingCriterionRepository.readByName(inputCriterion.getCouplingCriterionName());
			CouplingInstance newInstance = new CouplingInstance(couplingCriterion, InstanceType.SEPARATION_GROUP);
			model.addCouplingInstance(newInstance);
			couplingInstanceRepository.save(newInstance);
			newInstance.setName(inputCriterion.getCouplingCriterionName());
			newInstance.setModel(model);
			newInstance.setNanoentities(loadNanoentities(inputCriterion.getGroupAnanoentities(), model));
			newInstance.setSecondNanoentities(loadNanoentities(inputCriterion.getGroupBnanoentities(), model));
			log.info("Import separation constraint {} on nanoentities {} and {}", inputCriterion.getCouplingCriterionName(), newInstance.getNanoentities(), newInstance.getSecondNanoentities());
		}
	}

	@POST
	@Path("/{modelId}/cohesivegroups/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void importCohesiveGroups(@PathParam("modelId") final Long modelId, final List<CohesiveGroups> listOfGroups) {
		Model model = modelRepository.findOne(modelId);
		if (model == null || listOfGroups == null) {
			throw new InvalidRestParam();
		}

		persistCohesiveGroups(model, listOfGroups);
	}

	private void persistCohesiveGroups(Model model, final List<CohesiveGroups> listOfGroups) {
		for (CohesiveGroups groups : listOfGroups) {
			CouplingCriterion couplingCriterion = couplingCriterionRepository.readByName(groups.getCouplingCriterionName());
			CouplingCriterionCharacteristic characteristic = null;

			if (CouplingType.COMPATIBILITY.equals(couplingCriterion.getType())) {
				characteristic = findCharacteristic(groups.getCouplingCriterionName(), groups.getCharacteristicName());
				if (characteristic == null) {
					log.error("characteristic {} not known! ignore", groups.getCharacteristicName());
					continue;
				}
			}

			for (CohesiveGroup cohesiveGroup : groups.getCohesiveGroups()) {
				CouplingInstance instance = characteristic == null ? new CouplingInstance(couplingCriterion, InstanceType.SHARED_OWNER)
						: new CouplingInstance(characteristic, InstanceType.SHARED_OWNER);
				model.addCouplingInstance(instance);
				couplingInstanceRepository.save(instance);
				instance.setName(groups.getCouplingCriterionName());
				instance.setModel(model);
				instance.setNanoentities(loadNanoentities(cohesiveGroup.getNanoentities(), model));
				log.info("Import cohesive group {} on nanoentities {} ", cohesiveGroup.getName(), instance.getNanoentities());
			}
		}
	}

	private List<Nanoentity> loadNanoentities(final List<String> names, final Model model) {
		List<Nanoentity> nanoentities = new ArrayList<>();
		for (String nanoentityName : names) {
			Nanoentity nanoentity;
			if (nanoentityName.contains(".")) {
				String[] splittedName = nanoentityName.split("\\.");
				nanoentity = nanoentityRepository.findByContextAndNameAndModel(splittedName[0], splittedName[1], model);
			} else {
				nanoentity = nanoentityRepository.findByNameAndModel(nanoentityName, model);
			}

			if (nanoentity != null) {
				nanoentities.add(nanoentity);
			} else {
				log.warn("	 nanoentity with name {}", nanoentityName);
			}
		}
		return nanoentities;
	}

	private CouplingCriterionCharacteristic findCharacteristic(String coupling, String characteristic) {
		CouplingCriterion couplingCriterion = couplingCriterionRepository.readByName(coupling);
		Assert.notNull(couplingCriterion, "Coupling with name " + coupling + " not found!");
		CouplingCriterionCharacteristic result = couplingCriteriaCharacteristicRepository.readByNameAndCouplingCriterion(characteristic, couplingCriterion);
		Assert.notNull(result, "Characteristic with name " + characteristic + " not found!");
		return result;
	}
}
