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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Test case for {@link Move}
 *
 * @author Emily Björk
 */
@SuppressWarnings("javadoc")
public class MoveTest {

	@Test
	public void testEquals() throws SyntaxErrorException {
		assertEquals(Move.valueOf("r10"), Move.valueOf("R10"));
		assertEquals(Move.valueOf("resign"), Move.valueOf("resign"));
		assertEquals(Move.valueOf("pass"), Move.valueOf("pass"));
		assertNotEquals(Move.valueOf("r10"), Move.valueOf("r9"));
		assertNotEquals(Move.valueOf("a10"), Move.valueOf("b10"));
		assertNotEquals(Move.valueOf("a10"), null);
		assertNotEquals(Move.valueOf("a10"), "a10");
	}

	@Test
	public void testHashCode() {
		// Hash codes should be collision free for this simple class.
		final Set<Integer> hashCodes = new HashSet<>();
		for (int y = 0; y < Move.MAX_BOARD_SIZE; ++y) {
			for (int x = 0; x < Move.MAX_BOARD_SIZE; ++x) {
				final Integer hashCode = Integer.valueOf(new Move(x, y).hashCode());
				assertFalse(hashCodes.contains(hashCode));
				hashCodes.add(hashCode);
			}
		}

		final Integer hashCodePass = Integer.valueOf(Move.PASS.hashCode());
		assertFalse(hashCodes.contains(hashCodePass));
		hashCodes.add(hashCodePass);

		final Integer hashCodeResign = Integer.valueOf(Move.RESIGN.hashCode());
		assertFalse(hashCodes.contains(hashCodeResign));
		hashCodes.add(hashCodeResign);
	}

	@Test
	public void testToString() throws Exception {
		assertEquals("r11", new Move(16, 10).toString());
	}

	@Test
	public void testToStringPass() throws Exception {
		assertEquals("pass", Move.PASS.toString());
	}

	@Test
	public void testToStringResign() throws Exception {
		assertEquals("resign", Move.RESIGN.toString());
	}

	@Test
	public void testValueOfLowerCase() throws Exception {
		final Move value = Move.valueOf("r11");
		assertEquals(16, value.x);
		assertEquals(10, value.y);
	}

	@Test
	public void testValueOfLowerLeft() throws Exception {
		final Move value = Move.valueOf("a1");
		assertEquals(0, value.x);
		assertEquals(0, value.y);
	}

	@Test
	public void testValueOfPass() throws Exception {
		assertSame(Move.PASS, Move.valueOf("pass"));
		assertSame(Move.PASS, Move.valueOf("pASs"));
	}

	@Test
	public void testValueOfResign() throws Exception {
		assertSame(Move.RESIGN, Move.valueOf("resign"));
		assertSame(Move.RESIGN, Move.valueOf("rEsIgn"));
	}

	@Test(expected = SyntaxErrorException.class)
	public void testValueOfTooLargeY() throws Exception {
		Move.valueOf("A" + Integer.toString(Move.MAX_BOARD_SIZE + 1));
	}

	@Test(expected = SyntaxErrorException.class)
	public void testValueOfTooSmallY() throws Exception {
		Move.valueOf("A0");
	}

	@Test(expected = SyntaxErrorException.class)
	public void testValueOfUnknownLetter() throws Exception {
		Move.valueOf("å10");
	}

	@Test
	public void testValueOfUpperCase() throws Exception {
		final Move value = Move.valueOf("R11");
		assertEquals(16, value.x);
		assertEquals(10, value.y);
	}

	@Test
	public void testValueOfUpperRight() throws Exception {
		final Move value = Move.valueOf("z25");
		assertEquals(24, value.x);
		assertEquals(24, value.y);
	}

	@Test(expected = SyntaxErrorException.class)
	public void testValueRubbish() throws Exception {
		Move.valueOf("somethingelse");
	}

	@Test(expected = SyntaxErrorException.class)
	public void testValueRubbishEmptyString() throws Exception {
		Move.valueOf("");
	}

	@Test(expected = SyntaxErrorException.class)
	public void testValueRubbishExtraStuff() throws Exception {
		Move.valueOf("s4x");
	}

	@Test(expected = SyntaxErrorException.class)
	public void testValueRubbishNull() throws Exception {
		Move.valueOf(null);
	}
}