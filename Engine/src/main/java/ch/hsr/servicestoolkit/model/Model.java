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
	private List<DataField> dataFields = new ArrayList<DataField>();

	public List<DataField> getDataFields() {
		return dataFields;
	}

	void setDataFields(List<DataField> dataFields) {
		this.dataFields = dataFields;
	}

	public void addDataField(DataField dataField) {
		dataFields.add(dataField);
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
