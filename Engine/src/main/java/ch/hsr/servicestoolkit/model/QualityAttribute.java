package ch.hsr.servicestoolkit.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
public class QualityAttribute {

	@Id
	@GeneratedValue
	private Long id;

	@Enumerated(EnumType.STRING)
	private CriterionType criterionType;

	@ManyToMany
	@JoinTable(name = "datafield_to_qualityattribute", joinColumns = {
			@JoinColumn(name = "datafield_id", referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "qualityattr_id", referencedColumnName = "id") })
	private List<DataField> dataFields = new ArrayList<>();

	public List<DataField> getDataFields() {
		return dataFields;
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

	public void setId(Long id) {
		this.id = id;
	}

	public CriterionType getCriterionType() {
		return criterionType;
	}

	public void setCriterionType(CriterionType rating) {
		this.criterionType = rating;
	}

	// TODO use helper for hashcode and equals
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QualityAttribute other = (QualityAttribute) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
