package com.chess.core.interfaces;

/**
 * BoardFace class
 *
 * @author alien_roger
 * @created at: 05.03.12 5:16
 */
public interface BoardViewFace {

	void showOptions();

	void flipBoard();

	void switchAnalysis();

	void switchChat();

	void moveBack();

	void moveForward();

	void showHint();

	void newGame();
}
