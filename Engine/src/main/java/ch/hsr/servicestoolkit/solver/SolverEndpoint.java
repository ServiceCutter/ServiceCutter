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
import org.springframework.util.StopWatch;

import ch.hsr.servicestoolkit.importer.InvalidRestParam;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.repository.ModelRepository;
import ch.hsr.servicestoolkit.repository.MonoCouplingInstanceRepository;
import ch.hsr.servicestoolkit.score.relations.Scorer;

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

		Scorer scorer = new Scorer(monoCouplingInstanceRepository);
		Solver solver = null;
		String algorithm = config.getAlgorithm();
		StopWatch sw = new StopWatch();
		sw.start();
		if ("leung".equals(algorithm)) {
			solver = new GraphStreamSolver(model, scorer, config);
		} else if (GephiSolver.MODE_GIRVAN_NEWMAN.equals(algorithm)) {
			String mode = GephiSolver.MODE_GIRVAN_NEWMAN;
			Integer numberOfClusters = config.getValueForAlgorithmParam("numberOfClusters").intValue();
			solver = new GephiSolver(model, scorer, config, mode, numberOfClusters);
		} else if (GephiSolver.MODE_MARKOV.equals(algorithm)) {
			String mode = GephiSolver.MODE_MARKOV;
			Integer numberOfClusters = config.getValueForAlgorithmParam("numberOfClusters").intValue();
			solver = new GephiSolver(model, scorer, config, mode, numberOfClusters);
		} else {
			log.error("algorith {} not found, supported values: ", algorithm, "leung, giervan, markov");
			throw new InvalidRestParam();
		}
		sw.stop();
		log.info("Created graph in {}ms", sw.getLastTaskTimeMillis());
		sw.start();
		Set<BoundedContext> result = solver.solve();
		sw.stop();
		log.info("Found clusters in {}ms", sw.getLastTaskTimeMillis());
		log.info("model {} solved, found {} bounded contexts: {}", model.getId(), result.size(), result.toString());

		return result;
	}

}
