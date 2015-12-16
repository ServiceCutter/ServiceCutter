package ch.hsr.servicestoolkit.couplingcriteria;

import java.util.List;

import ch.hsr.servicestoolkit.model.criteria.CouplingCriterion;
import ch.hsr.servicestoolkit.model.criteria.CouplingCriterionCharacteristic;
import ch.hsr.servicestoolkit.model.criteria.CouplingType;

public class CouplingCriterionDTO implements Comparable<CouplingCriterionDTO> {

	private final Long id;
	private final String code;
	private final String name;
	private final String description;
	private final List<CouplingCriterionCharacteristic> characteristics;
	private final String decompositionImpact;
	private final CouplingType type;

	public CouplingCriterionDTO(final CouplingCriterion couplingCriterion, final List<CouplingCriterionCharacteristic> characteristics) {
		this.characteristics = characteristics;
		this.name = couplingCriterion.getName();
		this.id = couplingCriterion.getId();
		this.description = couplingCriterion.getDescription();
		decompositionImpact = couplingCriterion.getDecompositionImpact();
		type = couplingCriterion.getType();
		code = couplingCriterion.getCode();
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<CouplingCriterionCharacteristic> getCharacteristics() {
		return characteristics;
	}

	public String getDescription() {
		return description;
	}

	public String getDecompositionImpact() {
		return decompositionImpact;
	}

	public CouplingType getType() {
		return type;
	}

	public String getCode() {
		return code;
	}

	@Override
	public int compareTo(final CouplingCriterionDTO o) {
		Integer thisNumber = new Integer(code.split("-")[1]);
		Integer otherNumber = new Integer(o.getCode().split("-")[1]);
		return thisNumber.compareTo(otherNumber);
	}

}
