package ch.hsr.servicestoolkit.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.servicestoolkit.model.NanoEntity;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.score.relations.EntityPair;
import ch.hsr.servicestoolkit.score.relations.Score;

public abstract class AbstractSolver<N, E> implements Solver {

	private Model model;
	private final Logger log = LoggerFactory.getLogger(AbstractSolver.class);
	private Map<EntityPair, Map<String, Score>> scores;

	public AbstractSolver(final Model model, final Map<EntityPair, Map<String, Score>> scores) {
		this.model = model;
		this.scores = scores;
		log.info("Created solver of type {}", getClass());
	}

	protected abstract void createNode(String name);

	protected N getNode(final NanoEntity dataField) {
		return getNode(createNodeIdentifier(dataField));
	}

	protected abstract N getNode(String name);

	protected abstract void createEdgeAndSetWeight(NanoEntity first, NanoEntity second, double weight);

	protected abstract void removeEdge(E edge);

	protected abstract E getEdge(final NanoEntity first, final NanoEntity second);

	protected abstract Iterable<E> getEdges();

	protected abstract double getWeight(E edge);

	protected abstract void setWeight(E edge, double weight);

	protected void buildNodes() {
		// create nodes
		for (NanoEntity field : model.getDataFields()) {
			createNode(createNodeIdentifier(field));
		}
	}

	protected void buildEdges() {
		for (Entry<EntityPair, Map<String, Score>> entry : scores.entrySet()) {
			setWeight(entry.getKey().nanoentityA, entry.getKey().nanoentityB, entry.getValue().values().stream().mapToDouble(Score::getPrioritizedScore).sum());
			// Logging
			log.info("Score for field tuple {}", entry.getKey());
			for (Entry<String, Score> criteriaScores : entry.getValue().entrySet()) {
				log.info("{}: {} with priority {} results in {}", criteriaScores.getKey(), criteriaScores.getValue().getScore(), criteriaScores.getValue().getPriority(),
						criteriaScores.getValue().getPrioritizedScore());
			}
			log.info("---------------------------------------------------");
		}

		deleteNegativeEdges();
	}

	protected String createNodeIdentifier(final NanoEntity field) {
		return field.getContextName();
	}

	private void deleteNegativeEdges() {
		List<E> edgesToRemove = new ArrayList<>();

		for (E edge : getEdges()) {
			if (getWeight(edge) <= 0) {
				edgesToRemove.add(edge);
			}
		}
		log.info("Deleting {} edges with zero or negative weight", edgesToRemove.size());
		for (E edge : edgesToRemove) {
			removeEdge(edge);

		}
	}

	protected void setWeight(final NanoEntity first, final NanoEntity second, final double weight) {
		N nodeA = getNode(first);
		N nodeB = getNode(second);
		E existingEdge = getEdge(first, second);
		if (existingEdge != null) {
			log.info("add {} to weight of edge from node {} to {}", weight, nodeA, nodeB);
			setWeight(existingEdge, weight);
		} else {
			log.info("create edge with weight {} from node {} to {}", weight, nodeA, nodeB);
			createEdgeAndSetWeight(first, second, weight);
		}

	}

}
