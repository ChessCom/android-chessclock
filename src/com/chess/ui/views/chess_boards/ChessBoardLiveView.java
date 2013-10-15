package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.util.AttributeSet;

public class ChessBoardLiveView extends ChessBoardNetworkView {

	public ChessBoardLiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected boolean need2ShowSubmitButtons() {
		return getAppData().getShowSubmitButtonsLive();
	}

	@Override
	protected boolean isGameOver() {
		if (!getBoardFace().isPossibleToMakeMoves()) {
			if (getBoardFace().isPerformCheck(getBoardFace().getSide())) {
				getBoardFace().getHistDat()[getBoardFace().getPly() - 1].notation += "#";
				gameNetworkFace.invalidateGameScreen();
			}

			getBoardFace().setFinished(true); // todo: probably it is better to set Finished flag by lcc.onGameEnded event
			return true;
		}

		if (getBoardFace().isPerformCheck(getBoardFace().getSide())) {
			getBoardFace().getHistDat()[getBoardFace().getPly() - 1].notation += "+";
			gameNetworkFace.invalidateGameScreen();
			gameNetworkFace.onCheck();
		}
		return false;
	}

	@Override
	public void playMove() {
		gameNetworkFace.playMove();
	}

	@Override
	public void cancelMove() {
		gameNetworkFace.cancelMove();
	}

	@Override
	public void goHome() {
		gameNetworkFace.goHome();
	}
}
