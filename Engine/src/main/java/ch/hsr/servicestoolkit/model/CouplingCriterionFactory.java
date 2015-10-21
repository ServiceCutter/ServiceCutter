package ch.hsr.servicestoolkit.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

	public CouplingCriteriaVariant findOrCreateVariant(String coupling, String variant, boolean monoCoupling) {
		CouplingCriterion couplingCriterion = couplingCriterionRepository.readByName(coupling);
		if (couplingCriterion == null) {
			couplingCriterion = new CouplingCriterion();
			couplingCriterion.setName(coupling);
			couplingCriterionRepository.save(couplingCriterion);
		}
		CouplingCriteriaVariant result = couplingCriteriaVariantRepository.readByNameAndCouplingCriterion(variant, couplingCriterion);
		if (result == null) {
			result = new CouplingCriteriaVariant();
			result.setName(variant);
			result.setMonoCoupling(false);
			couplingCriteriaVariantRepository.save(result);
		}
		return result;
	}

	public CouplingCriteriaVariant findOrCreateVariant(String coupling, String variant) {
		return findOrCreateVariant(coupling, variant, true);
	}
}
