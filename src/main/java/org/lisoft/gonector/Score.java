package org.lisoft.gonector;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * This class represents the score of a game.
 *
 * @author Emily Björk
 */
public class Score {
	/**
	 * Constant to signify a draw game.
	 */
	public static final Score DRAW = new Score(null, 0);

	private static final NumberFormat NF;
	static {
		// Always use the ENGLISH locale to get a decimal point instead of a
		// decimal comma.
		NF = NumberFormat.getNumberInstance(Locale.ENGLISH);
		NF.setMinimumFractionDigits(0);
		NF.setMaximumFractionDigits(1);
	}

	private final double score;
	private final Player winner;

	/**
	 * Creates a new score object where the given player is the winner by the
	 * given amount.
	 *
	 * @param aWinner
	 *            The player that won, for draw use the {@link Score#DRAW}
	 *            constant instead.
	 * @param aScore
	 *            The score difference in favour of the winning player. Must
	 *            always be positive.
	 */
	public Score(Player aWinner, double aScore) {
		if (aScore < 0) {
			throw new IllegalArgumentException("Score must be positive!");
		}
		winner = aWinner;
		score = aScore;
	}

	@Override
	public String toString() {
		if (winner == null) {
			return "0";
		}

		// Capacity of 7 is enough for a score difference of 999.5 points
		final StringBuilder sb = new StringBuilder(7);
		sb.append(winner.toShortString()).append('+').append(NF.format(score));
		return sb.toString();
	}
}
