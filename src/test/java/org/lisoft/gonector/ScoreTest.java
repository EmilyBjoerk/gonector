package org.lisoft.gonector;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the {@link Score} class.
 *
 * @author Emily Björk
 */
@SuppressWarnings("javadoc")
public class ScoreTest {
	Locale oldLocale;

	@Before
	public void setup() {
		oldLocale = Locale.getDefault();
		// Use a locale with a decimal point.
		Locale.setDefault(Locale.FRANCE);
	}

	@After
	public void tearDown() {
		Locale.setDefault(oldLocale);
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testNegativeScore() {
		new Score(Player.BLACK, -32);
	}

	@Test
	public void testToString() {
		assertEquals("W+2.5", (new Score(Player.WHITE, 2.5)).toString());
		assertEquals("B+91", (new Score(Player.BLACK, 91)).toString());
		assertEquals("0", Score.DRAW.toString());
	}
}
