package com.chess.ui.interfaces;

import android.view.View;

/**
 * BoardFace class
 *
 * @author alien_roger
 * @created at: 05.03.12 5:16
 */
public interface BoardViewFace {

	void showOptions(View view);

//	void flipBoard();

	void switchAnalysis();

	void moveBack();

	void moveForward();

	void newGame();
}
