package ch.hsr.servicecutter.importer.api;

import java.util.List;

public class RelatedGroup {

	private String name;
	private List<String> nanoentities;

	// Jackson
	public RelatedGroup() {

	}

	public RelatedGroup(final List<String> nanoentities, final String name) {
		super();
		this.nanoentities = nanoentities;
		this.name = name;
	}

	public List<String> getNanoentities() {
		return nanoentities;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setNanoentities(final List<String> nanoentities) {
		this.nanoentities = nanoentities;
	}

}
