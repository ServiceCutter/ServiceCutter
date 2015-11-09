package ch.hsr.servicestoolkit.score;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;

/**
 * Describes a model, its fields and the coupling instances. A CouplingContext
 * is required to calculate the score of a ServiceCut.
 */
public class CouplingContext {

	private Model model;
	private List<DataField> dataFields;
	private Collection<MonoCouplingInstance> couplingInstances;

	public CouplingContext(Model model, Collection<MonoCouplingInstance> couplingInstances) {
		this.model = model;
		this.couplingInstances = couplingInstances;
		dataFields = model.getDataFields();
	}

	public Map<CouplingCriteriaVariant, List<MonoCouplingInstance>> getCouplingInstances(CouplingCriterion criterion) {
		return couplingInstances.stream().filter(c -> matchesCriterion(c, criterion)).collect(Collectors.groupingBy(MonoCouplingInstance::getCouplingCriteriaVariant));
	}

	boolean matchesCriterion(MonoCouplingInstance c, CouplingCriterion criterion) {
		return c.getCouplingCriteriaVariant().getCouplingCriterion().equals(criterion);
	}

}
