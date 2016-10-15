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
 * An exception that is thrown when parsing of the input command from GTP
 * failed.
 *
 * @author Emily Björk
 */
public class SyntaxErrorException extends Exception {
	/**
	 * Default generated serial number to keep Eclipse happy.
	 */
	private static final long serialVersionUID = 5188368640183899108L;

	/**
	 * Creates a new syntax error exception.
	 *
	 * @param aMessage
	 *            The message to send to the controller.
	 */
	public SyntaxErrorException(String aMessage) {
		super(aMessage);
	}

	/**
	 * Creates a new syntax error exception with a cause.
	 *
	 * @param aMessage
	 *            The message to send to the controller.
	 * @param aCause
	 *            The cause of this exception.
	 */
	public SyntaxErrorException(String aMessage, Throwable aCause) {
		super(aMessage, aCause);
	}
}
