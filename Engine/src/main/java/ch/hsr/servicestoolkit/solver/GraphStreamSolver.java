package ch.hsr.servicestoolkit.solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.graphstream.algorithm.community.Leung;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.score.relations.FieldPair;
import ch.hsr.servicestoolkit.score.relations.Score;

public class GraphStreamSolver extends AbstractSolver<Node, Edge> {

	private static final String WEIGHT = "weight";
	private SingleGraph graph;

	private final Logger log = LoggerFactory.getLogger(GraphStreamSolver.class);
	protected double m = 0.1;
	protected double delta = 0.05;
	private int iterations = 1;

	public GraphStreamSolver(final Model model, final Map<FieldPair, Map<String, Score>> scores, final SolverConfiguration config) {
		super(model, scores);
		graph = new SingleGraph("Service Cutter Graph");
		Double m = config.getAlgorithmParams().get("leungM");
		if (m != null) {
			log.info("parameter 'm' is {}", m);
			this.m = m;
		}
		Double delta = config.getAlgorithmParams().get("leungDelta");
		if (delta != null) {
			log.info("parameter 'delta' is {}", delta);
			this.delta = delta;
		}
		Double iterations = config.getAlgorithmParams().get("iterations");
		if (iterations != null) {
			this.iterations = iterations.intValue();
		}
		buildNodes();
		buildEdges();
	}

	@Override
	public SolverResult solve() {
		Leung algorithm = new Leung(graph, null, WEIGHT);
		algorithm.setParameters(m, delta);
		log.info("Using parameters m={} and delta={}", m, delta);
		for (int i = 0; i < iterations; i++) {
			algorithm.compute();
		}
		log.info("Solved with {} iterations ", iterations);
		Map<String, List<String>> families = new HashMap<>();
		for (Node node : graph) {
			String family = node.getAttribute("ui.class");
			families.putIfAbsent(family, new ArrayList<>());
			families.get(family).add(node.getId());
		}
		log.info("found {} families", families.keySet().size());
		try {
			graph.write("graph.xml");
		} catch (IOException e) {
			log.error("error while writing file", e);
		}

		char idGenerator = 'A';
		Set<Service> services = new HashSet<>();
		for (List<String> service : families.values()) {
			services.add(new Service(service, idGenerator++));
		}
		final SolverResult solverResult = new SolverResult(services);
		return solverResult;
	}

	@Override
	protected void createNode(final String name) {
		graph.addNode(name);
	}

	@Override
	protected Node getNode(final String name) {
		return graph.getNode(name);
	}

	@Override
	protected void createEdgeAndSetWeight(final DataField first, final DataField second, final double weight) {
		String firstName = createNodeIdentifier(first);
		String secondName = createNodeIdentifier(second);
		Edge edge = graph.addEdge(createEdgeIdentifier(firstName, secondName), firstName, secondName);
		setWeight(edge, weight);
	}

	String createEdgeIdentifier(final String firstName, final String secondName) {
		return firstName + "-" + secondName;
	}

	@Override
	protected void removeEdge(final Edge edge) {
		graph.removeEdge(edge);
	}

	@Override
	protected Edge getEdge(final DataField first, final DataField second) {
		String firstName = createNodeIdentifier(first);
		String secondName = createNodeIdentifier(second);
		return graph.getEdge(createEdgeIdentifier(firstName, secondName));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Iterable<Edge> getEdges() {
		return (Iterable<Edge>) graph.getEachEdge();
	}

	@Override
	protected double getWeight(final Edge edge) {
		Double weight = edge.getAttribute(WEIGHT);
		return weight != null ? weight : 0;
	}

	@Override
	protected void setWeight(final Edge edge, final double weight) {
		edge.setAttribute(WEIGHT, weight);
	}

}
