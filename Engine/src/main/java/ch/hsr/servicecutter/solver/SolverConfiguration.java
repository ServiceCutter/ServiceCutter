package ch.hsr.servicecutter.solver;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.MoreObjects;

@Component
public class SolverConfiguration {

	private Map<String, Double> algorithmParams = new HashMap<>();
	private Map<String, Double> priorities = new HashMap<>();
	private String algorithm = SolverEndpoint.MODE_GIRVAN_NEWMAN; // default

	private Logger log = LoggerFactory.getLogger(SolverConfiguration.class);

	public SolverConfiguration() {
	}

	public void setAlgorithmParams(final Map<String, Double> mclParams) {
		if (mclParams != null) {
			this.algorithmParams = mclParams;
		} else {
			throw new InvalidParameterException("mclParams should not be null!");
		}
	}

	public Map<String, Double> getPriorities() {
		return priorities;
	}

	public void setPriorities(final Map<String, Double> priorities) {
		if (priorities == null) {
			throw new InvalidParameterException();
		}
		this.priorities = priorities;
	}

	public Map<String, Double> getAlgorithmParams() {
		return algorithmParams;
	}

	public Double getPriorityForCouplingCriterion(final String criterionType) {
		if (!priorities.containsKey(criterionType)) {
			log.error("no priority defined for couplingCriterion: " + criterionType + ". Use 1");
			return 1d;
		}
		return priorities.get(criterionType);
	}

	public Double getValueForAlgorithmParam(final String key) {
		if (!algorithmParams.containsKey(key)) {
			log.error("no value defined for algorithm param: " + key + ". Use 1");
			return 1d;
		}
		return algorithmParams.get(key);
	}

	public void setAlgorithm(final String algorithm) {
		this.algorithm = algorithm;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(getClass()).add("algorithm", algorithm).add("algorithmParams", algorithmParams).add("priorities", priorities).toString();
	}

}