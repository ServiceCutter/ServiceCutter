package ch.hsr.servicestoolkit.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

@Entity
public class Model {

	@Id
	@GeneratedValue
	private Long id;
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "model")
	private List<ModelEntity> entities = new ArrayList<>();

	public List<ModelEntity> getEntities() {
		return entities;
	}

	public void setEntities(List<ModelEntity> entities) {
		this.entities = entities;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
