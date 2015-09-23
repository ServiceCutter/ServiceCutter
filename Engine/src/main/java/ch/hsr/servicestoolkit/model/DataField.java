package ch.hsr.servicestoolkit.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class DataField {

	@Id
	@GeneratedValue
	private Long id;
	private String name;
	private String context;

	@ManyToMany(mappedBy = "dataFields")
	private List<CouplingCriterion> couplingCriteria = new ArrayList<>();

	public List<CouplingCriterion> getCouplingCriteria() {
		return couplingCriteria;
	}

	public void setCouplingCriteria(List<CouplingCriterion> couplingCriteria) {
		this.couplingCriteria.clear();
		if (couplingCriteria != null) {
			this.couplingCriteria.addAll(couplingCriteria);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void addCouplingCriterion(CouplingCriterion criterion) {
		this.couplingCriteria.add(criterion);
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getContextName() {
		return context + "." + name;
	}

}
