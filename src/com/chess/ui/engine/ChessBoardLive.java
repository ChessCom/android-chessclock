package com.chess.ui.engine;

import com.chess.backend.statics.AppConstants;
import com.chess.ui.interfaces.BoardToGameActivityFace;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 27.09.12
 * Time: 13:47
 */
public class ChessBoardLive extends ChessBoard {

	private static ChessBoardLive instance;

	// todo: remove
	public ChessBoardLive(BoardToGameActivityFace gameActivityFace) {
		super(gameActivityFace);
	}

	public static ChessBoardLive getInstance(BoardToGameActivityFace gameActivityFace) {
		final Long gameId = gameActivityFace.getGameId();
		if (instance == null || !instance.gameId.equals(gameId)) {
			instance = new ChessBoardLive(gameActivityFace);
			instance.gameId = gameId;
			instance.setInit(true);
			instance.genCastlePos(AppConstants.DEFAULT_GAMEBOARD_CASTLE);
		}
		return instance;
	}

	public static void resetInstance() {
		instance = null;
	}
}
