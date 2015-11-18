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
import ch.hsr.servicestoolkit.score.relations.Scorer;
import cz.cvut.fit.krizeji1.girvan_newman.GirvanNewmanClusterer;
import cz.cvut.fit.krizeji1.markov_cluster.MCClusterer;

public class GephiSolver extends AbstractSolver<Node, Edge> {

	public static final String MODE_GIRVAN_NEWMAN = "Girvan-Newman";
	public static final String MODE_MARKOV = "MCL";
	private Map<String, Node> nodes;
	private SolverConfiguration config;
	private UndirectedGraph undirectedGraph;
	private GraphModel graphModel;

	private Logger log = LoggerFactory.getLogger(GephiSolver.class);
	private String mode;
	private Integer numberOfClusters;
	private char serviceIdGenerator = 'A';

	public GephiSolver(final Model model, final Scorer scorer, final SolverConfiguration config, final String mode, final Integer numberOfClusters) {
		super(model, scorer, config);
		this.config = config;
		this.mode = mode;
		this.numberOfClusters = numberOfClusters;
		if (model == null || model.getDataFields().isEmpty()) {
			throw new InvalidParameterException("invalid model!");
		}
		if (config == null) {
			throw new InvalidParameterException("config should not be null");
		}

		nodes = new HashMap<>();

		graphModel = bootstrapGephi();
		undirectedGraph = graphModel.getUndirectedGraph();

		log.info("gephi solver created with config {}", config);
		buildNodes();
		buildEdges();

		log.info("final edges: ");
		for (Edge edge : undirectedGraph.getEdges()) {
			log.info("{}-{}: {}", edge.getSource().getNodeData().getLabel(), edge.getTarget().getNodeData().getLabel(), edge.getWeight());
		}
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

	@Override
	public Set<BoundedContext> solve() {
		if (MODE_GIRVAN_NEWMAN.equals(mode)) {
			return solveWithGirvanNewman(numberOfClusters);
		}
		return solveWithMarkov();
	}

	Set<BoundedContext> solveWithGirvanNewman(final int numberOfClusters) {
		Log.debug("solve cluster with numberOfClusters = " + numberOfClusters);
		GirvanNewmanClusterer clusterer = new GirvanNewmanClusterer();
		clusterer.setPreferredNumberOfClusters(numberOfClusters);
		clusterer.execute(graphModel);
		return getClustererResult(clusterer);
	}

	Set<BoundedContext> solveWithMarkov() {
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
				BoundedContext boundedContext = new BoundedContext(dataFields, serviceIdGenerator++);
				result.add(boundedContext);
				log.debug("BoundedContext found: {}, {}", boundedContext.getDataFields().toString(), boundedContext.hashCode());
			}
		}
		return result;

	}

	@Override
	protected Iterable<Edge> getEdges() {
		return undirectedGraph.getEdges();
	}

	@Override
	protected Edge getEdge(final DataField first, final DataField second) {
		return undirectedGraph.getEdge(getNode(first), getNode(second));
	}

	@Override
	protected void removeEdge(final Edge edge) {
		undirectedGraph.removeEdge(edge);
	}

	@Override
	protected void createEdgeAndSetWeight(final DataField first, final DataField second, final double weight) {
		Edge edge = graphModel.factory().newEdge(getNode(first), getNode(second), (float) weight, false);
		undirectedGraph.addEdge(edge);
		edge.getEdgeData().setLabel(edge.getWeight() + "");
	}

	@Override
	protected double getWeight(final Edge edge) {
		return edge.getWeight();
	}

	@Override
	protected void setWeight(final Edge edge, final double weight) {
		edge.setWeight((float) weight);
		edge.getEdgeData().setLabel(edge.getWeight() + "");
	}

	@Override
	protected Node getNode(final String name) {
		return nodes.get(name);
	}

	@Override
	protected void createNode(final String name) {
		Node node = graphModel.factory().newNode(name);
		node.getNodeData().setLabel(name);
		undirectedGraph.addNode(node);
		nodes.put(name, node);
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
