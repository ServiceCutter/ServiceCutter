package ch.hsr.servicecutter.scorer;

import java.security.InvalidParameterException;

public class Score {

	private double score;
	private double priority;

	public Score(final double score, final double priority) {
		if (score < -10d || score > 10d) {
			throw new InvalidParameterException("score should be between -10 and 10");
		}
		this.score = score;
		this.priority = priority;
	}

	public double getScore() {
		return score;
	}

	public double getPriority() {
		return priority;
	}

	public double getPrioritizedScore() {
		return score * priority;
	}

	public Score withPriority(final double newPriority) {
		return new Score(this.score, newPriority);
	}

}
