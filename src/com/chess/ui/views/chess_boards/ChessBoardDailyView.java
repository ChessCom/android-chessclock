package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.util.AttributeSet;
import com.chess.ui.interfaces.game_ui.GameNetworkFace;

public class ChessBoardDailyView extends ChessBoardNetworkView {

	public ChessBoardDailyView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setGameFace(GameNetworkFace gameActivityFace) {
		super.setGameFace(gameActivityFace);
		this.gameNetworkFace = gameActivityFace;
	}

	@Override
	protected boolean need2ShowSubmitButtons() {
		return getAppData().getShowSubmitButtonsDaily();
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

	}

}
