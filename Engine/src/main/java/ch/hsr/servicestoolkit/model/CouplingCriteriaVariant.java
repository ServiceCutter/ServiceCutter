package ch.hsr.servicestoolkit.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class CouplingCriteriaVariant {

	@Id
	@GeneratedValue
	private Long id;
	private boolean monoCoupling = true;
	private String name;
	@Enumerated(EnumType.STRING)
	private CouplingType type;
	@ManyToOne
	private CouplingCriterion couplingCriterion;
	public static final String AGGREGATION = "Aggregation";
	public static final String COMPOSITION = "Composition";
	public static final String INHERITANCE = "Inheritance";
	public static final String SAME_ENTITY = "Same Entity";

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isMonoCoupling() {
		return monoCoupling;
	}

	public void setMonoCoupling(boolean monoCoupling) {
		this.monoCoupling = monoCoupling;
	}

	public CouplingType getType() {
		return type;
	}

	public void setType(CouplingType type) {
		this.type = type;
	}

	public CouplingCriterion getCouplingCriterion() {
		return couplingCriterion;
	}

	public void setCouplingCriterion(CouplingCriterion couplingCriterion) {
		this.couplingCriterion = couplingCriterion;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public enum CouplingType {
		PROXIMITY, DISTANCE
	}

	public MonoCouplingInstance createInstance() {
		MonoCouplingInstance result = monoCoupling ? new MonoCouplingInstance() : new DualCouplingInstance();
		result.setCouplingCriteriaVariant(this);
		return result;
	}
}
