package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.util.AttributeSet;
import com.chess.backend.statics.AppConstants;

public class ChessBoardLiveView extends ChessBoardNetworkView {

	public ChessBoardLiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected boolean need2ShowSubmitButtons() {
		return preferences.getBoolean(getAppData().getUsername() + AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE, false);
	}

	@Override
	protected boolean isGameOver() {
		if (!getBoardFace().isPossibleToMakeMoves()) {
			if (getBoardFace().inCheck(getBoardFace().getSide())) {
				getBoardFace().getHistDat()[getBoardFace().getPly() - 1].notation += "#";
				gameNetworkFace.invalidateGameScreen();
			}

			getBoardFace().setFinished(true); // todo: probably it is better to set Finished flag by lcc.onGameEnded event
			return true;
		}

		if (getBoardFace().inCheck(getBoardFace().getSide())) {
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
}
