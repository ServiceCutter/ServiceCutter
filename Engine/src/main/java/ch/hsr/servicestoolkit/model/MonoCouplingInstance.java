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

import com.fasterxml.jackson.annotation.JsonIgnore;

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

	@ManyToOne
	@JsonIgnore
	private Model model;

	@ManyToMany
	@JoinTable(name = "datafield_to_second_couplinginstance", joinColumns = { @JoinColumn(name = "datafield_id", referencedColumnName = "id") }, inverseJoinColumns = {
			@JoinColumn(name = "coupling_id", referencedColumnName = "id") })
	private List<DataField> dataFields = new ArrayList<>();
	@ManyToOne
	private CouplingCriteriaVariant variant;
	private boolean singleInstancePerModel = false;

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public List<DataField> getDataFields() {
		return Collections.unmodifiableList(dataFields);
	}

	public void addDataField(final DataField dataField) {
		dataFields.add(dataField);
	}

	public void setDataFields(final Collection<DataField> dataFields) {
		this.dataFields.clear();
		if (dataFields != null) {
			this.dataFields.addAll(dataFields);
		}
	}

	public boolean isSingleInstancePerModel() {
		return singleInstancePerModel;
	}

	public void setSingleInstancePerModel(final boolean singleInstancePerModel) {
		this.singleInstancePerModel = singleInstancePerModel;
	}

	public CouplingCriteriaVariant getVariant() {
		return variant;
	}

	public void setVariant(final CouplingCriteriaVariant variant) {
		this.variant = variant;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public boolean fieldsAreInSameService(final ServiceCut cut) {
		// TYPE: proximity
		for (Service service : cut.getServices()) {
			for (DataField dataField : getAllFields()) {
				// if service contains any of the data fields
				if (service.getDataFields().contains(dataField)) {
					// then is has to contain ALL of them
					if (service.getDataFields().containsAll(getAllFields())) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}

	public List<DataField> getAllFields() {
		return dataFields;
	}

	public void setModel(final Model model) {

		this.model = model;
	}
}
