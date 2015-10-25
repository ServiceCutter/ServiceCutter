package ch.hsr.servicestoolkit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.google.common.base.Objects;

@Entity
public class Model {

	@Id
	@GeneratedValue
	private Long id;
	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "model", fetch = FetchType.EAGER)
	private List<DataField> dataFields = new ArrayList<>();

	public List<DataField> getDataFields() {
		return Collections.unmodifiableList(dataFields);
	}

	public void addDataField(final DataField dataField) {
		dataFields.add(dataField);
		dataField.setModel(this);
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
		return Objects.hashCode(id);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Model) {
			Model other = (Model) obj;
			return this == other || Objects.equal(id, other.id);
		} else {
			return false;
		}
	}

}
