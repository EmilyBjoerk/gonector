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
 * An enumeration for the players in the game, white and black.
 *
 * @author Emily Björk
 */
public enum Player {
	/**
	 * The black player.
	 */
	BLACK,
	/**
	 * The white player.
	 */
	WHITE;

	/**
	 * Converts from a GTP string to an enumeration. The GTP strings are case
	 * insensitive.
	 *
	 * @param aPlayer
	 *            The string to convert.
	 * @return An enumeration representing the player.
	 * @throws SyntaxErrorException
	 *             If the string is malformed.
	 */
	public static Player fromString(String aPlayer) throws SyntaxErrorException {
		if (aPlayer == null || aPlayer.isEmpty()) {
			throw new SyntaxErrorException("Not a valid player string!");
		}

		final String str = aPlayer.toLowerCase();
		if ("b".equals(str) || "black".equals(str)) {
			return BLACK;
		} else if ("w".equals(str) || "white".equals(str)) {
			return WHITE;
		}
		throw new SyntaxErrorException("Unknown player: " + str + "!");
	}
}
