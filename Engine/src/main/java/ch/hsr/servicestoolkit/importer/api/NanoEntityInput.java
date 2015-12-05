package ch.hsr.servicestoolkit.importer.api;

//@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="name")
public class NanoEntityInput {
	private String name;

	// used by Jackson
	public NanoEntityInput() {
	}

	public NanoEntityInput(final String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
