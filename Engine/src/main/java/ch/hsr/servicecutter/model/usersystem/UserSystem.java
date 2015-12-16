package ch.hsr.servicecutter.model.usersystem;

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
public class UserSystem {

	@Id
	@GeneratedValue
	private Long id;
	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "userSystem", fetch = FetchType.EAGER)
	private List<Nanoentity> nanoentities = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "userSystem")
	private List<CouplingInstance> couplingInstances = new ArrayList<>();

	public List<Nanoentity> getNanoentities() {
		return Collections.unmodifiableList(nanoentities);
	}

	public void addNanoentity(final Nanoentity nanoentity) {
		nanoentities.add(nanoentity);
		nanoentity.setUserSystem(this);
	}

	public void addCouplingInstance(final CouplingInstance instance) {
		couplingInstances.add(instance);
		instance.setSystem(this);
	}

	public List<CouplingInstance> getCouplingInstances() {
		return couplingInstances;
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
		if (obj instanceof UserSystem) {
			UserSystem other = (UserSystem) obj;
			return this == other || Objects.equal(id, other.id);
		} else {
			return false;
		}
	}

}
