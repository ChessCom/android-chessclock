package com.chess.ui.interfaces;

import com.chess.backend.entity.SoundPlayer;

/**
 * GameActivityFace class
 *
 * @author alien_roger
 * @created at: 13.03.12 7:08
 */
public interface GameActivityFace {

	SoundPlayer getSoundPlayer();

	Boolean isUserColorWhite();

	Long getGameId();

	void showOptions();

	void showChoosePieceDialog(final int col, final int row);

	void newGame();

	void switch2Analysis(boolean isAnalysis);

    void turnScreenOff();
	
    void updateAfterMove();

	void invalidateGameScreen();

	/**
	 *
	 * @param message to be shown
	 * @param need2Finish tells the activity that it needs to finish
	 */
	void onGameOver(String message, boolean need2Finish);

	String getWhitePlayerName();

	String getBlackPlayerName();

	void onCheck();

	boolean currentGameExist();

	BoardFace getBoardFace();
}
