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
 * This interface specifies an API for a Go robot that can be interfaced through
 * GTP.
 *
 * In general the GTP protocol stipulates that some of these functions may not
 * be called before others, for example {@link #resizeBoard(int)} should always
 * be called before adding any moves. However a malicious controller may choose
 * to send these commands in an unexpected order, the implementation should
 * expected that for example play move can be called before the board size is
 * known. If this happens just do nothing and return immediately.
 *
 * @author Emily Björk
 */
public interface GoEngine {
	/**
	 * Add a move to the current game state. This can be used to play the game
	 * to a certain state or to inform the robot of the opponents move. In
	 * either case the robot should update its internal state.
	 *
	 * The robot may veto the move if it is invalid.
	 *
	 * @param aMove
	 *            The position to add a stone to.
	 * @param aPlayer
	 *            The colour of the stone.
	 * @return true if the move was legal, false otherwise.
	 */
	boolean addMove(Move aMove, Player aPlayer);

	/**
	 * Asks the robot if it supports scoring of games. The returned value must
	 * be constant for the life time of the {@link GoEngine} instance.
	 *
	 * If this method returns <code>true</code> then {@link #getScore()} must
	 * return a valid {@link Score} object for all games.
	 *
	 * @return <code>true</code> if this engine is capable of scoring a game.
	 */
	default boolean canScore() {
		return false;
	}

	/**
	 * The identity of the robot.
	 *
	 * @return A string with the name of the robot. May contain spaces, should
	 *         not contain version number. Must be ASCII only.
	 */
	String getName();

	/**
	 * Asks the robot to compute the score. If {@link #canScore()} returns true,
	 * then this method must be implemented and must return a valid
	 * {@link Score} object for the current game.
	 *
	 * @return A new {@link Score} object or {@link Score#DRAW}.
	 */
	default Score getScore() {
		throw new UnsupportedOperationException("Engine doesn't support scoring!");
	}

	/**
	 * The version number of the robot.
	 *
	 * @return A string encoding the version number. Must be ASCII only.
	 */
	String getVersion();

	/**
	 * Starts a new game, the robot should clear its internal state to a new
	 * game.
	 */
	void newGame();

	/**
	 * Asks the robot for the next move for the given player.
	 *
	 * @param aPlayer
	 *            The player that a move should be constructed for.
	 * @return The move the robot would like to play.
	 */
	Move nextMove(Player aPlayer);

	/**
	 * This method is called when the board size changes. The robot may return
	 * false if it doesn't support the given board size.
	 *
	 * The Go Text Protocol mandates that this is called at least once before
	 * the first call to {@link #newGame()}.
	 *
	 * @param aSize
	 *            A positive integer larger than zero.
	 * @return true if the board size is supported, false otherwise.
	 */
	boolean resizeBoard(int aSize);

	/**
	 * Inform the robot about the komi value for the game.
	 *
	 * @param aKomi
	 *            The new komi value, any value is allowed.
	 */
	void setKomi(float aKomi);
}
