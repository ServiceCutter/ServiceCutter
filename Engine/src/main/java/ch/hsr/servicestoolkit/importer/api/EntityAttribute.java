package ch.hsr.servicestoolkit.importer.api;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="name")
public class EntityAttribute {
	private String name;

	// used by Jackson
	public EntityAttribute(){}
	
	public EntityAttribute(String name) {
		super();
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
