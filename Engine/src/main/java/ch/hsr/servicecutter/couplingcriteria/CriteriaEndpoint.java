package ch.hsr.servicecutter.couplingcriteria;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.hsr.servicecutter.model.criteria.CouplingCriterion;
import ch.hsr.servicecutter.model.repository.CouplingCriterionCharacteristicRepository;
import ch.hsr.servicecutter.model.repository.CouplingCriterionRepository;

@Component
@Path("/engine")
public class CriteriaEndpoint {

	private CouplingCriterionRepository couplingCriterionRepository;
	private CouplingCriterionCharacteristicRepository couplingCriteriaCharacteristicRepository;

	@Autowired
	public CriteriaEndpoint(final CouplingCriterionRepository couplingCriterionRepository,
			final CouplingCriterionCharacteristicRepository couplingCriteriaCharacteristicRepository) {
		this.couplingCriterionRepository = couplingCriterionRepository;
		this.couplingCriteriaCharacteristicRepository = couplingCriteriaCharacteristicRepository;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/couplingcriteria")
	@Transactional
	public List<CouplingCriterionDTO> getCouplingCriteria() {
		Stream<CouplingCriterion> criteria = StreamSupport.stream(couplingCriterionRepository.findAll().spliterator(), false);
		List<CouplingCriterionDTO> result = criteria.map(criterion -> {
			return new CouplingCriterionDTO(criterion, couplingCriteriaCharacteristicRepository.readByCouplingCriterion(criterion));
		}).sorted().collect(Collectors.toList());
		return result;
	}

}
