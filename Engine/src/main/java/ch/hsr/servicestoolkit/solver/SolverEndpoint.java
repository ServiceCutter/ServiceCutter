package ch.hsr.servicestoolkit.solver;

import java.util.Collections;
import java.util.Set;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.repository.ModelRepository;
import ch.hsr.servicestoolkit.repository.MonoCouplingInstanceRepository;

@Component
@Path("/engine/solver")
public class SolverEndpoint {

	private final Logger log = LoggerFactory.getLogger(SolverEndpoint.class);
	private final ModelRepository modelRepository;
	private MonoCouplingInstanceRepository monoCouplingInstanceRepository;

	@Autowired
	public SolverEndpoint(final ModelRepository modelRepository, final MonoCouplingInstanceRepository monoCouplingInstanceRepository) {
		this.modelRepository = modelRepository;
		this.monoCouplingInstanceRepository = monoCouplingInstanceRepository;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{modelId}")
	@Transactional
	public Set<BoundedContext> solveModel(@PathParam("modelId") final Long id, final SolverConfiguration config) {
		Model model = modelRepository.findOne(id);
		if (model == null || config == null) {
			return Collections.emptySet();
		}

		GephiSolver solver = new GephiSolver(model, config, monoCouplingInstanceRepository);
		// Set<BoundedContext> result = solver.solveWithMarkov();
		Integer numberOfClusters = config.getValueForMCLAlgorithm("numberOfClusters").intValue();
		Set<BoundedContext> result = solver.solveWithGirvanNewman(numberOfClusters);
		log.info("model {} solved, found {} bounded contexts: {}", model.getId(), result.size(), result.toString());
		return result;
	}

}
