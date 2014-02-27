package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.chess.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.interfaces.boards.BoardViewNetworkFace;
import com.chess.ui.interfaces.game_ui.GameNetworkFace;
import com.chess.ui.views.game_controls.ControlsDailyView;

public abstract class ChessBoardNetworkView extends ChessBoardBaseView implements BoardViewNetworkFace {

	private String whiteUserName;
	private String blackUserName;
	public GameNetworkFace gameNetworkFace;

	public ChessBoardNetworkView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected abstract boolean need2ShowSubmitButtons();

	public void setGameFace(GameNetworkFace gameActivityFace) {
		super.setGameFace(gameActivityFace);
		this.gameNetworkFace = gameActivityFace;

		whiteUserName = gameNetworkFace.getWhitePlayerName();
		blackUserName = gameNetworkFace.getBlackPlayerName();
	}

	@Override
	public void afterUserMove() {

		super.afterUserMove();

		getBoardFace().setMovesCount(getBoardFace().getPly());
		gameNetworkFace.invalidateGameScreen();

		if (!getBoardFace().isAnalysis()) {
			if (need2ShowSubmitButtons()) {
				getBoardFace().setSubmit(true);
				gameNetworkFace.showSubmitButtonsLay(true);
			} else {
				gameNetworkFace.updateAfterMove();
			}
		}

		isGameOver();
	}

	@Override
	public void flipBoard() {
		getBoardFace().setReside(!getBoardFace().isReside());

		gameNetworkFace.toggleSides();
		gameNetworkFace.invalidateGameScreen();
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (useTouchTimer) { // start count before next touch
			handler.postDelayed(checkUserIsActive, StaticData.WAKE_SCREEN_TIMEOUT);
			userActive = true;
		}

		if (squareSize == 0) {
			return super.onTouchEvent(event);
		}

		if (isLocked()) {
			return processTouchEvent(event);
		}

		trackTouchEvent = false;
		if (!getBoardFace().isAnalysis()) {
			if (ChessBoard.isFinishedEchessGameMode(getBoardFace()) || getBoardFace().isFinished() || getBoardFace().isSubmit() ||
					(getBoardFace().getPly() < getBoardFace().getMovesCount())) {
				return true;
			}

			if (TextUtils.isEmpty(whiteUserName) || TextUtils.isEmpty(blackUserName))
				return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void showChat() {
		gameNetworkFace.switch2Chat();
	}

	public void updatePlayerNames(String whitePlayerName, String blackPlayerName) {
		whiteUserName = whitePlayerName;
		blackUserName = blackPlayerName;
	}

	public void setControlsView(ControlsDailyView controlsView) {
		super.setControlsView(controlsView);


		controlsView.setBoardViewFace(this);
	}

	@Override
	protected void resetMoving() {
		super.resetMoving();
		if (getBoardFace().isSubmit()) {
			cancelMove();
		}
	}
}
