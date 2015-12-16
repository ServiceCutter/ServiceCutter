package ch.hsr.servicecutter.solver;

public interface Solver {

	/**
	 * Find the candidate service cuts using an algorithm on the already created
	 * graph.
	 */
	SolverResult solve();

}
