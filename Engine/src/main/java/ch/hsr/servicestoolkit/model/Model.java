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

	public void addDataField(final DataField dataField) {
		dataFields.add(dataField);
	}

	public void setDataFields(final List<DataField> dataFields) {
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

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Model other = (Model) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

}
