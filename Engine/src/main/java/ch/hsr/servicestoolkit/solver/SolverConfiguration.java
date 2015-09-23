package ch.hsr.servicestoolkit.solver;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.hsr.servicestoolkit.model.CriterionType;

@Component
public class SolverConfiguration {

	private Map<CriterionType, Double> weights = new HashMap<>();

	private Logger log = LoggerFactory.getLogger(SolverConfiguration.class);

	public void setWeights(final Map<CriterionType, Double> weights) {
		if (weights != null) {
			this.weights = weights;
		} else {
			throw new InvalidParameterException("weights should not be null!");
		}
	}

	// needed for jackson deserialization
	public Map<CriterionType, Double> getWeights() {
		return weights;
	}

	public Double getWeightForCouplingCriterion(final CriterionType criterionType) {
		if (!weights.containsKey(criterionType)) {
			log.error("no weight defined for coupling criterion: " + criterionType + ". Use 0");
			return 0d;
		}
		return weights.get(criterionType);
	}
}