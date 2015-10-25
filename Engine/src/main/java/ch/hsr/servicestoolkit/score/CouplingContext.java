package ch.hsr.servicestoolkit.score;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;

public class CouplingContext {

	private Model model;
	private List<DataField> dataFields;
	private Collection<MonoCouplingInstance> couplingInstances;

	public CouplingContext(Model model, Collection<MonoCouplingInstance> couplingInstances) {
		this.model = model;
		this.couplingInstances = couplingInstances;
		dataFields = model.getDataFields();
	}

	public Collection<MonoCouplingInstance> getCouplingInstances(CouplingCriterion criterion) {
		return couplingInstances.stream().filter(c -> matchesCriterion(c, criterion)).collect(Collectors.toList());
	}

	boolean matchesCriterion(MonoCouplingInstance c, CouplingCriterion criterion) {
		return c.getCouplingCriteriaVariant().getCouplingCriterion().equals(criterion);
	}

}
