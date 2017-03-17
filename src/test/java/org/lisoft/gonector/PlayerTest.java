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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test suite for {@link Player} {@link Enum}.
 *
 * @author Emily Björk
 */
@SuppressWarnings("javadoc")
public class PlayerTest {

	@Test(expected = SyntaxErrorException.class)
	public void testFromStringBadString1() throws SyntaxErrorException {
		Player.fromString("blac");
	}

	@Test(expected = SyntaxErrorException.class)
	public void testFromStringBadString2() throws SyntaxErrorException {
		Player.fromString("whit");
	}

	@Test
	public void testFromStringCorrect() throws SyntaxErrorException {
		assertEquals(Player.BLACK, Player.fromString("b"));
		assertEquals(Player.BLACK, Player.fromString("B"));
		assertEquals(Player.BLACK, Player.fromString("black"));
		assertEquals(Player.BLACK, Player.fromString("BlACk"));
		assertEquals(Player.WHITE, Player.fromString("w"));
		assertEquals(Player.WHITE, Player.fromString("W"));
		assertEquals(Player.WHITE, Player.fromString("white"));
		assertEquals(Player.WHITE, Player.fromString("wHiTE"));
	}

	@Test(expected = SyntaxErrorException.class)
	public void testFromStringEmptyString() throws SyntaxErrorException {
		Player.fromString("");
	}

	@Test(expected = SyntaxErrorException.class)
	public void testFromStringNull() throws SyntaxErrorException {
		Player.fromString(null);
	}

	@Test
	public void testToShortString() {
		assertEquals("W", Player.WHITE.toShortString());
		assertEquals("B", Player.BLACK.toShortString());
	}

	@Test
	public void testValueOf() {
		// For test coverage only really.
		assertEquals(Player.BLACK, Player.valueOf("BLACK"));
		assertEquals(Player.WHITE, Player.valueOf("WHITE"));
	}

	@Test
	public void testValues() {
		// For test coverage only really.
		final Player[] players = Player.values();
		assertEquals(2, players.length);
	}
}
