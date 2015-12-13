package ch.hsr.servicestoolkit.score.cuts;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingCriterionCharacteristic;
import ch.hsr.servicestoolkit.model.CouplingInstance;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.Nanoentity;

/**
 * Describes a model, its nanoentities and the coupling instances. A
 * CouplingContext is required to calculate the score of a ServiceCut.
 */
@Deprecated
public class CouplingContext {

	private Model model;
	private List<Nanoentity> nanoentities;
	private Collection<CouplingInstance> couplingInstances;

	public CouplingContext(final Model model, final Collection<CouplingInstance> couplingInstances) {
		this.model = model;
		this.couplingInstances = couplingInstances;
		nanoentities = model.getNanoentities();
	}

	public Map<CouplingCriterionCharacteristic, List<CouplingInstance>> getCouplingInstances(final CouplingCriterion criterion) {
		return couplingInstances.stream().filter(c -> matchesCriterion(c, criterion)).collect(Collectors.groupingBy(CouplingInstance::getCharacteristic));
	}

	boolean matchesCriterion(final CouplingInstance c, final CouplingCriterion criterion) {
		return c.getCouplingCriterion().equals(criterion);
	}

	public List<Nanoentity> getNanoentities() {
		return nanoentities;
	}

	public Model getModel() {
		return model;
	}

}
