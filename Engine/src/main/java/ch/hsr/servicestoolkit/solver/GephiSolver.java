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
import java.util.stream.Collectors;

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

import ch.hsr.servicestoolkit.model.CouplingType;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.repository.MonoCouplingInstanceRepository;
import cz.cvut.fit.krizeji1.girvan_newman.GirvanNewmanClusterer;
import cz.cvut.fit.krizeji1.markov_cluster.MCClusterer;

public class GephiSolver {

	private Model model;
	private Map<String, Node> nodes;
	private SolverConfiguration config;
	private UndirectedGraph undirectedGraph;
	private GraphModel graphModel;
	private final MonoCouplingInstanceRepository monoCouplingInstanceRepository;

	private Logger log = LoggerFactory.getLogger(GephiSolver.class);

	public GephiSolver(final Model model, final SolverConfiguration config, final MonoCouplingInstanceRepository monoCouplingInstanceRepository) {
		this.monoCouplingInstanceRepository = monoCouplingInstanceRepository;
		if (model == null || model.getDataFields().isEmpty()) {
			throw new InvalidParameterException("invalid model!");
		}
		if (config == null) {
			throw new InvalidParameterException("config should not be null");
		}
		this.model = model;
		this.config = config;
		if (findCouplingCriteria().isEmpty()) {
			throw new InvalidParameterException("model needs at least 1 coupling criterion in order for gephi clusterer to work");
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
		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, previewModel.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(20));

		// Export
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		try {
			ec.exportFile(new File("debug_graph.pdf"));
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

	private Set<MonoCouplingInstance> findCouplingCriteria() {
		return new HashSet<>(monoCouplingInstanceRepository.findByModel(model));
	}

	private Map<String, List<MonoCouplingInstance>> getInstancesByCriterion(final List<MonoCouplingInstance> instances) {
		Map<String, List<MonoCouplingInstance>> instancesByCriterion = new HashMap<>();
		for (MonoCouplingInstance instance : instances) {
			String ccName = instance.getVariant().getCouplingCriterion().getName();
			if (instancesByCriterion.get(ccName) == null) {
				instancesByCriterion.put(ccName, new ArrayList<MonoCouplingInstance>());
			}
			instancesByCriterion.get(ccName).add(instance);
		}
		return instancesByCriterion;
	}

	private void buildEdges() {
		buildDistanceEdges();
		for (MonoCouplingInstance instance : findCouplingCriteria()) {
			// from every data field in the criterion to every other
			List<DataField> dataFields = instance.getAllFields();
			float weight = config.getWeightForVariant(instance.getVariant().getName()).floatValue();
			for (int i = 0; i < dataFields.size(); i++) {
				for (int j = i + 1; j < dataFields.size(); j++) {
					if (weight > 0) {
						addWeight(dataFields.get(i), dataFields.get(j), weight);
					}
				}
			}
		}
	}

	private void buildDistanceEdges() {
		// get all instances of distance variants
		List<MonoCouplingInstance> distanceCriteriaInstances = findCouplingCriteriaByType(CouplingType.DISTANCE);
		// get all instances group by distance CC
		for (Entry<String, List<MonoCouplingInstance>> instancesEntry : getInstancesByCriterion(distanceCriteriaInstances).entrySet()) {
			// compare all variants with each other
			List<MonoCouplingInstance> instances = instancesEntry.getValue();
			Double priority = config.getPriorityForCouplingCriterion(instances.get(0).getVariant().getCouplingCriterion().getName());
			for (int i = 0; i < instances.size() - 1; i++) {
				for (int j = i + 1; j < instances.size(); j++) {
					// for all fields in two different variants, calculate the
					// distance
					for (DataField fieldFromI : instances.get(i).getAllFields()) {
						for (DataField fieldFromJ : instances.get(j).getAllFields()) {
							int distance = Math.abs(instances.get(i).getVariant().getWeight() - instances.get(j).getVariant().getWeight());
							if (distance != 0) {
								addWeight(fieldFromI, fieldFromJ, -(distance * priority));
							}

						}
					}
				}
			}
		}
	}

	private List<MonoCouplingInstance> findCouplingCriteriaByType(final CouplingType type) {
		List<MonoCouplingInstance> distanceCriteriaInstances = findCouplingCriteria().stream().filter(instance -> type.equals(instance.getVariant().getCouplingCriterion()))
				.collect(Collectors.toList());
		return distanceCriteriaInstances;
	}

	private void addWeight(final DataField first, final DataField second, final double weight) {
		Node nodeA = getNodeByDataField(first);
		Node nodeB = getNodeByDataField(second);
		Edge existingEdge = undirectedGraph.getEdge(nodeA, nodeB);
		if (existingEdge != null) {
			log.info("add {} to weight of edge from node {} to {}", weight, nodeA, nodeB);
			existingEdge.setWeight((float) (existingEdge.getWeight() + weight));
		} else {
			log.info("create edge with weight {} from node {} to {}", weight, nodeA, nodeB);
			Edge edge = graphModel.factory().newEdge(nodeA, nodeB, (float) weight, false);
			undirectedGraph.addEdge(edge);
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
