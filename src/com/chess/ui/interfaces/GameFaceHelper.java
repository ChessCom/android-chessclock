package com.chess.ui.interfaces;

import android.content.Context;
import com.chess.ui.engine.ChessBoardLive;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameNetworkFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.10.13
 * Time: 11:46
 */
public class GameFaceHelper implements GameNetworkFace {

	private final SoundPlayer soundPlayer;
	private Context context;
	private ChessBoardLive chessBoard;

	public GameFaceHelper(Context context) {
		this.context = context;
		soundPlayer = new SoundPlayer(context);
	}

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
	public SoundPlayer getSoundPlayer() {
		return soundPlayer;
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
	public void showChoosePieceDialog(int file, int rank) {

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
	public void onNotationClicked(int pos) {
	}

	@Override
	public boolean currentGameExist() {
		return false;
	}

	@Override
	public BoardFace getBoardFace() {
		if (chessBoard == null) {
			chessBoard = new ChessBoardLive(this);
		}
		return chessBoard;
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

	@Override
	public boolean isAlive() {
		return context != null;
	}
}
