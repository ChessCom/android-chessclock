package com.chess.ui.interfaces;

/**
 * GameActivityFace class
 *
 * @author alien_roger
 * @created at: 13.03.12 7:08
 */
public interface GameTacticsActivityFace extends GameActivityFace {

	void verifyMove();

	void showHelp();

	void restart();

	void showHint();

	void showStats();
}
