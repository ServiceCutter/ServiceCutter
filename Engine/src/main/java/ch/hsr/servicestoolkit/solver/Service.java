package ch.hsr.servicestoolkit.solver;

import java.util.List;

import com.google.common.base.Objects;

public class Service {

	private List<String> dataFields;
	private char id;

	public Service(final List<String> dataFields, final char id) {
		this.dataFields = dataFields;
		this.id = id;
	}

	public Service() {
		// needed for Jackson
	}

	public List<String> getDataFields() {
		return dataFields;
	}

	public String getName() {
		return "Service " + id;
	}

	@Override
	public String toString() {
		return "Service " + id + " [dataFields=" + dataFields + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Service) {
			Service other = (Service) obj;
			return this == other || Objects.equal(id, other.id);
		} else {
			return false;
		}
	}
}
