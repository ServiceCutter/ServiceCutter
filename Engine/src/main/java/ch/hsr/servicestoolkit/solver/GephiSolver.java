package ch.hsr.servicestoolkit.solver;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gephi.clustering.api.Cluster;
import org.gephi.clustering.spi.Clusterer;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.jfree.util.Log;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.score.relations.FieldTuple;
import ch.hsr.servicestoolkit.score.relations.Score;
import ch.hsr.servicestoolkit.score.relations.Scorer;
import cz.cvut.fit.krizeji1.girvan_newman.GirvanNewmanClusterer;
import cz.cvut.fit.krizeji1.markov_cluster.MCClusterer;

public class GephiSolver {

	private Model model;
	private Map<String, Node> nodes;
	private SolverConfiguration config;
	private UndirectedGraph undirectedGraph;
	private GraphModel graphModel;

	private Logger log = LoggerFactory.getLogger(GephiSolver.class);
	private Scorer scorer;

	public GephiSolver(final Model model, final Scorer scorer, final SolverConfiguration config) {
		this.scorer = scorer;
		this.config = config;
		if (model == null || model.getDataFields().isEmpty()) {
			throw new InvalidParameterException("invalid model!");
		}
		if (config == null) {
			throw new InvalidParameterException("config should not be null");
		}
		this.model = model;

		nodes = new HashMap<>();

		graphModel = bootstrapGephi();
		undirectedGraph = graphModel.getUndirectedGraph();

		log.info("gephi solver created with config {}", config);
		buildNodes();
		buildEdges();

		saveAsPdf();

	}

	private void saveAsPdf() {

		PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
		PreviewModel previewModel = previewController.getModel();

		// Preview
		previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
		previewModel.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.TRANSLUCENT);
		previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGE_LABELS, Boolean.TRUE);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);

		previewModel.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.GRAY));
		previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.01f));
		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, previewModel.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(50f));

		previewController.refreshPreview();

		// OpenOrdLayout layout = new OpenOrdLayout(new OpenOrdLayoutBuilder());
		// layout.setGraphModel(graphModel);
		// layout.resetPropertiesValues();
		//
		// layout.initAlgo();
		// layout.goAlgo();
		// layout.endAlgo();

		// Export
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		try {
			ec.exportFile(new File("debug_graph.gexf"));
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
	}

	public Set<BoundedContext> solveWithGirvanNewman(final int numberOfClusters) {
		Log.debug("solve cluster with numberOfClusters = " + numberOfClusters);
		GirvanNewmanClusterer clusterer = new GirvanNewmanClusterer();
		clusterer.setPreferredNumberOfClusters(numberOfClusters);
		clusterer.execute(graphModel);
		return getClustererResult(clusterer);
	}

	public Set<BoundedContext> solveWithMarkov() {
		Log.debug("solve cluster with MCL");

		MCClusterer clusterer = new MCClusterer();
		// the higher the more clusters?
		clusterer.setInflation(config.getValueForAlgorithmParam("inflation"));
		clusterer.setExtraClusters(config.getValueForAlgorithmParam("extraClusters") > 0);
		clusterer.setSelfLoop(true); // must be true
		clusterer.setPower(config.getValueForAlgorithmParam("power"));
		clusterer.setPrune(config.getValueForAlgorithmParam("prune"));
		clusterer.execute(graphModel);
		return getClustererResult(clusterer);
	}

	// Returns a HashSet as the algorithms return redundant clusters
	private Set<BoundedContext> getClustererResult(final Clusterer clusterer) {
		Set<BoundedContext> result = new HashSet<>();
		if (clusterer.getClusters() != null) {
			for (Cluster cluster : clusterer.getClusters()) {
				List<String> dataFields = new ArrayList<>();
				for (Node node : cluster.getNodes()) {
					dataFields.add(node.toString());
				}
				BoundedContext boundedContext = new BoundedContext(dataFields);
				result.add(boundedContext);
				log.debug("BoundedContext found: {}, {}", boundedContext.getDataFields().toString(), boundedContext.hashCode());
			}
		}
		return result;

	}

	private void buildEdges() {
		for (Entry<FieldTuple, Map<String, Score>> entry : scorer.getScores(model, config).entrySet()) {
			addWeight(entry.getKey().fieldA, entry.getKey().fieldB, entry.getValue().values().stream().mapToDouble(Score::getPrioritizedScore).sum());
			// Logging
			log.info("Score for field tuple {}", entry.getKey());
			for (Entry<String, Score> criteriaScores : entry.getValue().entrySet()) {
				log.info("{}: {} with priority {} results in {}", criteriaScores.getKey(), criteriaScores.getValue().getScore(), criteriaScores.getValue().getPriority(),
						criteriaScores.getValue().getPrioritizedScore());
			}
			log.info("---------------------------------------------------");
		}

		deleteNegativeEdges();

		log.info("final edges: ");
		for (Edge edge : undirectedGraph.getEdges()) {
			log.info("{}-{}: {}", edge.getSource().getNodeData().getLabel(), edge.getTarget().getNodeData().getLabel(), edge.getWeight());
		}
	}

	private void deleteNegativeEdges() {
		List<Edge> edgesToRemvoe = new ArrayList<>();

		for (Edge edge : undirectedGraph.getEdges()) {
			if (edge.getWeight() <= 0) {
				edgesToRemvoe.add(edge);
			}
		}
		for (Edge edge : edgesToRemvoe) {
			undirectedGraph.removeEdge(edge);
		}
	}

	private void addWeight(final DataField first, final DataField second, final double weight) {
		Node nodeA = getNodeByDataField(first);
		Node nodeB = getNodeByDataField(second);
		Edge existingEdge = undirectedGraph.getEdge(nodeA, nodeB);
		if (existingEdge != null) {
			log.info("add {} to weight of edge from node {} to {}", weight, nodeA, nodeB);
			existingEdge.setWeight((float) (existingEdge.getWeight() + weight));
			existingEdge.getEdgeData().setLabel(existingEdge.getWeight() + "");
		} else {
			log.info("create edge with weight {} from node {} to {}", weight, nodeA, nodeB);
			Edge edge = graphModel.factory().newEdge(nodeA, nodeB, (float) weight, false);
			undirectedGraph.addEdge(edge);
			edge.getEdgeData().setLabel(edge.getWeight() + "");
		}

	}

	private Node getNodeByDataField(final DataField dataField) {
		return nodes.get(dataField.getContextName());
	}

	private void buildNodes() {
		// create nodes
		for (DataField field : model.getDataFields()) {
			String name = field.getContextName();
			Node node = graphModel.factory().newNode(name);
			node.getNodeData().setLabel(name);
			undirectedGraph.addNode(node);
			nodes.put(name, node);
		}
	}

	private GraphModel bootstrapGephi() {
		// boostrap gephi
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		@SuppressWarnings("unused")
		Workspace workspace = pc.getCurrentWorkspace();
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		return graphModel;
	}

}
