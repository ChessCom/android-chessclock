package com.chess.ui.engine;

import com.chess.ui.interfaces.BoardToGameActivityFace;

/**
 * ChessBoardTactics class
 *
 * @author alien_roger
 * @created at: 27.09.12 21:00
 */
public class ChessBoardTactics extends ChessBoard {

	private static ChessBoardTactics instance;

	private ChessBoardTactics(BoardToGameActivityFace gameActivityFace) {
		super(gameActivityFace);
	}

	public static ChessBoardTactics getInstance(BoardToGameActivityFace gameActivityFace) {
		final Long gameId = gameActivityFace.getGameId();
		if (instance == null || instance.gameId == null || !instance.gameId.equals(gameId)) {
			instance = new ChessBoardTactics(gameActivityFace);
			instance.gameId = gameId;
			instance.restored = false;
		} else {
			instance.restored = true;
		}
		return instance;
	}

	public static void resetInstance(){
		instance = null;
	}
}
