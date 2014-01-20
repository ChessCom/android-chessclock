package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.util.AttributeSet;
import com.chess.ui.interfaces.game_ui.GameDailyFace;

public class ChessBoardDailyView extends ChessBoardNetworkView {

	private GameDailyFace gameDailyFace;

	public ChessBoardDailyView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setGameFace(GameDailyFace gameDailyFace) {
		super.setGameFace(gameDailyFace);
		this.gameDailyFace = gameDailyFace;
	}

	@Override
	protected boolean need2ShowSubmitButtons() {
		return getAppData().getShowSubmitButtonsDaily();
	}

	@Override
	public boolean moveBack() {
		if (getBoardFace().getPly() <= getBoardFace().getMovesCount()) {
			gameDailyFace.showConditionsBtn(false);
		}

		return super.moveBack();
	}

	@Override
	public boolean moveForward() {
		if ((getBoardFace().getPly() + 1) == getBoardFace().getMovesCount()) {
			gameDailyFace.showConditionsBtn(true);
		}
		return super.moveForward();
	}

	@Override
	public void openConditions() {
		gameDailyFace.openConditions();
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
