package ch.hsr.servicecutter.importer.api;

import java.util.List;

public class NanoentitiesImport {

	List<ImportNanoentity> nanoentities;

	public NanoentitiesImport() {

	}

	public List<ImportNanoentity> getNanoentities() {
		return nanoentities;
	}

	public void setNanoentities(final List<ImportNanoentity> nanoentities) {
		this.nanoentities = nanoentities;
	}
}
