package com.chess.ui.engine;

import com.chess.ui.interfaces.game_ui.GameFace;

/**
 * ChessBoardLive class
 *
 * @author alien_roger
 * @created at: 27.09.12 21:15
 */
public class ChessBoardLive extends ChessBoard {

	public ChessBoardLive(GameFace gameFace) {
		super(gameFace);
		gameId = gameFace.getGameId();
	}
}
