package ch.hsr.servicestoolkit.importer.api;

import java.util.ArrayList;
import java.util.List;

public class UserRepresentationContainer {
	private List<UseCase> useCases = new ArrayList<>();
	private List<DistanceCharacteristic> characteristics = new ArrayList<>();
	private List<SeparationCriterion> separations = new ArrayList<>();
	private List<CohesiveGroups> cohesiveGroups = new ArrayList<>();

	public List<UseCase> getUseCases() {
		return useCases;
	}

	public void setUseCases(List<UseCase> useCases) {
		this.useCases = useCases;
	}

	public List<DistanceCharacteristic> getCharacteristics() {
		return characteristics;
	}

	public void setCharacteristics(List<DistanceCharacteristic> characteristics) {
		this.characteristics = characteristics;
	}

	public List<SeparationCriterion> getSeparations() {
		return separations;
	}

	public void setSeparations(List<SeparationCriterion> separations) {
		this.separations = separations;
	}

	public List<CohesiveGroups> getCohesiveGroups() {
		return cohesiveGroups;
	}

	public void setCohesiveGroups(List<CohesiveGroups> cohesiveGroups) {
		this.cohesiveGroups = cohesiveGroups;
	}
}
