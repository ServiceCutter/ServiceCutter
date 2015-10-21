package ch.hsr.servicestoolkit.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.google.common.base.Objects;

@Entity
public class CouplingCriterion {

	@Id
	@GeneratedValue
	private Long id;
	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CouplingCriterion) {
			CouplingCriterion other = (CouplingCriterion) obj;
			return this == other || Objects.equal(id, other.id);
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
