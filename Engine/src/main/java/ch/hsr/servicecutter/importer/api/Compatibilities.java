package ch.hsr.servicecutter.importer.api;

import java.util.List;

public class Compatibilities {
	private List<Characteristic> contentVolatility;
	private List<Characteristic> structuralVolatility;
	private List<Characteristic> availabilityCriticality;
	private List<Characteristic> consistencyCriticality;
	private List<Characteristic> storageSimilarity;
	private List<Characteristic> securityCriticality;

	public Compatibilities() {
	}

	public List<Characteristic> getContentVolatility() {
		return contentVolatility;
	}

	public void setContentVolatility(final List<Characteristic> contentVolatility) {
		this.contentVolatility = contentVolatility;
	}

	public List<Characteristic> getStructuralVolatility() {
		return structuralVolatility;
	}

	public void setStructuralVolatility(final List<Characteristic> structuralVolatility) {
		this.structuralVolatility = structuralVolatility;
	}

	public List<Characteristic> getAvailabilityCriticality() {
		return availabilityCriticality;
	}

	public void setAvailabilityCriticality(final List<Characteristic> availability) {
		this.availabilityCriticality = availability;
	}

	public List<Characteristic> getConsistencyCriticality() {
		return consistencyCriticality;
	}

	public void setConsistencyCriticality(final List<Characteristic> consistency) {
		this.consistencyCriticality = consistency;
	}

	public List<Characteristic> getStorageSimilarity() {
		return storageSimilarity;
	}

	public void setStorageSimilarity(final List<Characteristic> storageSimilarity) {
		this.storageSimilarity = storageSimilarity;
	}

	public List<Characteristic> getSecurityCriticality() {
		return securityCriticality;
	}

	public void setSecurityCriticality(final List<Characteristic> securityCriticality) {
		this.securityCriticality = securityCriticality;
	}

}
