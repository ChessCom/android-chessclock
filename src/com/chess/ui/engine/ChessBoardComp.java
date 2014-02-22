package com.chess.ui.engine;

import com.chess.ui.interfaces.game_ui.GameFace;

public class ChessBoardComp extends ChessBoard {

	private boolean hint;
	private boolean computerMoving;

	public ChessBoardComp(GameFace gameFace) {
		super(gameFace);
	}

	public boolean isComputerMoving() {
		return computerMoving;
	}

	public void setComputerMoving(boolean computerMoving) {
		this.computerMoving = computerMoving;
	}

	public boolean isHint() {
		return hint;
	}

	public void setHint(boolean hint) {
		this.hint = hint;
	}
}
