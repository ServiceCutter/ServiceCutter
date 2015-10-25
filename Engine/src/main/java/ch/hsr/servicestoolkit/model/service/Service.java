package ch.hsr.servicestoolkit.model.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ch.hsr.servicestoolkit.model.DataField;

public class Service {

	private Set<DataField> dataFields = new HashSet<>();

	public Service() {
	}

	public Service(DataField... dataFields) {
		Collections.addAll(this.dataFields, dataFields);
	}

	public Set<DataField> getDataFields() {
		return dataFields;
	}

	public void setDataFields(Set<DataField> dataFields) {
		this.dataFields = dataFields;
	}
}
