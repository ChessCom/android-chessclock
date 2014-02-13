package com.chess.ui.engine;

import com.chess.ui.interfaces.game_ui.GameFace;

public class ChessBoardOnline extends ChessBoard {

	private static ChessBoardOnline instance;

	private ChessBoardOnline(GameFace gameFace) {
		super(gameFace);
	}

	public static ChessBoardOnline getInstance(GameFace gameFace) {
		final Long gameId = gameFace.getGameId();
		if (instance == null || instance.gameId == null || !instance.gameId.equals(gameId)) {
			instance = new ChessBoardOnline(gameFace);
			instance.gameId = gameId;
			instance.justInitialized = true;
		} else {
			instance.justInitialized = false;
		}
		return instance;
	}

	public static void resetInstance() {
		instance = null;
	}
}
