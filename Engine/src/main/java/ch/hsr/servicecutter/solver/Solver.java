package ch.hsr.servicecutter.solver;

import ch.hsr.servicecutter.model.solver.SolverResult;

public interface Solver {

	/**
	 * Find the candidate service cuts using an algorithm on the already created
	 * graph.
	 */
	SolverResult solve();

}
