package ch.hsr.servicestoolkit.solver;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SolverConfiguration {

	private Map<String, Double> weights = new HashMap<>();
	private Map<String, Double> mclParams = new HashMap<>();

	private Logger log = LoggerFactory.getLogger(SolverConfiguration.class);

	public void setWeights(final Map<String, Double> weights) {
		if (weights != null) {
			this.weights = weights;
		} else {
			throw new InvalidParameterException("weights should not be null!");
		}
	}

	public void setMclParams(final Map<String, Double> mclParams) {
		if (mclParams != null) {
			this.mclParams = mclParams;
		} else {
			throw new InvalidParameterException("mclParams should not be null!");
		}
	}

	// needed for jackson deserialization
	public Map<String, Double> getWeights() {
		return weights;
	}

	public Map<String, Double> getMclParams() {
		return mclParams;
	}

	public Double getWeightForCouplingCriterion(final String criterionType) {
		if (!weights.containsKey(criterionType)) {
			log.error("no weight defined for coupling criterion: " + criterionType + ". Use 0");
			return 0d;
		}
		return weights.get(criterionType);
	}

	public Double getValueForMCLAlgorithm(final String key) {
		if (!mclParams.containsKey(key)) {
			log.error("no value defined for algorithm param: " + key + ". Use 0");
			return 0d;
		}
		return mclParams.get(key);
	}

}