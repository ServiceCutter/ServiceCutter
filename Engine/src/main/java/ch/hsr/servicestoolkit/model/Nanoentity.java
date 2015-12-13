package ch.hsr.servicestoolkit.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

@Entity
public class Nanoentity {

	@Id
	@GeneratedValue
	private Long id;
	private String name;
	private String context;
	@ManyToOne
	@JsonIgnore
	private Model model;

	public Nanoentity() {
	}

	public Nanoentity(final String name) {
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

	public void setModel(final Model model) {
		this.model = model;
	}

	@Override
	public String toString() {
		return getContextName();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Nanoentity) {
			Nanoentity other = (Nanoentity) obj;
			return this == other || Objects.equal(id, other.id);
		} else {
			return false;
		}
	}

}
