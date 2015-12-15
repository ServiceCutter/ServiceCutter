package ch.hsr.servicestoolkit.importer.api;

import java.util.List;

public class Compatibilities {
	private List<ImportCharacteristic> volatilityCompatibility;
	private List<ImportCharacteristic> changeSimilarityCompatibility;
	private List<ImportCharacteristic> availabilityCompatibility;
	private List<ImportCharacteristic> consistencyCompatibility;
	private List<ImportCharacteristic> storageSimilarityCompatibility;
	private List<ImportCharacteristic> securityCriticalityCompatibility;

	public Compatibilities() {
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
