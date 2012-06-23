package com.chess.ui.interfaces;

import android.content.Context;

/**
 * GameActivityFace class
 *
 * @author alien_roger
 * @created at: 13.03.12 7:08
 */
public interface GameActivityFace2 {


	void showOptions();

	void showSubmitButtonsLay(boolean show);

	void showChoosePieceDialog(final int col, final int row);

	void switch2Chat();

	void newGame();

	void switch2Analysis(boolean isAnalysis);

    void turnScreenOff();
	
    void updateAfterMove();

	void invalidateGameScreen();

	/**
	 *
	 * @param message
	 * @param need2Finish tells the activity that it needs to finish
	 */
	void onGameOver(String message, boolean need2Finish);

	String getWhitePlayerName();

	String getBlackPlayerName();

	Context getMeContext();

	void onCheck();
}
