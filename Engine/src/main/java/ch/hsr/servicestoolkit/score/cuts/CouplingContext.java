package ch.hsr.servicestoolkit.score.cuts;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.model.NanoEntity;

/**
 * Describes a model, its fields and the coupling instances. A CouplingContext
 * is required to calculate the score of a ServiceCut.
 */
@Deprecated
public class CouplingContext {

	private Model model;
	private List<NanoEntity> dataFields;
	private Collection<MonoCouplingInstance> couplingInstances;

	public CouplingContext(final Model model, final Collection<MonoCouplingInstance> couplingInstances) {
		this.model = model;
		this.couplingInstances = couplingInstances;
		dataFields = model.getDataFields();
	}

	public Map<CouplingCriteriaVariant, List<MonoCouplingInstance>> getCouplingInstances(final CouplingCriterion criterion) {
		return couplingInstances.stream().filter(c -> matchesCriterion(c, criterion)).collect(Collectors.groupingBy(MonoCouplingInstance::getVariant));
	}

	boolean matchesCriterion(final MonoCouplingInstance c, final CouplingCriterion criterion) {
		return c.getVariant().getCouplingCriterion().equals(criterion);
	}

	public List<NanoEntity> getDataFields() {
		return dataFields;
	}

	public Model getModel() {
		return model;
	}

}
