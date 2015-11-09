package ch.hsr.servicestoolkit.score;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingType;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.model.service.ServiceCut;

public class CouplingCriterionScoring {

	private static final double MAX_SCORE = 10;
	private final Logger logger = LoggerFactory.getLogger(CouplingCriterionScoring.class);

	/**
	 * Calculates the score of a given ServiceCut and a specific
	 * CouplingCriterion.
	 * 
	 * @return a double value between 0 (lowest) and 10 (highest).
	 */
	public double calculateScore(ServiceCut cut, CouplingCriterion criterion, CouplingContext context) {
		// only works for proximity! TODO: enhance for other types
		Assert.isTrue(CouplingType.PROXIMITY.equals(criterion.getType()));
		logger.info("calculating score for {}", criterion);
		// TODO validate that the service cut contains all fields!
		Map<CouplingCriteriaVariant, List<MonoCouplingInstance>> variantsMap = context.getCouplingInstances(criterion);
		double totalScore = 0;
		double totalWeight = 0;
		for (Entry<CouplingCriteriaVariant, List<MonoCouplingInstance>> entry : variantsMap.entrySet()) {
			CouplingCriteriaVariant variant = entry.getKey();
			Collection<MonoCouplingInstance> couplingInstances = entry.getValue();
			for (MonoCouplingInstance couplingInstance : couplingInstances) {
				double localScore = 0;
				if (couplingInstance.isSatisfiedBy(cut)) {
					localScore += MAX_SCORE;
				}
				logger.info("coupling variant {} was evaluated, adding score of {} with weight {}.", variant.getName(), localScore, variant.getWeight());
				totalScore += localScore * variant.getWeight();
				totalWeight += variant.getWeight();
			}
		}
		return totalScore / totalWeight;
	}

}
