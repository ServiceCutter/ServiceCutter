package ch.hsr.servicecutter.importer.api;

import java.util.List;

public class Compatibilities {
	private List<Characteristic> contentVolatility;
	private List<Characteristic> structuralVolatility;
	private List<Characteristic> availability;
	private List<Characteristic> consistency;
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

	public List<Characteristic> getAvailability() {
		return availability;
	}

	public void setAvailability(final List<Characteristic> availability) {
		this.availability = availability;
	}

	public List<Characteristic> getConsistency() {
		return consistency;
	}

	public void setConsistency(final List<Characteristic> consistency) {
		this.consistency = consistency;
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
