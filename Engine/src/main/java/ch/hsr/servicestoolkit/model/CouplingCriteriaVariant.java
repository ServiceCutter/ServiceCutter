package ch.hsr.servicestoolkit.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.google.common.base.MoreObjects;

@Entity
public class CouplingCriteriaVariant {

	public static final String AGGREGATION = "Aggregation";
	public static final String COMPOSITION = "Composition";
	public static final String INHERITANCE = "Inheritance";
	public static final String SAME_ENTITY = "Same Entity";
	public static final String SHARED_FIELD_ACCESS = "Shared Field Access";

	@Id
	@GeneratedValue
	private Long id;
	private boolean monoCoupling = true;
	private String name;
	private Integer weight;
	private boolean isDefault = false;
	@ManyToOne
	private CouplingCriterion couplingCriterion;

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public boolean isMonoCoupling() {
		return monoCoupling;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setMonoCoupling(final boolean monoCoupling) {
		this.monoCoupling = monoCoupling;
	}

	public void setDefault(final boolean isDefault) {
		this.isDefault = isDefault;
	}

	public CouplingCriterion getCouplingCriterion() {
		return couplingCriterion;
	}

	public void setCouplingCriterion(final CouplingCriterion couplingCriterion) {
		this.couplingCriterion = couplingCriterion;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public MonoCouplingInstance createInstance() {
		MonoCouplingInstance result = monoCoupling ? new MonoCouplingInstance() : new DualCouplingInstance();
		result.setVariant(this);
		return result;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(final Integer weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this.getClass()).add("id", id).add("name", name).add("weight", weight).toString();
	}
}
