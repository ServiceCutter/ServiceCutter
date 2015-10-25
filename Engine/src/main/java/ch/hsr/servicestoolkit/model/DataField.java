package ch.hsr.servicestoolkit.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
	@JsonIgnore
	private Model model;

	public DataField() {
	}

	public DataField(String name) {
		this.name = name;
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

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

}
