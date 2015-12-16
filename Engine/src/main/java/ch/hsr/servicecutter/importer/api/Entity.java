package ch.hsr.servicecutter.importer.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
public class Entity {
	private String name;
	private List<String> nanoentities;

	// used by Jackson
	public Entity() {
	}

	public Entity(final String name) {
		super();
		this.nanoentities = new ArrayList<String>();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addAttribute(final String attribute) {
		this.nanoentities.add(attribute);
	}

	public List<String> getNanoentities() {
		return nanoentities;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this.getClass()).add("name", name).toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Entity) {
			Entity other = (Entity) obj;
			return this == other || Objects.equal(name, other.name);
		} else {
			return false;
		}
	}

}
