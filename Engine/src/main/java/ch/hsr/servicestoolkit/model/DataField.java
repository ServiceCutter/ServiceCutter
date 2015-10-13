package ch.hsr.servicestoolkit.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class DataField {

	@Id
	@GeneratedValue
	private Long id;
	private String name;
	private String context;

	@ManyToOne
	private Model model;

	@ManyToMany(mappedBy = "dataFields")
	private List<CouplingCriterion> couplingCriteria = new ArrayList<>();

	public List<CouplingCriterion> getCouplingCriteria() {
		return couplingCriteria;
	}

	public void setCouplingCriteria(final List<CouplingCriterion> couplingCriteria) {
		this.couplingCriteria.clear();
		if (couplingCriteria != null) {
			this.couplingCriteria.addAll(couplingCriteria);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(final Model model) {
		this.model = model;
	}

	public void addCouplingCriterion(final CouplingCriterion criterion) {
		this.couplingCriteria.add(criterion);
	}

	public String getContext() {
		return context;
	}

	public void setContext(final String context) {
		this.context = context;
	}

	@JsonProperty
	public String getContextName() {
		return (StringUtils.hasText(context) ? context + "." : "") + name;
	}

	@JsonIgnore
	public void setContextName(final String foo) {
		// do nothing
	}

}
