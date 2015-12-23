package ch.hsr.servicecutter.solver.greedy;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.servicecutter.model.solver.EntityPair;
import ch.hsr.servicecutter.model.solver.Service;
import ch.hsr.servicecutter.model.solver.SolverResult;
import ch.hsr.servicecutter.model.usersystem.Nanoentity;
import ch.hsr.servicecutter.scorer.Score;
import ch.hsr.servicecutter.solver.Solver;

public class GreedySolver implements Solver {

	private Map<EntityPair, Map<String, Score>> scores;
	private Integer numberOfClusters;
	private List<Nanoentity> nanoentities;
	private Map<Nanoentity, ServiceBox> optimizationTracker;

	private Logger log = LoggerFactory.getLogger(GreedySolver.class);

	public GreedySolver(final List<Nanoentity> nanoentities, final Map<EntityPair, Map<String, Score>> scores, final Integer numberOfClusters) {
		this.nanoentities = nanoentities;
		this.scores = scores;
		this.numberOfClusters = numberOfClusters;
	}

	@Override
	public SolverResult solve() {
		optimizationTracker = new HashMap<>();

		List<ServiceBox> boxes = createBoxes();
		Deque<Nanoentity> queue = new LinkedList<Nanoentity>(nanoentities);

		List<ServiceBox> resultBoxes = construct(boxes, queue);
		char serviceIdGenerator = 'A';
		Set<Service> resultedService = new HashSet<>();
		for (ServiceBox box : resultBoxes) {
			resultedService.add(new Service(box.getCurrentNanoentities().stream().map(n -> n.getContextName()).collect(Collectors.toList()), serviceIdGenerator++));
		}

		return new SolverResult(resultedService);
	}

	private List<ServiceBox> construct(final List<ServiceBox> boxes, final Deque<Nanoentity> queue) {
		int counter = 0;
		while (!queue.isEmpty()) {
			constructionStep(queue, boxes);
			counter++;
			if (counter > boxes.size() && counter % boxes.size() == 0) {
				optimize(boxes, queue);
			}
		}

		return boxes;
	}

	private void optimize(final List<ServiceBox> boxes, final Deque<Nanoentity> queue) {
		boolean optimized = false;
		do {
			optimized = false;
			for (ServiceBox box : boxes) {
				Nanoentity nanoEntityToOptimize = optimizationStepForBox(queue, box);
				ServiceBox matchedBox = constructionStep(queue, boxes);
				if (!optimizationTracker.get(nanoEntityToOptimize).equals(matchedBox)) {
					optimized = true;
				}
			}
		} while (optimized == true);
	}

	private Nanoentity optimizationStepForBox(final Deque<Nanoentity> queue, final ServiceBox box) {
		Nanoentity nanoentity = box.getCurrentNanoentities().stream().min((n1, n2) -> Double.compare(getRelativeScore(n1, box), getRelativeScore(n2, box))).get();
		box.remove(nanoentity);
		queue.addFirst(nanoentity);
		optimizationTracker.put(nanoentity, box);
		return nanoentity;
	}

	private ServiceBox constructionStep(final Queue<Nanoentity> queue, final List<ServiceBox> boxes) {
		Nanoentity current = queue.poll();
		ServiceBox bestMatch = findBestMatchingBox(current, boxes);
		bestMatch.add(current);
		// if (optimizationTracker.containsKey(nanoentities)) {
		// if (!optimizationTracker.get(nanoentities).equals(bestMatch)) {
		// // TODO: handle infinite loops
		// optimizationTracker.remove(nanoentities);
		// }
		// }
		return bestMatch;
	}

	private ServiceBox findBestMatchingBox(final Nanoentity nanoentity, final List<ServiceBox> boxes) {
		for (ServiceBox box : boxes) {
			if (box.getCurrentNanoentities().isEmpty()) {
				return box;
			}
		}
		return boxes.stream().max((b1, b2) -> Double.compare(getRelativeScore(nanoentity, b1), getRelativeScore(nanoentity, b2))).get();
	}

	private double getRelativeScore(final Nanoentity nanoentity, final ServiceBox box) {
		Double score = 0d;
		for (Nanoentity entityInBox : box.getCurrentNanoentities()) {
			EntityPair pair = new EntityPair(entityInBox, nanoentity);
			if (scores.get(pair) != null && !scores.get(pair).values().isEmpty()) {
				score += scores.get(pair).values().stream().mapToDouble(f -> f.getPrioritizedScore()).sum();
			}
		}
		log.info("getRelativeScore from {} to {}: {}", nanoentity.getContextName(), box.getCurrentNanoentities(), score);
		return score;
	}

	private List<ServiceBox> createBoxes() {
		List<ServiceBox> boxes = new ArrayList<>();
		for (int i = 0; i < numberOfClusters; i++) {
			boxes.add(new ServiceBox());
		}
		return boxes;
	}

}
