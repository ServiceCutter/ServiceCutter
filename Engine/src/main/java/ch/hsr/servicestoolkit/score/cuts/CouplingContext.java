package ch.hsr.servicestoolkit.score.cuts;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.NanoEntity;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;

/**
 * Describes a model, its fields and the coupling instances. A CouplingContext
 * is required to calculate the score of a ServiceCut.
 */
public class CouplingContext {

	private Model model;
	private List<NanoEntity> dataFields;
	private Collection<MonoCouplingInstance> couplingInstances;

	public CouplingContext(Model model, Collection<MonoCouplingInstance> couplingInstances) {
		this.model = model;
		this.couplingInstances = couplingInstances;
		dataFields = model.getDataFields();
	}

	public Map<CouplingCriteriaVariant, List<MonoCouplingInstance>> getCouplingInstances(CouplingCriterion criterion) {
		return couplingInstances.stream().filter(c -> matchesCriterion(c, criterion)).collect(Collectors.groupingBy(MonoCouplingInstance::getVariant));
	}

	boolean matchesCriterion(MonoCouplingInstance c, CouplingCriterion criterion) {
		return c.getVariant().getCouplingCriterion().equals(criterion);
	}

	public List<NanoEntity> getDataFields() {
		return dataFields;
	}

	public Model getModel() {
		return model;
	}

}
