package ch.hsr.servicecutter.solver;

import ch.hsr.servicecutter.model.solver.EntityPair;
import ch.hsr.servicecutter.model.usersystem.UserSystem;
import ch.hsr.servicecutter.scorer.Score;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.ChineseWhispers;
import org.nlpub.watset.graph.Clustering;
import org.nlpub.watset.graph.NodeWeighting;

import java.util.Map;

public class ChineseWhispersSolver extends AbstractWatsetSolver {

    private final NodeWeighting nodeWeighting;

    public ChineseWhispersSolver(UserSystem userSystem, Map<EntityPair, Map<String, Score>> scores, SolverConfiguration config) {
        super(userSystem, scores);
        this.nodeWeighting = mapNodeWeightingConfig(config.getValueForAlgorithmParam("cwNodeWeighting").intValue());
    }

    @Override
    protected Clustering<String> getAlgorithm() {
        return new ChineseWhispers<String, DefaultWeightedEdge>(graph, nodeWeighting);
    }

    /**
     * Map integer value to nodeWeighting parameter here, because Service Cutter only supports numbers as algorithm parameters ...
     * <p>
     * 0 = top (default)
     * 1 = label
     * 2 = linear
     * 3 = log
     * <p>
     * https://github.com/nlpub/watset-java#chinese-whispers
     */
    private NodeWeighting mapNodeWeightingConfig(int nodeWeighting) {
        switch (nodeWeighting) {
            case 1:
                return NodeWeighting.label();
            case 2:
                return NodeWeighting.linear();
            case 3:
                return NodeWeighting.log();
        }
        return NodeWeighting.top();
    }

}
