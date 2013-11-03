package com.chess.ui.interfaces.boards;

/**
 * BoardFace class
 *
 * @author alien_roger
 * @created at: 05.03.12 5:16
 */
public interface BoardViewFace {

	void showOptions();

	void switchAnalysis();

	boolean moveBack();

	boolean moveForward();

	void moveBackFast();

	void moveForwardFast();

	void newGame();

	void setFastMovesMode(boolean fastMode);
}
