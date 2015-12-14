package ch.hsr.servicestoolkit.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.junit.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.hsr.servicestoolkit.model.service.Service;
import ch.hsr.servicestoolkit.model.service.ServiceCut;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class CouplingInstance implements Comparable<CouplingInstance> {

	@Id
	@GeneratedValue
	private Long id;
	private String name;

	@ManyToOne
	@JsonIgnore
	private Model model;

	@ManyToMany
	@JoinTable(name = "nanoentity_to_second_couplinginstance", joinColumns = {@JoinColumn(name = "nanoentity_id", referencedColumnName = "id")}, inverseJoinColumns = {
			@JoinColumn(name = "coupling_id", referencedColumnName = "id")})
	private List<Nanoentity> nanoentities = new ArrayList<>();
	@ManyToOne
	@NotNull
	private CouplingCriterion couplingCriterion;
	@ManyToOne
	private CouplingCriterionCharacteristic characteristic;
	private boolean singleInstancePerModel = false;
	@ManyToMany
	@JoinTable(name = "nanoentity_to_couplinginstance", joinColumns = {@JoinColumn(name = "nanoentity_id", referencedColumnName = "id")}, inverseJoinColumns = {
			@JoinColumn(name = "coupling_id", referencedColumnName = "id")})
	private List<Nanoentity> secondNanoentities = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	private InstanceType instanceType;

	public CouplingInstance(CouplingCriterion couplingCriterion, InstanceType type) {
		Assert.assertNotEquals("Constructor only to be used for not-compatibility criteria!", CouplingType.COMPATIBILITY, couplingCriterion.getType());
		instanceType = type;
		this.couplingCriterion = couplingCriterion;
	}

	public CouplingInstance(CouplingCriterionCharacteristic characteristic, InstanceType type) {
		instanceType = type;
		setCharacteristicAndCriterion(characteristic);
	}

	public CouplingInstance() {
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public List<Nanoentity> getNanoentities() {
		return Collections.unmodifiableList(nanoentities);
	}

	public void addNanoentity(final Nanoentity nanoentity) {
		nanoentities.add(nanoentity);
	}

	public void setNanoentities(final Collection<Nanoentity> nanoentities) {
		this.nanoentities.clear();
		if (nanoentities != null) {
			this.nanoentities.addAll(nanoentities);
		}
	}

	public boolean isSingleInstancePerModel() {
		return singleInstancePerModel;
	}

	public void setSingleInstancePerModel(final boolean singleInstancePerModel) {
		this.singleInstancePerModel = singleInstancePerModel;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public boolean nanoentitiesAreInSameService(final ServiceCut cut) {
		// TYPE: proximity
		for (Service service : cut.getServices()) {
			for (Nanoentity nanoentity : getAllNanoentities()) {
				// if service contains any of the nanoentities
				if (service.getNanoentities().contains(nanoentity)) {
					// then is has to contain ALL of them
					if (service.getNanoentities().containsAll(getAllNanoentities())) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}

	public void setModel(final Model model) {
		this.model = model;
	}

	public CouplingCriterion getCouplingCriterion() {
		return couplingCriterion;
	}

	public void setCouplingCriterion(CouplingCriterion couplingCriterion) {
		this.couplingCriterion = couplingCriterion;
	}

	public CouplingCriterionCharacteristic getCharacteristic() {
		return characteristic;
	}

	public void setCharacteristic(CouplingCriterionCharacteristic characteristic) {
		setCharacteristicAndCriterion(characteristic);
	}

	public boolean isCharacteristic(String name) {
		return characteristic != null && characteristic.getName().equals(name);
	}

	public List<Nanoentity> getSecondNanoentities() {
		return Collections.unmodifiableList(secondNanoentities);
	}

	public void addSecondNanoentity(final Nanoentity entity) {
		secondNanoentities.add(entity);
	}

	public void setSecondNanoentities(final List<Nanoentity> secondNanoentities) {
		this.secondNanoentities.clear();
		if (secondNanoentities != null) {
			this.secondNanoentities.addAll(secondNanoentities);
		}
	}

	public List<Nanoentity> getAllNanoentities() {
		List<Nanoentity> result = new ArrayList<>();
		result.addAll(nanoentities);
		result.addAll(secondNanoentities);
		return result;
	}

	private void setCharacteristicAndCriterion(CouplingCriterionCharacteristic characteristic) {
		Assert.assertNotNull(characteristic);
		this.characteristic = characteristic;
		this.couplingCriterion = characteristic.getCouplingCriterion();
	}

	@Override
	public int compareTo(CouplingInstance o) {
		return couplingCriterion.getName().compareTo(o.getCouplingCriterion().getName());
	}

	public InstanceType getType() {
		return instanceType;
	}

}
