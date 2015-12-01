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
	@JoinTable(name = "datafield_to_couplinginstance", joinColumns = { @JoinColumn(name = "datafield_id", referencedColumnName = "id") }, inverseJoinColumns = {
			@JoinColumn(name = "coupling_id", referencedColumnName = "id") })
	private List<NanoEntity> secondDataFields = new ArrayList<>();
	private Double frequency; // TODO: refactor, only used for semantic
								// proximity use cases

	public List<NanoEntity> getSecondDataFields() {
		return Collections.unmodifiableList(secondDataFields);
	}

	public void addSecondDataField(final NanoEntity secondDataField) {
		secondDataFields.add(secondDataField);
	}

	public void setFrequency(final double frequency) {
		this.frequency = frequency;
	}

	public Double getFrequency() {
		return frequency;
	}

	public void setSecondDataFields(final List<NanoEntity> secondDataFields) {
		this.secondDataFields.clear();
		if (secondDataFields != null) {
			this.secondDataFields.addAll(secondDataFields);
		}
	}

	@Override
	public List<NanoEntity> getAllFields() {
		List<NanoEntity> result = new ArrayList<>();
		result.addAll(super.getAllFields());
		result.addAll(secondDataFields);
		return result;
	}

	@Override
	public String toString() {
		return getName();
	}

}
