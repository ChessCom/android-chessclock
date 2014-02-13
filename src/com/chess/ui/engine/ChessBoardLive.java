package com.chess.ui.engine;

import com.chess.ui.interfaces.game_ui.GameFace;

/**
 * ChessBoardLive class
 *
 * @author alien_roger
 * @created at: 27.09.12 21:15
 */
public class ChessBoardLive extends ChessBoard {

	private static ChessBoardLive instance;

	private ChessBoardLive(GameFace gameFace) {
		super(gameFace);
	}

	public static ChessBoardLive getInstance(GameFace gameFace) {
		final Long gameId = gameFace.getGameId();
		if (instance == null || instance.gameId == null || !instance.gameId.equals(gameId)) {
			instance = new ChessBoardLive(gameFace);
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
