package ch.hsr.servicestoolkit.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import ch.hsr.servicestoolkit.model.service.Service;
import ch.hsr.servicestoolkit.model.service.ServiceCut;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue(value = "1")
public class MonoCouplingInstance {

	@Id
	@GeneratedValue
	private Long id;
	private String name;
	@ManyToMany
	@JoinTable(name = "datafield_to_second_couplinginstance", joinColumns = {@JoinColumn(name = "datafield_id", referencedColumnName = "id")}, inverseJoinColumns = {
			@JoinColumn(name = "coupling_id", referencedColumnName = "id")})
	private List<DataField> dataFields = new ArrayList<>();
	@ManyToOne
	private CouplingCriteriaVariant couplingCriteriaVariant;
	private boolean singleInstancePerModel = false;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<DataField> getDataFields() {
		return Collections.unmodifiableList(dataFields);
	}

	public void addDataField(DataField dataField) {
		dataFields.add(dataField);
	}

	public void setDataFields(Collection<DataField> dataFields) {
		this.dataFields.clear();
		if (dataFields != null) {
			this.dataFields.addAll(dataFields);
		}
	}

	public boolean isSingleInstancePerModel() {
		return singleInstancePerModel;
	}

	public void setSingleInstancePerModel(boolean singleInstancePerModel) {
		this.singleInstancePerModel = singleInstancePerModel;
	}

	public CouplingCriteriaVariant getCouplingCriteriaVariant() {
		return couplingCriteriaVariant;
	}

	public void setCouplingCriteriaVariant(CouplingCriteriaVariant couplingCriteriaVariant) {
		this.couplingCriteriaVariant = couplingCriteriaVariant;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSatisfiedBy(ServiceCut cut) {
		// TYPE: proximity
		for (Service service : cut.getServices()) {
			for (DataField dataField : dataFields) {
				// if service contains any of the data fields
				if (service.getDataFields().contains(dataField)) {
					// then is has to contain ALL of them
					if (service.getDataFields().containsAll(dataFields)) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}
}
