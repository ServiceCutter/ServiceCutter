package ch.hsr.servicestoolkit.importer.api;

import java.util.List;

public class UserRepresentationContainer {

	private List<UseCase> useCases;
	private List<RelatedGroup> sharedOwnerGroups;
	private List<RelatedGroup> aggregates;
	private List<RelatedGroup> entities;
	private List<RelatedGroup> predefinedServices;
	private List<RelatedGroup> separatedSecurityZones;
	private List<RelatedGroup> securityAccessGroups;
	private List<ImportCharacteristic> volatilityCompatibility;
	private List<ImportCharacteristic> changeSimilarityCompatibility;
	private List<ImportCharacteristic> availabilityCompatibility;
	private List<ImportCharacteristic> consistencyCompatibility;
	private List<ImportCharacteristic> storageSimilarityCompatibility;
	private List<ImportCharacteristic> securityCriticalityCompatibility;

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

	public List<ImportCharacteristic> getVolatilityCompatibility() {
		return volatilityCompatibility;
	}

	public void setVolatilityCompatibility(final List<ImportCharacteristic> volatilityCompatibility) {
		this.volatilityCompatibility = volatilityCompatibility;
	}

	public List<ImportCharacteristic> getChangeSimilarityCompatibility() {
		return changeSimilarityCompatibility;
	}

	public void setChangeSimilarityCompatibility(final List<ImportCharacteristic> changeSimilarityCompatibility) {
		this.changeSimilarityCompatibility = changeSimilarityCompatibility;
	}

	public List<ImportCharacteristic> getAvailabilityCompatibility() {
		return availabilityCompatibility;
	}

	public void setAvailabilityCompatibility(final List<ImportCharacteristic> availabilityCompatibility) {
		this.availabilityCompatibility = availabilityCompatibility;
	}

	public List<ImportCharacteristic> getConsistencyCompatibility() {
		return consistencyCompatibility;
	}

	public void setConsistencyCompatibility(final List<ImportCharacteristic> consistencyCompatibility) {
		this.consistencyCompatibility = consistencyCompatibility;
	}

	public List<ImportCharacteristic> getStorageSimilarityCompatibility() {
		return storageSimilarityCompatibility;
	}

	public void setStorageSimilarityCompatibility(final List<ImportCharacteristic> storageSimilarityCompatibility) {
		this.storageSimilarityCompatibility = storageSimilarityCompatibility;
	}

	public List<ImportCharacteristic> getSecurityCriticalityCompatibility() {
		return securityCriticalityCompatibility;
	}

	public void setSecurityCriticalityCompatibility(final List<ImportCharacteristic> securityCriticalityCompatibility) {
		this.securityCriticalityCompatibility = securityCriticalityCompatibility;
	}

}
