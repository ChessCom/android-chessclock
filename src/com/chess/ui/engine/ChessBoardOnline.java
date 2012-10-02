package com.chess.ui.engine;

import com.chess.ui.interfaces.BoardToGameActivityFace;

public class ChessBoardOnline extends ChessBoard {

	private static ChessBoardOnline instance;

	private ChessBoardOnline(BoardToGameActivityFace gameActivityFace) {
		super(gameActivityFace);
	}

	public static ChessBoardOnline getInstance(BoardToGameActivityFace gameActivityFace) {
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
