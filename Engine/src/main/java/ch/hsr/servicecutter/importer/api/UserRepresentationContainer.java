package ch.hsr.servicecutter.importer.api;

import java.util.List;

public class UserRepresentationContainer {
	private List<UseCase> useCases;
	private List<RelatedGroup> sharedOwnerGroups;
	private List<RelatedGroup> aggregates;
	private List<RelatedGroup> entities;
	private List<RelatedGroup> predefinedServices;
	private List<RelatedGroup> separatedSecurityZones;
	private List<RelatedGroup> securityAccessGroups;
	private Compatibilities compatibilities;

	public UserRepresentationContainer() {
	}

	public List<UseCase> getUseCases() {
		return useCases;
	}

	public void setUseCases(final List<UseCase> useCases) {
		this.useCases = useCases;
	}

	public List<RelatedGroup> getSharedOwnerGroups() {
		return sharedOwnerGroups;
	}

	public void setSharedOwnerGroups(final List<RelatedGroup> sharedOwnerGroups) {
		this.sharedOwnerGroups = sharedOwnerGroups;
	}

	public List<RelatedGroup> getAggregates() {
		return aggregates;
	}

	public void setAggregates(final List<RelatedGroup> aggregates) {
		this.aggregates = aggregates;
	}

	public List<RelatedGroup> getEntities() {
		return entities;
	}

	public void setEntities(final List<RelatedGroup> entities) {
		this.entities = entities;
	}

	public List<RelatedGroup> getPredefinedServices() {
		return predefinedServices;
	}

	public void setPredefinedServices(final List<RelatedGroup> predefinedServices) {
		this.predefinedServices = predefinedServices;
	}

	public List<RelatedGroup> getSeparatedSecurityZones() {
		return separatedSecurityZones;
	}

	public void setSeparatedSecurityZones(final List<RelatedGroup> separatedSecurityZones) {
		this.separatedSecurityZones = separatedSecurityZones;
	}

	public List<RelatedGroup> getSecurityAccessGroups() {
		return securityAccessGroups;
	}

	public void setSecurityAccessGroups(final List<RelatedGroup> securityAccessGroups) {
		this.securityAccessGroups = securityAccessGroups;
	}

	public Compatibilities getCompatibilities() {
		return compatibilities;
	}

	public void setCompatibilities(final Compatibilities compatibilities) {
		this.compatibilities = compatibilities;
	}

}
