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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyChar;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
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
import java.io.Writer;
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

	@Test
	public void testBoardSize() throws Exception {
		when(engine.resizeBoard(19)).thenReturn(Boolean.TRUE);
		assertEquals("=\n\n", runCommand("boardsize 19\n"));
		verify(engine).resizeBoard(19);

		when(engine.resizeBoard(17)).thenReturn(Boolean.FALSE);
		assertEquals("? unacceptable size\n\n", runCommand("boardsize 17\n"));
		verify(engine).resizeBoard(17);
	}

	@Test
	public void testBoardSizeOutsideSpec() throws Exception {
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

	@Test
	public void testBoardSizeTooFewArgs() throws Exception {
		assertEquals("? syntax error in command: boardsize\nError was: Invalid number of arguments!\n\n",
				runCommand("boardsize\n"));
		verifyZeroInteractions(engine);
	}

	@Test
	public void testBoardSizeWrongTypeArgs() throws Exception {
		assertEquals("? syntax error in command: boardsize ninteen\nError was: Not an integer: ninteen!\n\n",
				runCommand("boardsize ninteen\n"));
		assertEquals("?32 syntax error in command: 32 boardsize ninteen\nError was: Not an integer: ninteen!\n\n",
				runCommand("32 boardsize ninteen\n"));
		verifyZeroInteractions(engine);
	}

	@Test
	public void testClearBoard() throws Exception {
		assertEquals("=\n\n", runCommand("clear_board\n"));
		verify(engine).newGame();
	}

	@Test
	public void testEmptyLine() throws Exception {
		assertEquals("", runCommand("\n\r\n"));
		assertEquals("", runCommand(" "));
		assertEquals("", runCommand("\t"));

	}

	@Test(expected = RuntimeException.class)
	public void testEngineThrows() throws Exception {

		doThrow(RuntimeException.class).when(engine).setKomi(3.2f);

		// No response when engine throws.
		assertEquals("", runCommand("komi 3.2\n"));
	}

	@Test
	public void testGenMove() throws Exception {
		when(engine.nextMove(Player.BLACK)).thenReturn(Move.RESIGN);
		assertEquals("= resign\n\n", runCommand("genmove black\n"));
		verify(engine).nextMove(Player.BLACK);
	}

	@Test
	public void testGenMoveBadArg() throws Exception {
		assertEquals("? syntax error in command: genmove who\nError was: Unknown player: who!\n\n",
				runCommand("genmove who\n"));
		verifyZeroInteractions(engine);
	}

	@Test
	public void testGenMoveMissingArg() throws Exception {
		assertEquals("? syntax error in command: genmove\nError was: Invalid number of arguments!\n\n",
				runCommand("genmove\n"));
		verifyZeroInteractions(engine);
	}

	@Test
	public void testKnownCommand() throws Exception {
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

	@Test
	public void testKomi() throws Exception {
		assertEquals("=\n\n", runCommand("komi 4.3\n"));
		verify(engine).setKomi(4.3f);
	}

	@Test
	public void testKomiInteger() throws Exception {
		assertEquals("=\n\n", runCommand("komi 4\n"));
		verify(engine).setKomi(4.0f);
	}

	@Test
	public void testKomiNoArg() throws Exception {
		assertEquals("? syntax error in command: komi\nError was: Invalid number of arguments!\n\n",
				runCommand("komi\n"));

		verifyZeroInteractions(engine);
	}

	@Test
	public void testKomiNonNumberArg() throws Exception {
		assertEquals("? syntax error in command: komi foo\nError was: Not a float: foo!\n\n", runCommand("komi foo\n"));

		verifyZeroInteractions(engine);
	}

	@Test
	public void testListCommand() throws Exception {
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

	@Test
	public void testName() throws Exception {
		when(engine.getName()).thenReturn("abc");
		assertEquals("= abc\n\n", runCommand("name\n"));
		assertEquals("= abc\n\n", runCommand("name # This is a comment\n"));
		assertEquals("=32 abc\n\n", runCommand("32 name\n"));
	}

	@Test
	public void testPipeThrows() throws Exception {
		@SuppressWarnings("resource")
		final Writer w = mock(Writer.class);
		when(w.append(any())).thenThrow(IOException.class);
		when(w.append(anyChar())).thenThrow(IOException.class);

		try (StringReader stringReader = new StringReader("komi 3.2\n");
				final BufferedReader br = new BufferedReader(stringReader);) {
			final GoTextProtocol cut = new GoTextProtocol(br, w, engine);
			cut.call();
		}
		// Don't write anything to the pipe after the exception.
		verify(w).append('=');
		verifyNoMoreInteractions(w);
	}

	@Test
	public void testPlay() throws Exception, SyntaxErrorException {
		when(engine.addMove(Move.valueOf("r10"), Player.BLACK)).thenReturn(true);
		assertEquals("=\n\n", runCommand("play black r10\n"));
		verify(engine).addMove(Move.valueOf("r10"), Player.BLACK);
	}

	@Test
	public void testPlayBadMove() throws Exception {
		assertEquals(
				"? syntax error in command: play b somewhere\nError was: Invalid move: somewhere, expected integer after first character!\n\n",
				runCommand("play b somewhere\n"));
		verifyZeroInteractions(engine);
	}

	@Test
	public void testPlayBadPlayer() throws Exception {
		assertEquals("? syntax error in command: play bl r10\nError was: Unknown player: bl!\n\n",
				runCommand("play bl r10\n"));
		verifyZeroInteractions(engine);
	}

	@Test
	public void testPlayInvalidMove() throws Exception, SyntaxErrorException {
		when(engine.addMove(Move.valueOf("r10"), Player.BLACK)).thenReturn(false);
		assertEquals("? illegal move\n\n", runCommand("play black r10\n"));
		verify(engine).addMove(Move.valueOf("r10"), Player.BLACK);
	}

	@Test
	public void testPlayMissingArgs() throws Exception {
		assertEquals("? syntax error in command: play black\nError was: Invalid number of arguments!\n\n",
				runCommand("play black\n"));
		verifyZeroInteractions(engine);
	}

	@Test
	public void testProtocolVersion() throws Exception {
		assertEquals("= 2\n\n", runCommand("protocol_version\n"));
		assertEquals("= 2\n\n", runCommand("protocol_version # This is a comment\n"));
		assertEquals("=3 2\n\n", runCommand("3 protocol_version\n"));
	}

	@Test
	public void testQuit() throws Exception {
		assertEquals("=\n\n", runCommand("quit\n"));
		assertEquals("=\n\n", runCommand("quit # This is a comment\n"));
		assertEquals("=32\n\n", runCommand("32 quit\n"));
	}

	@Test
	public void testUnknownCommand() throws Exception {
		when(engine.getName()).thenReturn("abc");
		assertEquals("?123 unknown command\n\n", runCommand("123 bad_command argument 1 2 3\n"));
		assertEquals("? unknown command\n\n", runCommand("bad_command\n"));
	}

	@Test
	public void testVersion() throws Exception {
		when(engine.getVersion()).thenReturn("123");
		assertEquals("= 123\n\n", runCommand("version\n"));
		assertEquals("=1 123\n\n", runCommand("1 version\n"));
	}

	private String runCommand(String aCommand) throws Exception {
		try (StringReader stringReader = new StringReader(aCommand);
				final BufferedReader br = new BufferedReader(stringReader);
				StringWriter stringWriter = new StringWriter();
				final BufferedWriter bw = new BufferedWriter(stringWriter);) {
			final GoTextProtocol cut = new GoTextProtocol(br, bw, engine);
			cut.call();
			return stringWriter.toString();
		}
	}
}
