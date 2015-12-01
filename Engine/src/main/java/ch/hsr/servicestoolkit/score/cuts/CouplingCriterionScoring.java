package ch.hsr.servicestoolkit.score.cuts;

import static java.util.stream.Collectors.summingDouble;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingType;
import ch.hsr.servicestoolkit.model.NanoEntity;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.model.service.Service;
import ch.hsr.servicestoolkit.model.service.ServiceCut;

public class CouplingCriterionScoring {

	public static final double MAX_SCORE = 10;
	private final Logger logger = LoggerFactory.getLogger(CouplingCriterionScoring.class);

	/**
	 * Calculates the score of a given ServiceCut and a specific
	 * CouplingCriterion.
	 * 
	 * @return a double value between 0 (lowest) and 10 (highest).
	 */
	public double calculateScore(final ServiceCut cut, final CouplingCriterion criterion, final CouplingContext context) {
		logger.info("calculating score for {}", criterion);
		// TODO validate that the service cut contains all fields!
		Map<CouplingCriteriaVariant, List<MonoCouplingInstance>> variantsMap = context.getCouplingInstances(criterion);
		double result = 0;
		if (CouplingType.PROXIMITY.equals(criterion.getType())) {
			result = calculateProximity(cut, variantsMap);
		} else if (CouplingType.DISTANCE.equals(criterion.getType())) {
			result = calculateDistance(cut, variantsMap);
		} else {
			throw new IllegalStateException("Unknown coupling type " + criterion.getType());
		}
		return result;
	}

	private double calculateDistance(final ServiceCut cut, final Map<CouplingCriteriaVariant, List<MonoCouplingInstance>> variantsMap) {
		Collection<Service> services = cut.getServices();
		// init
		Map<Service, List<Integer>> weightsPerService = new HashMap<>();
		for (Service service : services) {
			weightsPerService.put(service, new ArrayList<>());
		}
		// collect weights per service
		for (Entry<CouplingCriteriaVariant, List<MonoCouplingInstance>> e : variantsMap.entrySet()) {
			for (MonoCouplingInstance couplingInstance : e.getValue()) {
				for (NanoEntity dataField : couplingInstance.getDataFields()) {
					Service service = cut.getService(dataField);
					weightsPerService.get(service).add(e.getKey().getWeight());
				}
			}
		}
		// get scores
		double totalScore = 0;
		for (Service service : services) {
			List<Integer> weights = weightsPerService.get(service);
			double sum = weights.stream().collect(summingDouble(f -> f));
			double mean = sum / weights.size();
			double sumOfDeviations = weights.stream().map(v -> Math.abs(mean - v)).collect(summingDouble(f -> f));
			double localScore = MAX_SCORE - (sumOfDeviations * 2) / weights.size();
			totalScore += localScore;
			logger.info("service [{}] was evaluated, adding score of {}.", service.getFieldNames(), localScore);
		}
		return totalScore / services.size();
	}

	private double calculateProximity(final ServiceCut cut, final Map<CouplingCriteriaVariant, List<MonoCouplingInstance>> variantsMap) {
		double result;
		double totalScore = 0;
		double totalWeight = 0;
		for (Entry<CouplingCriteriaVariant, List<MonoCouplingInstance>> entry : variantsMap.entrySet()) {
			CouplingCriteriaVariant variant = entry.getKey();
			Collection<MonoCouplingInstance> couplingInstances = entry.getValue();
			for (MonoCouplingInstance couplingInstance : couplingInstances) {
				double localScore = 0;
				if (couplingInstance.fieldsAreInSameService(cut)) {
					localScore += MAX_SCORE;
				}
				logger.info("coupling variant {} was evaluated, adding score of {} with weight {}.", variant.getName(), localScore, variant.getWeight());
				totalScore += localScore * variant.getWeight();
				totalWeight += variant.getWeight();
			}
		}
		result = totalScore / totalWeight;
		return result;
	}

}
