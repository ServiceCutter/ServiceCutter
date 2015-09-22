package ch.hsr.servicestoolkit.solver;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gephi.clustering.api.Cluster;
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

import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.Model;
import cz.cvut.fit.krizeji1.girvan_newman.GirvanNewmanClusterer;

public class GephiSolver implements Solver {

	private Model model;
	private Map<String, Node> nodes;
	private SolverConfiguration config;
	private UndirectedGraph undirectedGraph;
	private GraphModel graphModel;

	private Logger log = LoggerFactory.getLogger(GephiSolver.class);

	public GephiSolver(Model model, SolverConfiguration config) {
		if (model == null || model.getDataFields().isEmpty()) {
			throw new InvalidParameterException("invalid model!");
		}
		if (config == null) {
			throw new InvalidParameterException("config should not be null");
		}
		this.model = model;
		this.config = config;
		if (findCouplingCriteria().isEmpty()) {
			throw new InvalidParameterException(
					"model needs at least 1 coupling criterion in order for gephi clusterer to work");
		}
		nodes = new HashMap<>();

		graphModel = bootstrapGephi();
		undirectedGraph = graphModel.getUndirectedGraph();

		buildNodes();
		buildEdges();

		saveAsPdf();

	}

	private void saveAsPdf() {
		// Preview
		PreviewModel previewModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
		previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
		previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGE_LABELS, Boolean.TRUE);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.GRAY));
		previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT,
				previewModel.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(20));

		// Export
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		try {
			ec.exportFile(new File("debug_graph.pdf"));
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
	}

	@Override
	public List<BoundedContext> solve(int numberOfClusters) {
		Log.debug("solve cluster with numberOfClusters = " + numberOfClusters);
		List<BoundedContext> result = new ArrayList<>();

		GirvanNewmanClusterer clusterer = new GirvanNewmanClusterer();
		clusterer.setPreferredNumberOfClusters(numberOfClusters);
		clusterer.execute(graphModel);
		if (clusterer.getClusters() != null) {
			for (Cluster cluster : clusterer.getClusters()) {
				List<String> dataFields = new ArrayList<>();
				for (Node node : cluster.getNodes()) {
					dataFields.add(node.getNodeData().getLabel());
				}
				result.add(new BoundedContext(dataFields));
				log.debug("cluster found: {}", Arrays.toString(cluster.getNodes()));
			}
		}

		return result;
	}

	private Set<CouplingCriterion> findCouplingCriteria() {
		Set<CouplingCriterion> couplingCriteria = new HashSet<>();
		// TODO refactor model of field and criteria
		for (DataField field : model.getDataFields()) {
			for (CouplingCriterion criterion : field.getCouplingCriteria()) {
				if (!couplingCriteria.contains(criterion)) {
					couplingCriteria.add(criterion);
				}
			}
		}
		return couplingCriteria;
	}

	private void buildEdges() {
		for (CouplingCriterion criterion : findCouplingCriteria()) {
			// from every data field in the criterion to every other
			for (int i = 0; i < criterion.getDataFields().size(); i++) {
				for (int j = i + 1; j < criterion.getDataFields().size(); j++) {
					Node nodeA = getNodeByDataField(criterion.getDataFields().get(i));
					Node nodeB = getNodeByDataField(criterion.getDataFields().get(j));
					float weight = config.getWeightForCouplingCriterion(criterion.getCriterionType()).floatValue();
					Edge existingEdge = undirectedGraph.getEdge(nodeA, nodeB);
					if (existingEdge != null) {
						log.debug("add {} to weight of edge from node {} to {}", weight, nodeA, nodeB);
						existingEdge.setWeight(existingEdge.getWeight() + weight);
					} else {
						log.debug("create edge with weight {} from node {} to {}", weight, nodeA, nodeB);
						Edge edge = graphModel.factory().newEdge(nodeA, nodeB, weight, false);
						undirectedGraph.addEdge(edge);
					}
				}
			}
		}
	}

	private Node getNodeByDataField(DataField dataField) {
		return nodes.get(dataField.getName());
	}

	private void buildNodes() {
		// create nodes
		for (DataField field : model.getDataFields()) {
			Node node = graphModel.factory().newNode(field.getName());
			undirectedGraph.addNode(node);
			nodes.put(field.getName(), node);
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
