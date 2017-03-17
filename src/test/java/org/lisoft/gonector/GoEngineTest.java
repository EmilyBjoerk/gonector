package org.lisoft.gonector;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * This test suite tests the default implemented interface parts of
 * {@link GoEngine}.
 *
 * @author Emily Björk
 */
@SuppressWarnings("javadoc")
public class GoEngineTest {

	/**
	 * Dummy class to check default methods.
	 *
	 * @author Emily Björk
	 */
	public static class TestEngine implements GoEngine {
		@Override
		public boolean addMove(Move aMove, Player aPlayer) {
			return false;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public String getVersion() {
			return null;
		}

		@Override
		public void newGame() {
			/* No-op */
		}

		@Override
		public Move nextMove(Player aPlayer) {
			return null;
		}

		@Override
		public boolean resizeBoard(int aSize) {
			return false;
		}

		@Override
		public void setKomi(float aKomi) {
			/* No-op */
		}
	}

	@Test
	public void testDefaultCanScore() {
		assertFalse(new TestEngine().canScore());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testDefaultGetScore() {
		new TestEngine().getScore();
	}
}
