package com.chess.ui.interfaces.game_ui;

import android.view.View;
import com.chess.backend.statics.SoundPlayer;
import com.chess.ui.interfaces.boards.BoardFace;

/**
 * GameFace class
 *
 * @author alien_roger
 * @created at: 13.03.12 7:08
 */
public interface GameFace {

	SoundPlayer getSoundPlayer();

	Boolean isUserColorWhite();

	Long getGameId();

	void showOptions(View view);

	void showChoosePieceDialog(final int col, final int row);

	void newGame();

//	void switch2Analysis(boolean isAnalysis);
	void switch2Analysis();

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

	void toggleSides();
}
