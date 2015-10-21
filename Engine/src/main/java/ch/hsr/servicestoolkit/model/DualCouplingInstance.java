package ch.hsr.servicestoolkit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
@DiscriminatorValue(value = "2")
public class DualCouplingInstance extends MonoCouplingInstance {
	@ManyToMany
	@JoinTable(name = "datafield_to_second_couplinginstance", joinColumns = {@JoinColumn(name = "datafield_id", referencedColumnName = "id")}, inverseJoinColumns = {
			@JoinColumn(name = "coupling_id", referencedColumnName = "id")})
	private List<DataField> secondDataFields = new ArrayList<>();

	public List<DataField> getSecondDataFields() {
		return Collections.unmodifiableList(secondDataFields);
	}

	public void addSecondDataField(DataField secondDataField) {
		secondDataFields.add(secondDataField);
	}

	public void setSecondDataFields(List<DataField> secondDataFields) {
		this.secondDataFields.clear();
		if (secondDataFields != null) {
			this.secondDataFields.addAll(secondDataFields);
		}
	}

}
