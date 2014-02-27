package com.chess.ui.interfaces.game_ui;

import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.interfaces.boards.BoardFace;

/**
 * GameFace class
 *
 * @author alien_roger
 * @created at: 13.03.12 7:08
 */
public interface GameFace {

	SoundPlayer getSoundPlayer();

	boolean isUserColorWhite();

	Long getGameId();

	void showOptions();

	void showChoosePieceDialog(final int file, final int rank);

	void newGame();

	void switch2Analysis();

	void releaseScreenLockFlag();

	void updateAfterMove();

	void invalidateGameScreen();

	void onGameOver(String title, String reason);

	String getWhitePlayerName();

	String getBlackPlayerName();

	void onCheck();

	boolean currentGameExist();

	BoardFace getBoardFace();

	void toggleSides();

	void onNotationClicked(int pos);

	void updateParentView();

	boolean userCanMovePieceByColor(int color);

	boolean isObservingMode();

	boolean isAlive();
}
