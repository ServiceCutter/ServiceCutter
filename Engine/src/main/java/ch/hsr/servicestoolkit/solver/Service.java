package ch.hsr.servicestoolkit.solver;

import java.util.List;

import com.google.common.base.Objects;

public class Service {

	List<String> dataFields;
	String name;

	public Service(final List<String> dataFields, final char id) {
		super();
		this.dataFields = dataFields;
		this.name = "Service " + id;
	}

	public Service() {
		// needed for Jackson
	}

	public List<String> getDataFields() {
		return dataFields;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "BoundedContext [dataFields=" + dataFields + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Service) {
			Service other = (Service) obj;
			return this == other || Objects.equal(name, other.name);
		} else {
			return false;
		}
	}

}
