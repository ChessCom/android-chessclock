package com.chess.ui.engine;

import com.chess.ui.interfaces.game_ui.GameFace;

public class ChessBoardDaily extends ChessBoard {

	private static ChessBoardDaily instance;

	private ChessBoardDaily(GameFace gameFace) {
		super(gameFace);
	}

	public static ChessBoardDaily getInstance(GameFace gameFace) {
		final Long gameId = gameFace.getGameId();
		if (instance == null || instance.gameId == null || !instance.gameId.equals(gameId)) {
			instance = new ChessBoardDaily(gameFace);
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
