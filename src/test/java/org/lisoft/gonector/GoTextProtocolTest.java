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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

/**
 * A test suite for the {@link GoTextProtocol} class.
 *
 * @author Emily Björk
 */
@SuppressWarnings({ "boxing", "javadoc" })
public class GoTextProtocolTest {
	final GoEngine engine = mock(GoEngine.class);

	@Test(timeout = 3000)
	public void testBoardSize() throws IOException {
		when(engine.resizeBoard(19)).thenReturn(Boolean.TRUE);
		assertEquals("=\n\n", runCommand("boardsize 19\n"));
		verify(engine).resizeBoard(19);

		when(engine.resizeBoard(17)).thenReturn(Boolean.FALSE);
		assertEquals("? unacceptable size\n\n", runCommand("boardsize 17\n"));
		verify(engine).resizeBoard(17);
	}

	@Test(timeout = 3000)
	public void testBoardSizeOutsideSpec() throws IOException {
		when(engine.resizeBoard(anyInt())).thenReturn(Boolean.TRUE);
		assertEquals("? unacceptable size\n\n",
				runCommand("boardsize " + Integer.toString(Move.MIN_BOARD_SIZE - 1) + "\n"));
		assertEquals("=\n\n", runCommand("boardsize " + Integer.toString(Move.MIN_BOARD_SIZE) + "\n"));
		assertEquals("=\n\n", runCommand("boardsize " + Integer.toString(Move.MAX_BOARD_SIZE) + "\n"));
		assertEquals("? unacceptable size\n\n",
				runCommand("boardsize " + Integer.toString(Move.MAX_BOARD_SIZE + 1) + "\n"));
		verify(engine).resizeBoard(Move.MIN_BOARD_SIZE);
		verify(engine).resizeBoard(Move.MAX_BOARD_SIZE);
		verifyNoMoreInteractions(engine);
	}

	@Test(timeout = 3000)
	public void testBoardSizeTooFewArgs() throws IOException {
		assertEquals("? syntax error in command: boardsize\nError was: Invalid number of arguments!\n\n",
				runCommand("boardsize\n"));
		verifyZeroInteractions(engine);
	}

	@Test(timeout = 3000)
	public void testBoardSizeWrongTypeArgs() throws IOException {
		assertEquals("? syntax error in command: boardsize ninteen\nError was: Not an integer: ninteen!\n\n",
				runCommand("boardsize ninteen\n"));
		assertEquals("?32 syntax error in command: 32 boardsize ninteen\nError was: Not an integer: ninteen!\n\n",
				runCommand("32 boardsize ninteen\n"));
		verifyZeroInteractions(engine);
	}

	@Test(timeout = 3000)
	public void testClearBoard() throws IOException {
		assertEquals("=\n\n", runCommand("clear_board\n"));
		verify(engine).newGame();
	}

	@Test(timeout = 3000)
	public void testEmptyLine() throws IOException {
		assertEquals("", runCommand("\n\r\n"));
		assertEquals("", runCommand(" "));
		assertEquals("", runCommand("\t"));

	}

	@Test(timeout = 3000)
	public void testGenMove() throws IOException {
		when(engine.nextMove(Player.BLACK)).thenReturn(Move.RESIGN);
		assertEquals("= resign\n\n", runCommand("genmove black\n"));
		verify(engine).nextMove(Player.BLACK);
	}

	@Test(timeout = 3000)
	public void testGenMoveBadArg() throws IOException {
		assertEquals("? syntax error in command: genmove who\nError was: Unknown player: who!\n\n",
				runCommand("genmove who\n"));
		verifyZeroInteractions(engine);
	}

	@Test(timeout = 3000)
	public void testGenMoveMissingArg() throws IOException {
		assertEquals("? syntax error in command: genmove\nError was: Invalid number of arguments!\n\n",
				runCommand("genmove\n"));
		verifyZeroInteractions(engine);
	}

	@Test(timeout = 3000)
	public void testKnownCommand() throws IOException {
		// Required commands:
		assertEquals("= true\n\n", runCommand("known_command protocol_version\n"));
		assertEquals("= true\n\n", runCommand("known_command name\n"));
		assertEquals("= true\n\n", runCommand("known_command version\n"));
		assertEquals("= true\n\n", runCommand("known_command known_command\n"));
		assertEquals("= true\n\n", runCommand("known_command list_commands\n"));
		assertEquals("= true\n\n", runCommand("known_command quit\n"));
		assertEquals("= true\n\n", runCommand("known_command boardsize\n"));
		assertEquals("= true\n\n", runCommand("known_command clear_board\n"));
		assertEquals("=3 true\n\n", runCommand("3 known_command komi\n"));
		assertEquals("=2 true\n\n", runCommand("2 known_command play\n"));
		assertEquals("=1 true\n\n", runCommand("1 known_command genmove\n"));
		assertEquals("= false\n\n", runCommand("known_command notexists\n"));
	}

	@Test(timeout = 3000)
	public void testKomi() throws IOException {
		assertEquals("=\n\n", runCommand("komi 4.3\n"));
		verify(engine).setKomi(4.3f);
	}

	@Test(timeout = 3000)
	public void testKomiInteger() throws IOException {
		assertEquals("=\n\n", runCommand("komi 4\n"));
		verify(engine).setKomi(4.0f);
	}

	@Test(timeout = 3000)
	public void testKomiNoArg() throws IOException {
		assertEquals("? syntax error in command: komi\nError was: Invalid number of arguments!\n\n",
				runCommand("komi\n"));

		verifyZeroInteractions(engine);
	}

	@Test(timeout = 3000)
	public void testKomiNonNumberArg() throws IOException {
		assertEquals("? syntax error in command: komi foo\nError was: Not a float: foo!\n\n", runCommand("komi foo\n"));

		verifyZeroInteractions(engine);
	}

	@Test(timeout = 3000)
	public void testListCommand() throws IOException {
		final String cmds = runCommand("list_commands");
		assertTrue(cmds.startsWith("= "));
		final Set<String> commandSet = Arrays.stream(cmds.substring(2).split("\n")).filter(x -> !x.isEmpty())
				.collect(Collectors.toSet());

		assertTrue(commandSet.contains("protocol_version"));
		assertTrue(commandSet.contains("name"));
		assertTrue(commandSet.contains("version"));
		assertTrue(commandSet.contains("known_command"));
		assertTrue(commandSet.contains("list_commands"));
		assertTrue(commandSet.contains("quit"));
		assertTrue(commandSet.contains("boardsize"));
		assertTrue(commandSet.contains("clear_board"));
		assertTrue(commandSet.contains("komi"));
		assertTrue(commandSet.contains("play"));
		assertTrue(commandSet.contains("genmove"));

	}

	@Test(timeout = 3000)
	public void testName() throws IOException {
		when(engine.getName()).thenReturn("abc");
		assertEquals("= abc\n\n", runCommand("name\n"));
		assertEquals("= abc\n\n", runCommand("name # This is a comment\n"));
		assertEquals("=32 abc\n\n", runCommand("32 name\n"));
	}

	@Test(timeout = 3000)
	public void testPlay() throws IOException, SyntaxErrorException {
		when(engine.addMove(Move.valueOf("r10"), Player.BLACK)).thenReturn(true);
		assertEquals("=\n\n", runCommand("play black r10\n"));
		verify(engine).addMove(Move.valueOf("r10"), Player.BLACK);
	}

	@Test(timeout = 3000)
	public void testPlayBadMove() throws IOException {
		assertEquals(
				"? syntax error in command: play b somewhere\nError was: Invalid move: somewhere, expected integer after first character!\n\n",
				runCommand("play b somewhere\n"));
		verifyZeroInteractions(engine);
	}

	@Test(timeout = 3000)
	public void testPlayBadPlayer() throws IOException {
		assertEquals("? syntax error in command: play bl r10\nError was: Unknown player: bl!\n\n",
				runCommand("play bl r10\n"));
		verifyZeroInteractions(engine);
	}

	@Test(timeout = 3000)
	public void testPlayInvalidMove() throws IOException, SyntaxErrorException {
		when(engine.addMove(Move.valueOf("r10"), Player.BLACK)).thenReturn(false);
		assertEquals("? illegal move\n\n", runCommand("play black r10\n"));
		verify(engine).addMove(Move.valueOf("r10"), Player.BLACK);
	}

	@Test(timeout = 3000)
	public void testPlayMissingArgs() throws IOException {
		assertEquals("? syntax error in command: play black\nError was: Invalid number of arguments!\n\n",
				runCommand("play black\n"));
		verifyZeroInteractions(engine);
	}

	@Test(timeout = 3000)
	public void testProtocolVersion() throws IOException {
		assertEquals("= 2\n\n", runCommand("protocol_version\n"));
		assertEquals("= 2\n\n", runCommand("protocol_version # This is a comment\n"));
		assertEquals("=3 2\n\n", runCommand("3 protocol_version\n"));
	}

	@Test(timeout = 3000)
	public void testQuit() throws IOException {
		assertEquals("=\n\n", runCommand("quit\n"));
		assertEquals("=\n\n", runCommand("quit # This is a comment\n"));
		assertEquals("=32\n\n", runCommand("32 quit\n"));
	}

	@Test(timeout = 3000)
	public void testUnknownCommand() throws IOException {
		when(engine.getName()).thenReturn("abc");
		assertEquals("?123 unknown command\n\n", runCommand("123 bad_command argument 1 2 3\n"));
		assertEquals("? unknown command\n\n", runCommand("bad_command\n"));
	}

	@Test(timeout = 3000)
	public void testVersion() throws IOException {
		when(engine.getVersion()).thenReturn("123");
		assertEquals("= 123\n\n", runCommand("version\n"));
		assertEquals("=1 123\n\n", runCommand("1 version\n"));
	}

	private String runCommand(String aCommand) throws IOException {
		try (StringReader stringReader = new StringReader(aCommand);
				final BufferedReader br = new BufferedReader(stringReader);
				StringWriter stringWriter = new StringWriter();
				final BufferedWriter bw = new BufferedWriter(stringWriter);) {
			final GoTextProtocol cut = new GoTextProtocol(br, bw, engine);
			cut.run();
			return stringWriter.toString();
		}
	}
}
