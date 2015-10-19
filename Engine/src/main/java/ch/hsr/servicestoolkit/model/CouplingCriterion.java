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

import com.google.common.base.Objects;

@Entity
public class CouplingCriterion {

	@Id
	@GeneratedValue
	private Long id;
	@Enumerated(EnumType.STRING)
	private CriterionType criterionType;
	private String name;

	@ManyToMany
	@JoinTable(name = "datafield_to_couplingcriterion", joinColumns = {@JoinColumn(name = "datafield_id", referencedColumnName = "id")}, inverseJoinColumns = {
			@JoinColumn(name = "criterion_id", referencedColumnName = "id")})
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

	public void setId(Long id) {
		this.id = id;
	}

	public CriterionType getCriterionType() {
		return criterionType;
	}

	public void setCriterionType(CriterionType rating) {
		this.criterionType = rating;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CouplingCriterion) {
			CouplingCriterion other = (CouplingCriterion) obj;
			return Objects.equal(this, other) || Objects.equal(id, other.id);
		} else {
			return false;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
