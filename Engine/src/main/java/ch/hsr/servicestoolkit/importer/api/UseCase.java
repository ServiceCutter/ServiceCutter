package ch.hsr.servicestoolkit.importer.api;

import java.util.ArrayList;
import java.util.List;

public class UseCase {

	private List<String> nanoentitiesRead = new ArrayList<>();
	private List<String> nanoentitiesWritten = new ArrayList<>();
	private String name;

	public UseCase() {
	}

	public UseCase(final String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public List<String> getNanoentitiesRead() {
		return nanoentitiesRead;
	}

	public void setNanoentitiesRead(final List<String> nanoentitiesRead) {
		if (nanoentitiesRead != null) {
			this.nanoentitiesRead.clear();
			this.nanoentitiesRead.addAll(nanoentitiesRead);
		}
	}

	public List<String> getNanoentitiesWritten() {
		return nanoentitiesWritten;
	}

	public void setNanoentitiesWritten(final List<String> nanoentitiesWritten) {
		if (nanoentitiesWritten != null) {
			this.nanoentitiesWritten.clear();
			this.nanoentitiesWritten.addAll(nanoentitiesWritten);
		}
	}

	@Override
	public String toString() {
		return "UseCase [nanoentitiesRead=" + nanoentitiesRead + ", nanoentitiesWritten=" + nanoentitiesWritten + ", name=" + name + "]";
	}

}
