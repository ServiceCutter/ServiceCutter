package ch.hsr.servicecutter.model.analyzer;

import java.util.List;

public class ServiceRelation {

	private String serviceA;
	private String serviceB;
	private List<String> sharedEntities;

	public ServiceRelation() {
		// Jackson
	}

	public ServiceRelation(final List<String> sharedEntities, final String serviceA, final String serviceB) {
		this.sharedEntities = sharedEntities;
		this.serviceA = serviceA;
		this.serviceB = serviceB;
	}

	public List<String> getSharedEntities() {
		return sharedEntities;
	}

	public String getServiceA() {
		return serviceA;
	}

	public String getServiceB() {
		return serviceB;
	}
}
