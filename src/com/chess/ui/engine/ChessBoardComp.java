package com.chess.ui.engine;

import com.chess.ui.interfaces.BoardToGameActivityFace;

public class ChessBoardComp extends ChessBoard {

	private static ChessBoardComp instance;

	private ChessBoardComp(BoardToGameActivityFace gameActivityFace) {
		super(gameActivityFace);
	}

	public static ChessBoardComp getInstance(BoardToGameActivityFace gameActivityFace) {
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
}
