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
		return !getBoardFace().isPossibleToMakeMoves();
	}

	@Override
	public void openConditions() {
		// not used here.
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
