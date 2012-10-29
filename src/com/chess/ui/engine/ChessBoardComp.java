package com.chess.ui.engine;

import com.chess.ui.interfaces.GameActivityFace;

public class ChessBoardComp extends ChessBoard {

	private static ChessBoardComp instance;
	private boolean hint;
	private boolean computerMoving;

	private ChessBoardComp(GameActivityFace gameActivityFace) {
		super(gameActivityFace);
	}

	public static ChessBoardComp getInstance(GameActivityFace gameActivityFace) {
		if (instance == null) {
			instance = new ChessBoardComp(gameActivityFace);
			instance.justInitialized = true;
		} else {
			instance.justInitialized = false;
		}
		return instance;
	}

	public static void resetInstance(){
		instance = null;
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
