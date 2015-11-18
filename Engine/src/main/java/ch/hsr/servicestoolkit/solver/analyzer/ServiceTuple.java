package ch.hsr.servicestoolkit.solver.analyzer;

public class ServiceTuple {

	private String serviceA;
	private String serviceB;

	public ServiceTuple(final String serviceA, final String serviceB) {
		this.serviceA = serviceA;
		this.serviceB = serviceB;
	}

	public ServiceTuple() {
		// Jackson
	}

	public String getServiceA() {
		return serviceA;
	}

	public String getServiceB() {
		return serviceB;
	}

	@Override
	public int hashCode() {
		return getCompareString().hashCode();
	}

	private String getCompareString() {
		String stringToHash;
		if (serviceA.compareTo(serviceB) > 0) {
			stringToHash = serviceA + "-" + serviceB;
		} else {
			stringToHash = serviceB + "-" + serviceA;
		}
		return stringToHash;
	}

	@Override
	public String toString() {
		return getCompareString();
	}

	@Override
	public boolean equals(final Object obj) {
		return getCompareString().equals(((ServiceTuple) obj).getCompareString());
	}

}
