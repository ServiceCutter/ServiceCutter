package ch.hsr.servicecutter.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ch.hsr.servicecutter.importer.api.Characteristic;
import ch.hsr.servicecutter.importer.api.Compatibilities;
import ch.hsr.servicecutter.importer.api.Entity;
import ch.hsr.servicecutter.importer.api.EntityRelation;
import ch.hsr.servicecutter.importer.api.EntityRelationDiagram;
import ch.hsr.servicecutter.importer.api.RelatedGroup;
import ch.hsr.servicecutter.importer.api.UseCase;
import ch.hsr.servicecutter.importer.api.UserRepresentationContainer;

public class InputCloner {

	public static List<EntityRelationDiagram> cloneErd(final EntityRelationDiagram erd, final int numberOfClones) {
		List<EntityRelationDiagram> erds = new ArrayList<>();
		for (int i = 0; i < numberOfClones; i++) {
			final int erdId = i;
			EntityRelationDiagram newErd = new EntityRelationDiagram();
			newErd.setName(erd.getName() + erdId);
			erds.add(newErd);

			List<Entity> newEntities = new ArrayList<>();
			List<EntityRelation> newRelations = new ArrayList<>();

			erd.getEntities().forEach(e -> {
				Entity newEntity = new Entity(e.getName() + erdId);
				e.getNanoentities().forEach(n -> newEntity.addAttribute(n));
				newEntities.add(newEntity);
			});
			erd.getRelations().forEach(relation -> {
				EntityRelation newRelation = new EntityRelation();
				newRelation.setType(relation.getType());
				newRelation.setOrigin(newEntities.stream().filter(e -> e.getName().equals(relation.getOrigin().getName() + erdId)).findFirst().get());
				newRelation.setDestination(newEntities.stream().filter(e -> e.getName().equals(relation.getDestination().getName() + erdId)).findFirst().get());
				newRelations.add(newRelation);
			});
			newErd.setRelations(newRelations);
			newErd.setEntities(newEntities);
		}
		return erds;
	}

	public static List<UserRepresentationContainer> cloneUserReps(final UserRepresentationContainer userRepresentations, final Long systemNr) {
		List<UserRepresentationContainer> userReps = new ArrayList<>();
		for (int i = 0; i < systemNr; i++) {
			final int erdId = i;
			UserRepresentationContainer newUserRep = new UserRepresentationContainer();
			newUserRep.setAggregates(cloneRelatedGroups(userRepresentations.getAggregates(), erdId));
			newUserRep.setEntities(cloneRelatedGroups(userRepresentations.getEntities(), erdId));
			newUserRep.setPredefinedServices(cloneRelatedGroups(userRepresentations.getPredefinedServices(), erdId));
			newUserRep.setSecurityAccessGroups(cloneRelatedGroups(userRepresentations.getSecurityAccessGroups(), erdId));
			newUserRep.setSeparatedSecurityZones(cloneRelatedGroups(userRepresentations.getSeparatedSecurityZones(), erdId));
			newUserRep.setSharedOwnerGroups(cloneRelatedGroups(userRepresentations.getSharedOwnerGroups(), erdId));
			newUserRep.setCompatibilities(cloneCompatibilities(userRepresentations.getCompatibilities(), erdId));
			newUserRep.setUseCases(cloneUseCases(userRepresentations.getUseCases(), erdId));
			userReps.add(newUserRep);
		}

		return userReps;

	}

	private static List<UseCase> cloneUseCases(final List<UseCase> useCases, final int erdId) {
		List<UseCase> newUseCases = new ArrayList<>();
		useCases.forEach(uc -> {
			UseCase newUC = new UseCase(uc.getName() + erdId);
			newUC.setLatencyCritical(uc.isLatencyCritical());
			newUC.setName(uc.getName() + erdId);
			newUC.setNanoentitiesRead(injectErdId(uc.getNanoentitiesRead(), erdId));
			newUC.setNanoentitiesWritten(injectErdId(uc.getNanoentitiesWritten(), erdId));
			newUseCases.add(newUC);
		});
		return newUseCases;
	}

	private static Compatibilities cloneCompatibilities(final Compatibilities compatibilities, final int erdId) {
		Compatibilities newCompatibilities = new Compatibilities();
		newCompatibilities.setAvailabilityCriticality(cloneCharacteristics(compatibilities.getAvailabilityCriticality(), erdId));
		newCompatibilities.setConsistencyCriticality(cloneCharacteristics(compatibilities.getConsistencyCriticality(), erdId));
		newCompatibilities.setContentVolatility(cloneCharacteristics(compatibilities.getContentVolatility(), erdId));
		newCompatibilities.setSecurityCriticality(cloneCharacteristics(compatibilities.getSecurityCriticality(), erdId));
		newCompatibilities.setStorageSimilarity(cloneCharacteristics(compatibilities.getStorageSimilarity(), erdId));
		newCompatibilities.setStructuralVolatility(cloneCharacteristics(compatibilities.getStructuralVolatility(), erdId));
		newCompatibilities.setSecurityCriticality(cloneCharacteristics(compatibilities.getSecurityCriticality(), erdId));
		return newCompatibilities;
	}

	private static List<Characteristic> cloneCharacteristics(final List<Characteristic> characteristics, final int erdId) {
		if (characteristics == null) {
			return null;
		}

		List<Characteristic> newCharacteristcs = new ArrayList<>();
		characteristics.forEach(c -> {
			Characteristic newCharacteristic = new Characteristic();
			newCharacteristic.setCharacteristic(c.getCharacteristic());
			newCharacteristic.setNanoentities(injectErdId(c.getNanoentities(), erdId));
			newCharacteristcs.add(newCharacteristic);
		});
		return newCharacteristcs;
	}

	private static List<RelatedGroup> cloneRelatedGroups(final List<RelatedGroup> relatedGroups, final int erdId) {
		if (relatedGroups == null) {
			return null;
		}
		List<RelatedGroup> newRelatedGroups = new ArrayList<>();

		relatedGroups.forEach(group -> {
			RelatedGroup newGroup = new RelatedGroup();
			newGroup.setName(group.getName() + erdId);
			newGroup.setNanoentities(injectErdId(group.getNanoentities(), erdId));
			newRelatedGroups.add(newGroup);
		});

		return newRelatedGroups;
	}

	private static List<String> injectErdId(final List<String> nanoentities, final int erdId) {
		return nanoentities.stream().map(nanoentity -> injectErdId(nanoentity, erdId)).collect(Collectors.toList());
	}

	private static String injectErdId(final String nanoentity, final int erdId) {
		return new StringBuilder(nanoentity).insert(nanoentity.indexOf('.'), erdId).toString();
	}

}
