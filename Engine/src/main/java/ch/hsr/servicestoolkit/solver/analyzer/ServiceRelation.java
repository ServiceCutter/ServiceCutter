package ch.hsr.servicestoolkit.solver.analyzer;

import java.util.List;

public class ServiceRelation {

	private String serviceA;
	private String serviceB;
	private int score;
	private List<String> sharedFields;

	public ServiceRelation() {
		// Jackson
	}

	public ServiceRelation(final List<String> sharedFields, final Double score, final String serviceA, final String serviceB) {
		this.sharedFields = sharedFields;
		this.score = (int) Math.round(score);
		this.serviceA = serviceA;
		this.serviceB = serviceB;
	}

	public int getScore() {
		return score;
	}

	public List<String> getSharedFields() {
		return sharedFields;
	}

	public String getServiceA() {
		return serviceA;
	}

	public String getServiceB() {
		return serviceB;
	}
}
