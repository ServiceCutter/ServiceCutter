package ch.hsr.servicecutter.importer.api;

import java.util.ArrayList;
import java.util.List;

public class UseCase {

	private String name;
	private List<String> nanoentitiesRead = new ArrayList<>();
	private List<String> nanoentitiesWritten = new ArrayList<>();
	private boolean isLatencyCritical = false;

	public UseCase() {
	}

	public UseCase(final String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
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

	public boolean isLatencyCritical() {
		return isLatencyCritical;
	}

	public void setLatencyCritical(final boolean isLatencyCritical) {
		this.isLatencyCritical = isLatencyCritical;
	}

}
