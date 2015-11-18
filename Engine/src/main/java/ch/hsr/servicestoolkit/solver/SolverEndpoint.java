package ch.hsr.servicestoolkit.solver;

import java.util.Collections;
import java.util.Map;

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
import ch.hsr.servicestoolkit.score.relations.FieldTuple;
import ch.hsr.servicestoolkit.score.relations.Score;
import ch.hsr.servicestoolkit.score.relations.Scorer;
import ch.hsr.servicestoolkit.solver.analyzer.ServiceCutAnalyzer;

@Component
@Path("/engine/solver")
public class SolverEndpoint {

	private final Logger log = LoggerFactory.getLogger(SolverEndpoint.class);
	private final ModelRepository modelRepository;
	private MonoCouplingInstanceRepository monoCouplingInstanceRepository;
	private ServiceCutAnalyzer analyzer;

	@Autowired
	public SolverEndpoint(final ModelRepository modelRepository, final MonoCouplingInstanceRepository monoCouplingInstanceRepository, final ServiceCutAnalyzer analyzer) {
		this.modelRepository = modelRepository;
		this.monoCouplingInstanceRepository = monoCouplingInstanceRepository;
		this.analyzer = analyzer;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{modelId}")
	@Transactional
	public SolverResult solveModel(@PathParam("modelId") final Long id, final SolverConfiguration config) {
		Model model = modelRepository.findOne(id);
		if (model == null || config == null) {
			return new SolverResult(Collections.emptySet());
		}

		Scorer scorer = new Scorer(monoCouplingInstanceRepository);
		Solver solver = null;
		String algorithm = config.getAlgorithm();
		StopWatch sw = new StopWatch();
		sw.start();

		Map<FieldTuple, Map<String, Score>> scores = scorer.getScores(model, config);
		if ("leung".equals(algorithm)) {
			solver = new GraphStreamSolver(model, scores, config);
		} else if (GephiSolver.MODE_GIRVAN_NEWMAN.equals(algorithm)) {
			String mode = GephiSolver.MODE_GIRVAN_NEWMAN;
			Integer numberOfClusters = config.getValueForAlgorithmParam("numberOfClusters").intValue();
			solver = new GephiSolver(model, scores, mode, numberOfClusters);
		} else if (GephiSolver.MODE_MARKOV.equals(algorithm)) {
			String mode = GephiSolver.MODE_MARKOV;
			Integer numberOfClusters = config.getValueForAlgorithmParam("numberOfClusters").intValue();
			solver = new GephiSolver(model, scores, mode, numberOfClusters);
		} else {
			log.error("algorith {} not found, supported values: ", algorithm, "leung, giervan, markov");
			throw new InvalidRestParam();
		}
		sw.stop();
		log.info("Created graph in {}ms", sw.getLastTaskTimeMillis());
		sw.start();
		SolverResult result = solver.solve();
		sw.stop();
		log.info("Found clusters in {}ms", sw.getLastTaskTimeMillis());
		log.info("model {} solved, found {} bounded contexts: {}", model.getId(), result.getServices().size(), result.toString());
		analyzer.analyseResult(result, scores, model);
		return result;
	}

}
