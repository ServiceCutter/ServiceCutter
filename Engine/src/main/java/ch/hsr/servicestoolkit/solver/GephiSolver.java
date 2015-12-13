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

import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.NanoEntity;
import ch.hsr.servicestoolkit.score.relations.EntityPair;
import ch.hsr.servicestoolkit.score.relations.Score;
import cz.cvut.fit.krizeji1.girvan_newman.GirvanNewmanClusterer;

public class GephiSolver extends AbstractSolver<Node, Edge> {

	private Map<String, Node> nodes;
	private UndirectedGraph undirectedGraph;
	private GraphModel graphModel;

	private Logger log = LoggerFactory.getLogger(GephiSolver.class);
	private Integer numberOfClusters;
	private char serviceIdGenerator = 'A';

	public GephiSolver(final Model model, final Map<EntityPair, Map<String, Score>> scores, final Integer numberOfClusters) {
		super(model, scores);
		this.numberOfClusters = numberOfClusters;
		if (model == null || model.getDataFields().isEmpty()) {
			throw new InvalidParameterException("invalid model!");
		}

		nodes = new HashMap<>();

		graphModel = bootstrapGephi();
		undirectedGraph = graphModel.getUndirectedGraph();

		log.info("gephi solver created");
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

		// Export
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		try {
			ec.exportFile(new File("debug_graph.gexf"));
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SolverResult solve() {
		return solveWithGirvanNewman(numberOfClusters);
	}

	SolverResult solveWithGirvanNewman(final int numberOfClusters) {
		Log.debug("solve cluster with numberOfClusters = " + numberOfClusters);
		GirvanNewmanClusterer clusterer = new GirvanNewmanClusterer();
		clusterer.setPreferredNumberOfClusters(numberOfClusters);
		clusterer.execute(graphModel);
		SolverResult solverResult = new SolverResult(getClustererResult(clusterer));
		return solverResult;
	}

	// Returns a HashSet as the algorithms return redundant clusters
	private Set<Service> getClustererResult(final Clusterer clusterer) {
		Set<Service> result = new HashSet<>();
		if (clusterer.getClusters() != null) {
			for (Cluster cluster : clusterer.getClusters()) {
				List<String> dataFields = new ArrayList<>();
				for (Node node : cluster.getNodes()) {
					dataFields.add(node.toString());
				}
				Service boundedContext = new Service(dataFields, serviceIdGenerator++);
				result.add(boundedContext);
				log.debug("BoundedContext found: {}, {}", boundedContext.getNanoentities().toString(), boundedContext.hashCode());
			}
		}
		return result;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Iterable<Edge> getEdges() {
		return undirectedGraph.getEdges();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Edge getEdge(final NanoEntity first, final NanoEntity second) {
		return undirectedGraph.getEdge(getNode(first), getNode(second));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void removeEdge(final Edge edge) {
		undirectedGraph.removeEdge(edge);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createEdgeAndSetWeight(final NanoEntity first, final NanoEntity second, final double weight) {
		Edge edge = graphModel.factory().newEdge(getNode(first), getNode(second), (float) weight, false);
		undirectedGraph.addEdge(edge);
		edge.getEdgeData().setLabel(edge.getWeight() + "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected double getWeight(final Edge edge) {
		return edge.getWeight();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setWeight(final Edge edge, final double weight) {
		edge.setWeight((float) weight);
		edge.getEdgeData().setLabel(edge.getWeight() + "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Node getNode(final String name) {
		return nodes.get(name);
	}

	/**
	 * {@inheritDoc}
	 */
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
