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

	@ManyToMany(mappedBy = "dataFields")
	private List<QualityAttribute> qualityAttributes = new ArrayList<>();

	public List<QualityAttribute> getQualityAttributes() {
		return qualityAttributes;
	}

	public void setQualityAttributes(List<QualityAttribute> qualityAttributes) {
		this.qualityAttributes = qualityAttributes;
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

}
