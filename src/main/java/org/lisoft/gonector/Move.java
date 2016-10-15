/* @formatter:off
 * Gonector - A Java implementation of the Go Text Protocol version 2.
 * Copyright (C) 2016 Emily Björk
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
// @formatter:on
package org.lisoft.gonector;

/**
 * Represents one move as defined by the GTP.
 *
 * @author Emily Björk
 */
public class Move {
	/**
	 * The maximum supported board size by the Go Text Protocol. The
	 * {@link GoEngine} only has to support a subset of the sizes.
	 */
	public static final int MAX_BOARD_SIZE = 25;
	/**
	 * The minimum supported board size by the Go Text Protocol. The
	 * {@link GoEngine} only has to support a subset of the sizes.
	 */
	public static final int MIN_BOARD_SIZE = 2;

	/**
	 * A special move that should be used when the {@link GoEngine} wants to
	 * pass.
	 */
	public static final Move PASS = new Move(-2, 0);

	/**
	 * A special move that should be used when the {@link GoEngine} wants to
	 * resign.
	 */
	public static final Move RESIGN = new Move(-1, 0);

	private static final String LETTERS = "abcdefghjklmnopqrstuvwxyz";
	private static final String PASS_STRING = "pass";
	private static final String RESIGN_STRING = "resign";

	/**
	 * Converts a GTP move string ("R14" for example) into a move object. The
	 * GTP string is case insensitive.
	 *
	 * @param aMove
	 *            The string to convert.
	 * @return A {@link Move} object that represents the move.
	 * @throws SyntaxErrorException
	 *             Thrown if the move is invalid.
	 */
	public static Move valueOf(String aMove) throws SyntaxErrorException {
		if (null == aMove || aMove.isEmpty()) {
			throw new SyntaxErrorException("No move given!");
		}
		final String str = aMove.toLowerCase();

		if (PASS_STRING.equals(str)) {
			return PASS;
		}
		if (RESIGN_STRING.equals(str)) {
			return RESIGN;
		}

		final int x = LETTERS.indexOf(str.charAt(0));
		final int y;
		try {
			y = Integer.parseInt(str.substring(1)) - 1;
		} catch (final Exception e) {
			throw new SyntaxErrorException("Invalid move: " + str + ", expected integer after first character!", e);
		}
		if (x < 0 || y < 0 || y >= MAX_BOARD_SIZE) {
			throw new SyntaxErrorException("Invalid move: " + str + ", coordinate out of range!");
		}
		return new Move(x, y);
	}

	/**
	 * The x (horizontal) position of the move. Will be -1 for special move
	 * {@link #RESIGN} and -2 for special move {@link #PASS}. The value 0 is to
	 * the left and positive numbers to the right. I.e. Cartesian coordinates.
	 */
	public final int x;

	/**
	 * The y (vertical) position of the move. The value 0 is to the bottom and
	 * positive numbers move up on the board. I.e. Cartesian coordinates.
	 */
	public final int y;

	/**
	 * Creates a new immutable Move.
	 *
	 * @param aX
	 *            The value of the x position.
	 * @param aY
	 *            The value of the y position.
	 */
	public Move(int aX, int aY) {
		x = aX;
		y = aY;
	}

	@Override
	public boolean equals(Object aThat) {
		if (this == aThat) {
			return true;
		}
		if (aThat instanceof Move) {
			final Move move = (Move) aThat;
			return x == move.x && y == move.y;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return y * MAX_BOARD_SIZE + x;
	}

	@Override
	public String toString() {
		if (x == -1) {
			return RESIGN_STRING;
		} else if (x < 0) {
			return PASS_STRING;
		}
		return LETTERS.charAt(x) + Integer.toString(y + 1);
	}
}
