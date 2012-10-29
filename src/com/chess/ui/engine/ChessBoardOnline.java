package com.chess.ui.engine;

import com.chess.ui.interfaces.GameActivityFace;

public class ChessBoardOnline extends ChessBoard {

	private static ChessBoardOnline instance;

	private ChessBoardOnline(GameActivityFace gameActivityFace) {
		super(gameActivityFace);
	}

	public static ChessBoardOnline getInstance(GameActivityFace gameActivityFace) {
		final Long gameId = gameActivityFace.getGameId();
		if (instance == null || instance.gameId == null || !instance.gameId.equals(gameId)) {
			instance = new ChessBoardOnline(gameActivityFace);
			instance.gameId = gameId;
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
