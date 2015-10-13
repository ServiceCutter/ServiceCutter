package ch.hsr.servicestoolkit.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

@Entity
public class Model {

	@Id
	@GeneratedValue
	private Long id;
	private String name;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "model")
	private List<DataField> dataFields = new ArrayList<>();

	public List<DataField> getDataFields() {
		return dataFields;
	}

	public void addDataField(DataField dataField) {
		dataFields.add(dataField);
	}

	public void setDataFields(List<DataField> dataFields) {
		this.dataFields.clear();
		if (dataFields != null) {
			this.dataFields.addAll(dataFields);
		}
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
