package ch.hsr.servicecutter.solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import ch.hsr.servicecutter.analyzer.ServiceCutAnalyzer;
import ch.hsr.servicecutter.importer.ImportEndpoint;
import ch.hsr.servicecutter.model.repository.UserSystemRepository;
import ch.hsr.servicecutter.model.solver.EntityPair;
import ch.hsr.servicecutter.model.solver.SolverResult;
import ch.hsr.servicecutter.model.usersystem.UserSystem;
import ch.hsr.servicecutter.rest.InvalidRestParam;
import ch.hsr.servicecutter.scorer.Score;
import ch.hsr.servicecutter.scorer.Scorer;

@Component
@Path("/engine/solver")
public class SolverEndpoint {

	private final Logger log = LoggerFactory.getLogger(SolverEndpoint.class);
	private final UserSystemRepository userSystemRepository;
	private ServiceCutAnalyzer analyzer;
	private Scorer scorer;

	public static final String MODE_GIRVAN_NEWMAN = "Girvan-Newman";
	public static final String MODE_LEUNG = "Leung";
	public static final String[] MODES = new String[] { MODE_GIRVAN_NEWMAN, MODE_LEUNG };

	@Autowired
	public SolverEndpoint(final UserSystemRepository userSystemRepository, final Scorer scorer, final ServiceCutAnalyzer analyzer) {
		this.userSystemRepository = userSystemRepository;
		this.scorer = scorer;
		this.analyzer = analyzer;
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{systemId}")
	@Transactional
	public SolverResult solveSystem(@PathParam("systemId") final Long id) throws JsonGenerationException, JsonMappingException, IOException {
		SolverConfiguration config = new SolverConfiguration();
		Map<String, Double> priorities = new HashMap<>();

		config.setPriorities(priorities);
		return solveSystem(id, config);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{systemId}")
	@Transactional
	public SolverResult solveSystem(@PathParam("systemId") final Long id, final SolverConfiguration config) throws JsonGenerationException, JsonMappingException, IOException {

		SolverResult result = null;
		List<WatchResult> calculationTimes = new ArrayList<>();

		for (long systemNr = 1l; systemNr <= ImportEndpoint.NR_OF_CLONES; systemNr++) {
			// long systemNr = 1;
			UserSystem userSystem = userSystemRepository.findOne(systemNr);
			if (userSystem == null || config == null) {
				return new SolverResult(Collections.emptySet());
			}

			Solver solver = null;
			String algorithm = config.getAlgorithm();
			StopWatch sw = new StopWatch();
			sw.start();

			Map<EntityPair, Map<String, Score>> scores = scorer.getScores(userSystem, (final String key) -> {
				return config.getPriorityForCouplingCriterion(key);
			});
			if (MODE_LEUNG.equals(algorithm)) {
				solver = new GraphStreamSolver(userSystem, scores, config);
			} else if (MODE_GIRVAN_NEWMAN.equals(algorithm)) {
				Integer numberOfClusters = config.getValueForAlgorithmParam("numberOfClusters").intValue();
				solver = new GephiSolver(userSystem, scores, numberOfClusters);
			} else {
				log.error("algorithm {} not found, supported values: {}", algorithm, MODES);
				throw new InvalidRestParam();
			}
			sw.stop();
			long graphCreationTime = sw.getLastTaskTimeMillis();
			log.info("Created graph in {}ms", graphCreationTime);
			sw.start();
			result = solver.solve();
			sw.stop();
			calculationTimes.add(new WatchResult(systemNr, graphCreationTime, sw.getLastTaskTimeMillis()));
			log.info("Found clusters in {}ms", sw.getLastTaskTimeMillis());
			log.info("userSystem {} solved, found {} bounded contexts: {}", userSystem.getId(), result.getServices().size(), result.toString());

			System.gc();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		for (WatchResult r : calculationTimes) {
			log.info("{} {} {}", r.multiplier, r.graphConstruction, r.algorithm);
		}

		// if (result.getServices().size() > 0) {
		// analyzer.analyseResult(result, scores, userSystem);
		// } else {
		// log.warn("No services found!");
		// }

		return result;
	}

	private class WatchResult {
		public WatchResult(final long systemNr, final long graphCreationTime, final long lastTaskTimeMillis) {
			multiplier = systemNr;
			graphConstruction = graphCreationTime;
			algorithm = lastTaskTimeMillis;

		}

		public long multiplier;
		public long graphConstruction;
		public long algorithm;
	}

}
