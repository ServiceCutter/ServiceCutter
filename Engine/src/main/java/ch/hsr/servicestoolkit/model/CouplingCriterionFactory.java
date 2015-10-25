package ch.hsr.servicestoolkit.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import ch.hsr.servicestoolkit.repository.CouplingCriteriaVariantRepository;
import ch.hsr.servicestoolkit.repository.CouplingCriterionRepository;

@Component
public class CouplingCriterionFactory {

	private final CouplingCriterionRepository couplingCriterionRepository;
	private final CouplingCriteriaVariantRepository couplingCriteriaVariantRepository;

	@Autowired
	public CouplingCriterionFactory(CouplingCriterionRepository couplingCriterionRepository, CouplingCriteriaVariantRepository couplingCriteriaVariantRepository) {
		this.couplingCriterionRepository = couplingCriterionRepository;
		this.couplingCriteriaVariantRepository = couplingCriteriaVariantRepository;
	}

	public CouplingCriteriaVariant findVariant(String coupling, String variant) {
		CouplingCriterion couplingCriterion = couplingCriterionRepository.readByName(coupling);
		Assert.notNull(couplingCriterion);
		CouplingCriteriaVariant result = couplingCriteriaVariantRepository.readByNameAndCouplingCriterion(variant, couplingCriterion);
		Assert.notNull(result);
		return result;
	}

	public CouplingCriteriaVariant findOrCreateVariant(String coupling, String variant) {
		return findVariant(coupling, variant);
	}
}
