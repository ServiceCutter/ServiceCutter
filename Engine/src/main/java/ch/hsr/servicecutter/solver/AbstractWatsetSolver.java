package ch.hsr.servicecutter.solver;

import ch.hsr.servicecutter.model.solver.EntityPair;
import ch.hsr.servicecutter.model.solver.Service;
import ch.hsr.servicecutter.model.solver.SolverResult;
import ch.hsr.servicecutter.model.usersystem.Nanoentity;
import ch.hsr.servicecutter.model.usersystem.UserSystem;
import ch.hsr.servicecutter.scorer.Score;
import com.google.common.collect.Lists;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.nlpub.watset.graph.Clustering;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractWatsetSolver extends AbstractSolver<String, DefaultWeightedEdge> {

    protected final Graph<String, DefaultWeightedEdge> graph;

    public AbstractWatsetSolver(UserSystem userSystem, Map<EntityPair, Map<String, Score>> scores) {
        super(userSystem, scores);

        graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        buildNodes();
        buildEdges();
    }

    /**
     * Override this method to define which watset (https://github.com/nlpub/watset-java) algorithm is taken
     */
    protected abstract Clustering<String> getAlgorithm();

    @Override
    protected void createNode(String name) {
        graph.addVertex(name);
    }

    @Override
    protected String getNode(String name) {
        return name;
    }

    @Override
    protected void createEdgeAndSetWeight(Nanoentity first, Nanoentity second, double weight) {
        graph.addEdge(createNodeIdentifier(first), createNodeIdentifier(second));
        graph.setEdgeWeight(createNodeIdentifier(first), createNodeIdentifier(second), weight);
    }

    @Override
    protected void removeEdge(DefaultWeightedEdge edge) {
        graph.removeEdge(edge);
    }

    @Override
    protected DefaultWeightedEdge getEdge(Nanoentity first, Nanoentity second) {
        return graph.getEdge(createNodeIdentifier(first), createNodeIdentifier(second));
    }

    @Override
    protected Iterable<DefaultWeightedEdge> getEdges() {
        return graph.edgeSet();
    }

    @Override
    protected double getWeight(DefaultWeightedEdge edge) {
        return graph.getEdgeWeight(edge);
    }

    @Override
    protected void setWeight(DefaultWeightedEdge edge, double weight) {
        graph.setEdgeWeight(edge, weight);
    }

    @Override
    public SolverResult solve() {
        Clustering<String> alg = getAlgorithm();
        alg.fit();
        Collection<Collection<String>> clusterSet = alg.getClusters();
        Set<Service> services = new HashSet<>();
        char idGenerator = 'A';
        for (Collection<String> cluster : clusterSet) {
            services.add(new Service(Lists.newLinkedList(cluster), idGenerator++));
        }
        return new SolverResult(services);
    }
}
