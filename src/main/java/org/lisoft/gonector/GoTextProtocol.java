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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class implements the GTP protocol and links it to a {@link GoEngine}.
 *
 * @author Emily Björk
 */
public class GoTextProtocol implements Runnable {
	/**
	 * This functional interface is used to implement a command in the Go Text
	 * Protocol.
	 *
	 * @author Emily Björk
	 */
	@FunctionalInterface
	private interface Command {
		/**
		 * @param aId
		 *            An optional ID as specified in the GTP. Negative if not
		 *            present.
		 * @param aArguments
		 *            All the arguments to the command.
		 * @return <code>true</code> if the client should continue,
		 *         <code>false</code> if the GTP engine should disconnect.
		 * @throws SyntaxErrorException
		 *             Thrown if the input was malformed and no response was
		 *             sent yet. If the command has already sent a response,
		 *             then it must not throw this exception.
		 * @throws Exception
		 *             A command may throw any exception. Throwing will result
		 *             in the exception being logged and the Go Text Protocol
		 *             shutting down.
		 */
		boolean process(int aId, String[] aArguments) throws SyntaxErrorException, Exception;
	}

	/**
	 * A regular expression that is used for matching and splitting commands
	 * into parts.
	 */
	private static final Pattern COMMAND_PATTERN = Pattern.compile("(\\d+)? ?(\\S+)(?: (.*))?");

	/**
	 * The version of the GTP protocol that is implemented.
	 */
	private static final String GTP_VERSION = "2";

	/**
	 * Special error message defined in the GTP specification.
	 */
	private static final String ILLEGAL_MOVE = "illegal move";

	/**
	 * Special error message defined in the GTP specification.
	 */
	private static final String UNACCEPTABLE_SIZE = "unacceptable size";

	/**
	 * Special error message defined in the GTP specification.
	 */
	private static final String UNKNOWN_COMMAND = "unknown command";

	private static void assertArguments(int aNumArguments, String[] aArguments) throws SyntaxErrorException {
		if (aArguments.length < aNumArguments) {
			throw new SyntaxErrorException("Invalid number of arguments!");
		}
	}

	private static String clean(String aCommand) {
		String ans = aCommand.replace('\t', ' ');
		final int commentStart = ans.indexOf('#');
		if (commentStart >= 0) {
			ans = ans.substring(0, commentStart);
		}
		return ans.replaceAll("[\\p{Cntrl}&&[^\\n]]", "").trim();
	}

	private final Map<String, Command> commands;
	private final GoEngine engine;
	private final Logger logger = LogManager.getLogger(GoTextProtocol.class);

	private final BufferedReader reader;

	private final Writer writer;

	/**
	 * Creates a new {@link GoTextProtocol} instance.
	 *
	 * @param aReader
	 *            The reader to read input from the controller from. It is the
	 *            responsibility of the caller to close this reader when
	 *            {@link #run()} exits.
	 * @param aWriter
	 *            A writer to send input to the controller from. It is the
	 *            responsibility of the caller to close this writer when
	 *            {@link #run()} exits.
	 * @param aEngine
	 *            A {@link GoEngine} that is used for serving the requests from
	 *            the controller.
	 */
	public GoTextProtocol(BufferedReader aReader, Writer aWriter, GoEngine aEngine) {
		reader = aReader;
		writer = aWriter;
		engine = aEngine;
		commands = new HashMap<>();
		commands.put("protocol_version", (id, arg) -> {
			respond(true, id, GTP_VERSION);
			return true;
		});
		commands.put("name", (id, arg) -> {
			respond(true, id, engine.getName());
			return true;
		});
		commands.put("version", (id, arg) -> {
			respond(true, id, engine.getVersion());
			return true;
		});
		commands.put("known_command", (id, arg) -> {
			assertArguments(1, arg);
			respond(true, id, Boolean.toString(commands.containsKey(arg[0])));
			return true;
		});
		commands.put("list_commands", (id, arg) -> {
			final String ans = commands.keySet().stream().collect(Collectors.joining("\n"));
			respond(true, id, ans);
			return true;
		});
		commands.put("quit", (id, arg) -> {
			respond(true, id, "");
			return false;
		});
		commands.put("boardsize", (id, arg) -> {
			assertArguments(1, arg);
			final int size;
			try {
				size = Integer.parseInt(arg[0]);
			} catch (final NumberFormatException e) {
				throw new SyntaxErrorException("Not an integer: " + arg[0] + "!");
			}
			boolean success = false;
			if (size >= Move.MIN_BOARD_SIZE && size <= Move.MAX_BOARD_SIZE) {
				success = engine.resizeBoard(size);
			}
			respond(success, id, success ? "" : UNACCEPTABLE_SIZE);
			return true;
		});
		commands.put("clear_board", (id, arg) -> {
			engine.newGame();
			respond(true, id, "");
			return true;
		});
		commands.put("komi", (id, arg) -> {
			assertArguments(1, arg);
			final float komi;
			try {
				komi = Float.parseFloat(arg[0]);
			} catch (final NumberFormatException e) {
				throw new SyntaxErrorException("Not a float: " + arg[0] + "!");
			}
			engine.setKomi(komi);
			respond(true, id, "");
			return true;
		});
		commands.put("play", (id, arg) -> {
			assertArguments(2, arg);
			final Player player = Player.fromString(arg[0]);
			final boolean success = engine.addMove(Move.valueOf(arg[1]), player);
			respond(success, id, success ? "" : ILLEGAL_MOVE);
			return true;
		});
		commands.put("genmove", (id, arg) -> {
			assertArguments(1, arg);
			final Player player = Player.fromString(arg[0]);
			final Move move = engine.nextMove(player);
			respond(true, id, move.toString());
			return true;
		});
	}

	@Override
	public void run() {
		try {
			while (true) {
				int id = -1;
				String line = null;

				try {
					line = reader.readLine();
					logger.debug("Remote sent: " + line);

					if (null == line) {
						break;// Remote disconnected
					}
					line = clean(line);
					if (line.isEmpty()) {
						continue;
					}

					Command cmd = null;
					String[] args = new String[0];
					final Matcher m = COMMAND_PATTERN.matcher(line);
					if (m.matches()) {
						if (m.group(1) != null) {
							id = Integer.parseInt(m.group(1));
						}
						final String cmdName = m.group(2);
						if (m.group(3) != null) {
							args = m.group(3).split(" ");
						}
						cmd = commands.get(cmdName);
					}

					if (null != cmd) {
						final boolean keepGoing = cmd.process(id, args);
						if (!keepGoing) {
							break;
						}
					} else {
						respond(false, id, UNKNOWN_COMMAND);
					}
				} catch (final SyntaxErrorException e) {
					respond(false, id, "syntax error in command: " + line + "\nError was: " + e.getMessage());
				}
			}
		} catch (final IOException e) {
			logger.error("An IO error occurred: {}", e.getMessage());
			logger.error("Closing connection.");
		} catch (final Exception e) {
			logger.fatal("Terminating due to unknown!", e);
		}
	}

	/**
	 * Writes a response to the controller.
	 *
	 * @param aSuccess
	 *            Whether or not the command being responded to was successful.
	 * @param aId
	 *            The optional ID of the command being responded to, negative if
	 *            not present. Note, simply pass through the ID from
	 *            {@link Command#process(int, String[])}.
	 * @param aMessage
	 *            The message to send, must not be null.
	 * @throws IOException
	 */
	private void respond(boolean aSuccess, int aId, String aMessage) throws IOException {
		if (!aSuccess && aMessage.trim().isEmpty()) {
			throw new RuntimeException("A failure must always have a message!");
		}
		writer.append(aSuccess ? '=' : '?');
		if (aId >= 0) {
			writer.append(Integer.toString(aId));
		}
		if (!aMessage.isEmpty()) {
			writer.append(' ').append(aMessage);
		}
		writer.append("\n\n");
		writer.flush();
	}
}
