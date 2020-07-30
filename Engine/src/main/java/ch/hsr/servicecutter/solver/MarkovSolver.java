package ch.hsr.servicecutter.solver;

import ch.hsr.servicecutter.model.solver.EntityPair;
import ch.hsr.servicecutter.model.usersystem.UserSystem;
import ch.hsr.servicecutter.scorer.Score;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.Clustering;
import org.nlpub.watset.graph.MarkovClustering;

import java.util.Map;

public class MarkovSolver extends AbstractWatsetSolver {

    private int expansionOperations;
    private double powerCoefficient;

    public MarkovSolver(UserSystem userSystem, Map<EntityPair, Map<String, Score>> scores, SolverConfiguration config) {
        super(userSystem, scores);
        this.expansionOperations = config.getValueForAlgorithmParam("mclExpansionOperations").intValue();
        this.powerCoefficient = config.getValueForAlgorithmParam("mclPowerCoefficient").doubleValue();
    }

    @Override
    protected Clustering<String> getAlgorithm() {
        return new MarkovClustering<String, DefaultWeightedEdge>(graph, expansionOperations, powerCoefficient);
    }
}
