package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.util.AttributeSet;
import com.chess.backend.statics.AppConstants;

public class ChessBoardDailyView extends ChessBoardNetworkView {

	public ChessBoardDailyView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected boolean need2ShowSubmitButtons() {
		String sharedKey = AppConstants.PREF_SHOW_SUBMIT_MOVE_DAILY;
		return preferences.getBoolean(getAppData().getUsername() + sharedKey, true);
	}

	@Override
	public void playMove() {
		gameFace.playMove();
	}

	@Override
	public void cancelMove() {
		gameFace.cancelMove();
	}

}
