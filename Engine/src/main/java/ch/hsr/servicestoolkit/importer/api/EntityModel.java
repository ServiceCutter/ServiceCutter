package ch.hsr.servicestoolkit.importer.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="name")
public class EntityModel {
	private String name;
	private List<EntityAttribute> attributes;

	// used by Jackson
	public EntityModel(){}
	
	public EntityModel(String name) {
		super();
		this.attributes = new ArrayList<EntityAttribute>();
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void addAttribute(EntityAttribute attribute){
		this.attributes.add(attribute);
	}
	
	public List<EntityAttribute> getAttributes() {
		return attributes;
	}
}
