package ch.hsr.servicestoolkit.score;

import java.util.Collection;

import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.model.service.ServiceCut;

public class CouplingCriterionScoring {

	public double calculateScore(ServiceCut cut, CouplingCriterion criterion, CouplingContext context) {
		Collection<MonoCouplingInstance> couplingInstances = context.getCouplingInstances(criterion);
		return calculateScore(cut, couplingInstances.toArray(new MonoCouplingInstance[couplingInstances.size()]));
	}

	private double calculateScore(ServiceCut cut, MonoCouplingInstance... couplingInstances) {
		// only works for proximity!
		double max = 10;
		double nrOfInstances = couplingInstances.length;
		double nrOfSatisfiedInstances = 0;
		for (MonoCouplingInstance monoCouplingInstance : couplingInstances) {
			if (monoCouplingInstance.isSatisfiedBy(cut)) {
				nrOfSatisfiedInstances++;
			}
		}
		return max * (nrOfSatisfiedInstances / nrOfInstances);
	}

}
