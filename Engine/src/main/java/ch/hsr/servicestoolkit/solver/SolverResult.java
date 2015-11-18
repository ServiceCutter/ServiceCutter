package ch.hsr.servicestoolkit.solver;

import java.util.Set;

public class SolverResult {

	private Set<Service> services;

	// Jackson
	public SolverResult() {
	}

	public SolverResult(final Set<Service> services) {
		this.services = services;
	}

	public Set<Service> getServices() {
		return services;
	}
}
