package ch.hsr.servicestoolkit.importer.api;

import java.util.List;

public class CohesiveGroup {

	private List<String> nanoEntities;
	private String name;

	// Jackson
	public CohesiveGroup() {

	}

	public CohesiveGroup(final List<String> nanoEntities, final String name) {
		super();
		this.nanoEntities = nanoEntities;
		this.name = name;
	}

	public List<String> getNanoEntities() {
		return nanoEntities;
	}

	public String getName() {
		return name;
	}

}
