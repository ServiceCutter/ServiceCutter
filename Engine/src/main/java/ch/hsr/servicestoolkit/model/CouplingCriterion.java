package ch.hsr.servicestoolkit.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

@Entity
public class CouplingCriterion {

	public static final String SEMANTIC_PROXIMITY = "Semantic Proximity";
	public static final String IDENTITY_LIFECYCLE = "Identity & Lifecycle Commonality";

	@Id
	@GeneratedValue
	private Long id;
	private String name;
	private String description;
	private String decompositionImpact;
	@Enumerated(EnumType.STRING)
	private CouplingType type;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDecompositionImpact() {
		return decompositionImpact;
	}

	public void setDecompositionImpact(String decompositionImpact) {
		this.decompositionImpact = decompositionImpact;
	}

	public CouplingType getType() {
		return type;
	}

	public void setType(CouplingType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this.getClass()).add("id", id).add("name", name).add("type", type).toString();
	}

}
