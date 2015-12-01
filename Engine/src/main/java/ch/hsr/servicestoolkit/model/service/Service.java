package ch.hsr.servicestoolkit.model.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import ch.hsr.servicestoolkit.model.NanoEntity;

public class Service {

	private Set<NanoEntity> dataFields = new HashSet<>();

	public Service() {
	}

	public Service(NanoEntity... dataFields) {
		Collections.addAll(this.dataFields, dataFields);
	}

	public Set<NanoEntity> getDataFields() {
		return dataFields;
	}

	public void setDataFields(Set<NanoEntity> dataFields) {
		this.dataFields = dataFields;
	}

	public String getFieldNames() {
		return dataFields.stream().map(NanoEntity::getName).collect(Collectors.joining(","));
	}
}
