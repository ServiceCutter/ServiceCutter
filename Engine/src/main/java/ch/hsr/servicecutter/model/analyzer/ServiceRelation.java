package ch.hsr.servicecutter.model.analyzer;

import java.util.List;

public class ServiceRelation {

	private String serviceA;
	private String serviceB;
	private int score;
	private List<String> sharedEntities;

	public ServiceRelation() {
		// Jackson
	}

	public ServiceRelation(final List<String> sharedEntities, final Double score, final String serviceA, final String serviceB) {
		this.sharedEntities = sharedEntities;
		this.score = (int) Math.round(score);
		this.serviceA = serviceA;
		this.serviceB = serviceB;
	}

	public int getScore() {
		return score;
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
