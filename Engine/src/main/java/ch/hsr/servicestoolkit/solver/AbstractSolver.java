package ch.hsr.servicestoolkit.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.score.relations.FieldTuple;
import ch.hsr.servicestoolkit.score.relations.Score;
import ch.hsr.servicestoolkit.score.relations.Scorer;

public abstract class AbstractSolver<N, E> implements Solver {

	private Model model;
	private Scorer scorer;
	private final Logger log = LoggerFactory.getLogger(AbstractSolver.class);
	private SolverConfiguration config;

	public AbstractSolver(Model model, Scorer scorer, SolverConfiguration config) {
		this.model = model;
		this.scorer = scorer;
		this.config = config;
		log.info("Created solver of type {}", getClass());
	}

	protected abstract void createNode(String name);

	protected N getNode(DataField dataField) {
		return getNode(createNodeIdentifier(dataField));
	}

	protected abstract N getNode(String name);

	protected abstract void createEdgeAndSetWeight(DataField first, DataField second, double weight);

	protected abstract void removeEdge(E edge);

	protected abstract E getEdge(final DataField first, final DataField second);

	protected abstract Iterable<E> getEdges();

	protected abstract double getWeight(E edge);

	protected abstract void setWeight(E edge, double weight);

	protected void buildNodes() {
		// create nodes
		for (DataField field : model.getDataFields()) {
			createNode(createNodeIdentifier(field));
		}
	}

	protected void buildEdges() {
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

	}

	protected String createNodeIdentifier(DataField field) {
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

	protected void addWeight(final DataField first, final DataField second, final double weight) {
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
