package ch.hsr.servicecutter.model.solver;

import java.util.List;

import com.google.common.base.Objects;

public class Service {

	private List<String> nanoentities;
	private char id;

	public Service(final List<String> nanoentities, final char id) {
		this.nanoentities = nanoentities;
		this.id = id;
	}

	public Service() {
		// needed for Jackson
	}

	public List<String> getNanoentities() {
		return nanoentities;
	}

	public String getName() {
		return "Service " + id;
	}

	@Override
	public String toString() {
		return "Service " + id + " [nanoentities=" + nanoentities + "]";
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
