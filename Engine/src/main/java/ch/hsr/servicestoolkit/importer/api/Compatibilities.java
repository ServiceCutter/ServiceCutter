package ch.hsr.servicestoolkit.importer.api;

import java.util.List;

public class Compatibilities {
	private List<Characteristic> volatilityCompatibility;
	private List<Characteristic> changeSimilarityCompatibility;
	private List<Characteristic> availabilityCompatibility;
	private List<Characteristic> consistencyCompatibility;
	private List<Characteristic> storageSimilarityCompatibility;
	private List<Characteristic> securityCriticalityCompatibility;

	public Compatibilities() {
	}

	public List<Characteristic> getVolatilityCompatibility() {
		return volatilityCompatibility;
	}

	public void setVolatilityCompatibility(final List<Characteristic> volatilityCompatibility) {
		this.volatilityCompatibility = volatilityCompatibility;
	}

	public List<Characteristic> getChangeSimilarityCompatibility() {
		return changeSimilarityCompatibility;
	}

	public void setChangeSimilarityCompatibility(final List<Characteristic> changeSimilarityCompatibility) {
		this.changeSimilarityCompatibility = changeSimilarityCompatibility;
	}

	public List<Characteristic> getAvailabilityCompatibility() {
		return availabilityCompatibility;
	}

	public void setAvailabilityCompatibility(final List<Characteristic> availabilityCompatibility) {
		this.availabilityCompatibility = availabilityCompatibility;
	}

	public List<Characteristic> getConsistencyCompatibility() {
		return consistencyCompatibility;
	}

	public void setConsistencyCompatibility(final List<Characteristic> consistencyCompatibility) {
		this.consistencyCompatibility = consistencyCompatibility;
	}

	public List<Characteristic> getStorageSimilarityCompatibility() {
		return storageSimilarityCompatibility;
	}

	public void setStorageSimilarityCompatibility(final List<Characteristic> storageSimilarityCompatibility) {
		this.storageSimilarityCompatibility = storageSimilarityCompatibility;
	}

	public List<Characteristic> getSecurityCriticalityCompatibility() {
		return securityCriticalityCompatibility;
	}

	public void setSecurityCriticalityCompatibility(final List<Characteristic> securityCriticalityCompatibility) {
		this.securityCriticalityCompatibility = securityCriticalityCompatibility;
	}

}
