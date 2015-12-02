package ch.hsr.servicestoolkit.importer.api;

//@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="name")
public class EntityAttribute {
	private String name;

	// used by Jackson
	public EntityAttribute() {
	}

	public EntityAttribute(final String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
