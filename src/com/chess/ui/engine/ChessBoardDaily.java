package com.chess.ui.engine;

import com.chess.ui.interfaces.game_ui.GameFace;

public class ChessBoardDaily extends ChessBoard {

	public ChessBoardDaily(GameFace gameFace) {
		super(gameFace);
		gameId = gameFace.getGameId();
	}
}
