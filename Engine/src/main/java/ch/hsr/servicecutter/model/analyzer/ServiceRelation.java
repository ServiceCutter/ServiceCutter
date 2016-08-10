package ch.hsr.servicecutter.model.analyzer;

import java.util.Set;

public class ServiceRelation {

	public enum Direction {
		OUTGOING, // service A to service B
		INCOMING, // the other way round
		BIDIRECTIONAL
	}

	private String serviceA;
	private String serviceB;
	private Set<String> sharedEntities;
	private Direction direction;

	public ServiceRelation() {
		// Jackson
	}

	public ServiceRelation(final Set<String> sharedEntities, final String serviceA, final String serviceB, final Direction direction) {
		this.sharedEntities = sharedEntities;
		this.serviceA = serviceA;
		this.serviceB = serviceB;
		this.direction = direction;
	}

	public Set<String> getSharedEntities() {
		return sharedEntities;
	}

	public String getServiceA() {
		return serviceA;
	}

	public String getServiceB() {
		return serviceB;
	}

	public Direction getDirection() {
		return direction;
	}

}
