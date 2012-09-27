package com.chess.ui.engine;

import com.chess.ui.interfaces.BoardToGameActivityFace;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 27.09.12
 * Time: 13:41
 */
public class ChessBoardTactics extends ChessBoard {

	private static ChessBoardTactics instance;
	private Boolean justInitialized;

	// todo: remove
	public ChessBoardTactics(BoardToGameActivityFace gameActivityFace) {
		super(gameActivityFace);
	}

	public static ChessBoardTactics getInstance(BoardToGameActivityFace gameActivityFace) {
		final Long gameId = gameActivityFace.getGameId();
		if (instance == null || instance.gameId == null || !instance.gameId.equals(gameId)) {
			instance = new ChessBoardTactics(gameActivityFace);
			instance.gameId = gameId;
			instance.justInitialized = true;
		} else {
			instance.justInitialized = false;
		}
		return instance;
	}

	public boolean isJustInitialized() {
		return justInitialized;
	}

	// todo: extract TT-specific stuff from ChessBoard
}
