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

import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.repository.ModelRepository;
import ch.hsr.servicestoolkit.rest.InvalidRestParam;
import ch.hsr.servicestoolkit.score.relations.EntityPair;
import ch.hsr.servicestoolkit.score.relations.Score;
import ch.hsr.servicestoolkit.score.relations.Scorer;
import ch.hsr.servicestoolkit.solver.analyzer.ServiceCutAnalyzer;

@Component
@Path("/engine/solver")
public class SolverEndpoint {

	private final Logger log = LoggerFactory.getLogger(SolverEndpoint.class);
	private final ModelRepository modelRepository;
	private ServiceCutAnalyzer analyzer;
	private Scorer scorer;

	public static final String MODE_GIRVAN_NEWMAN = "Girvan-Newman";
	public static final String MODE_LEUNG = "Leung";
	public static final String[] MODES = new String[] { MODE_GIRVAN_NEWMAN, MODE_LEUNG };

	@Autowired
	public SolverEndpoint(final ModelRepository modelRepository, final Scorer scorer, final ServiceCutAnalyzer analyzer) {
		this.modelRepository = modelRepository;
		this.scorer = scorer;
		this.analyzer = analyzer;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{modelId}")
	@Transactional
	public SolverResult solveModel(@PathParam("modelId") final Long id, final SolverConfiguration config) {
		Model model = modelRepository.findOne(id);
		if (model == null || config == null || config.getPriorities().isEmpty()) {
			return new SolverResult(Collections.emptySet());
		}

		Solver solver = null;
		String algorithm = config.getAlgorithm();
		StopWatch sw = new StopWatch();
		sw.start();

		Map<EntityPair, Map<String, Score>> scores = scorer.getScores(model, (final String key) -> {
			return config.getPriorityForCouplingCriterion(key);
		});
		if (MODE_LEUNG.equals(algorithm)) {
			solver = new GraphStreamSolver(model, scores, config);
		} else if (MODE_GIRVAN_NEWMAN.equals(algorithm)) {
			Integer numberOfClusters = config.getValueForAlgorithmParam("numberOfClusters").intValue();
			solver = new GephiSolver(model, scores, numberOfClusters);
		} else {
			log.error("algorithm {} not found, supported values: {}", algorithm, MODES);
			throw new InvalidRestParam();
		}
		sw.stop();
		log.info("Created graph in {}ms", sw.getLastTaskTimeMillis());
		sw.start();
		SolverResult result = solver.solve();
		sw.stop();
		log.info("Found clusters in {}ms", sw.getLastTaskTimeMillis());
		log.info("model {} solved, found {} bounded contexts: {}", model.getId(), result.getServices().size(), result.toString());
		if (result.getServices().size() > 0) {
			analyzer.analyseResult(result, scores, model);
		} else {
			log.warn("No services found!");
		}
		return result;
	}

}
