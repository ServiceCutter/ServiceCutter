package ch.hsr.servicestoolkit.importer.api;

import java.util.ArrayList;
import java.util.List;

public class UserRepresentationContainer {
	private List<UseCase> useCases = new ArrayList<>();
	private List<ImportCharacteristic> characteristics = new ArrayList<>();
	private List<RelatedGroups> relatedGroups = new ArrayList<>();

	public List<UseCase> getUseCases() {
		return useCases;
	}

	public void setUseCases(List<UseCase> useCases) {
		this.useCases = useCases;
	}

	public List<ImportCharacteristic> getCharacteristics() {
		return characteristics;
	}

	public void setCharacteristics(List<ImportCharacteristic> characteristics) {
		this.characteristics = characteristics;
	}

	public List<RelatedGroups> getRelatedGroups() {
		return relatedGroups;
	}

	public void setRelatedGroups(List<RelatedGroups> relatedGroups) {
		this.relatedGroups = relatedGroups;
	}

}
