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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class implements the GTP protocol and links it to a {@link GoEngine}.
 *
 * The class implements {@link Callable} so that you can submit it to an
 * {@link ExecutorService} to run asynchronously.
 *
 * @author Emily Björk
 */
public class GoTextProtocol implements Callable<Void> {
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
	private static final Pattern COMMAND_PATTERN = Pattern.compile("^(\\d*)\\s*(\\S+)\\s*([^#]*?)\\s*(#.*)?$");
	private static final String[] EMPTY_ARGS = new String[0];

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
	 *            {@link #call()} exits.
	 * @param aWriter
	 *            A writer to send input to the controller from. It is the
	 *            responsibility of the caller to close this writer when
	 *            {@link #call()} exits.
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
		if (engine.canScore()) {
			commands.put("final_score", (id, arg) -> {
				final Score score = engine.getScore();
				if (null != score) {
					respond(true, id, score.toString());
					return true;
				}
				logger.fatal("getScore() returned null!");
				return false;
			});
		}
	}

	/**
	 * Runs the protocol until the remote disconnects.
	 *
	 * @throws Exception
	 *             Whatever the {@link GoEngine} throws. If {@link GoEngine}
	 *             throws this method will log the error and then re-throw,
	 *             terminating the parsing. The connection to the controller
	 *             must then be reset because the engine and the controller may
	 *             have de-synced.
	 */
	@Override
	public Void call() throws Exception {
		try {
			while (true) {
				int id = -1;
				String line = null;

				try {
					line = reader.readLine();
					logger.debug("Remote sent: {}", line);

					if (null == line) {
						break;// Remote disconnected
					}
					// Remove any non newline control characters per protocol
					// specification. We use regexp that is insensitive to \t
					// and " " so we don't need to replace \t by " ", the regexp
					// also takes care of comments so we don't need to do that
					// either.
					line = line.replaceAll("[\\p{Cntrl}&&[^\\n]]", "");

					final Matcher m = COMMAND_PATTERN.matcher(line);
					// The regular expression is designed so that everything
					// except whitespace only lines match.
					if (m.matches()) {
						if (!m.group(1).isEmpty()) {
							id = Integer.parseInt(m.group(1));
						}
						final String cmdName = m.group(2);
						final String[] args = m.group(3).isEmpty() ? EMPTY_ARGS : m.group(3).split("\\s+");
						final Command cmd = commands.get(cmdName);

						if (null != cmd) {
							if (!cmd.process(id, args)) {
								break;
							}
						} else {
							respond(false, id, UNKNOWN_COMMAND);
						}
					}
				} catch (final SyntaxErrorException e) {
					respond(false, id, "syntax error in command: " + line + "\nError was: " + e.getMessage());
				}
			}
		} catch (final IOException e) {
			logger.error("An IO error occurred: {}", e.getMessage());
			logger.error("Closing connection.");
		} catch (final Exception e) {
			logger.fatal("Terminating due to unknown exception!", e);
			throw e;
		}
		return null;
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
		final StringBuilder sb = new StringBuilder();

		sb.append(aSuccess ? '=' : '?');
		if (aId >= 0) {
			sb.append(Integer.toString(aId));
		}
		if (!aMessage.isEmpty()) {
			sb.append(' ').append(aMessage);
		}
		logger.debug("Local sending...: [{}]", sb.toString());
		writer.append(sb.toString()).append("\n\n");
		writer.flush();
		logger.debug("Local sent OK");
	}
}
