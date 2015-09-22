package ch.hsr.servicestoolkit.solver;

import java.util.List;

public interface Solver {

	List<BoundedContext> solve(int numberOfClusters);

}
