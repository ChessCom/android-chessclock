package com.chess.ui.interfaces.game_ui;

/**
 * GameFace class
 *
 * @author alien_roger
 * @created at: 13.03.12 7:08
 */
public interface GameTacticsFace extends GameFace {

	void verifyMove();

	void showHint();

	void restart();

	void showAnswer();

	void vsComputer();

	void onStartTactic();

	boolean isLastMoveMadeUser();
}
