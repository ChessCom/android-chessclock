package com.chess.ui.interfaces;

import com.chess.ui.engine.ChessBoardLive;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameNetworkFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.10.13
 * Time: 11:46
 */
public abstract class AbstractGameNetworkFaceHelper implements GameNetworkFace {


	@Override
	public void showSubmitButtonsLay(boolean show) {

	}

	@Override
	public void switch2Chat() {

	}

	@Override
	public void playMove() {

	}

	@Override
	public void cancelMove() {

	}

	@Override
	public void goHome() {

	}

	@Override
	public boolean isUserColorWhite() {
		return true;
	}

	@Override
	public Long getGameId() {
		return 0L;
	}

	@Override
	public void showOptions() {

	}

	@Override
	public void showChoosePieceDialog(int col, int row) {

	}

	@Override
	public void newGame() {

	}

	@Override
	public void switch2Analysis() {

	}

	@Override
	public void releaseScreenLockFlag() {

	}

	@Override
	public void updateAfterMove() {

	}

	@Override
	public void invalidateGameScreen() {

	}

	@Override
	public void onGameOver(String title, String reason) {

	}

	@Override
	public String getWhitePlayerName() {
		return "";
	}

	@Override
	public String getBlackPlayerName() {
		return "";
	}

	@Override
	public void onCheck() {

	}

	@Override
	public void onNotationClicked(int pos) {}

	@Override
	public boolean currentGameExist() {
		return false;
	}

	@Override
	public BoardFace getBoardFace() {
		return ChessBoardLive.getInstance(this);
	}

	@Override
	public void toggleSides() {

	}

	@Override
	public void updateParentView() {

	}

	@Override
	public boolean isUserAbleToMove(int color) {
		return false;
	}

	@Override
	public boolean isObservingMode() {
		return false;
	}
}
