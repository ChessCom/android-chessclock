package com.chess.ui.engine;

import com.chess.ui.interfaces.game_ui.GameFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.09.13
 * Time: 14:42
 */
public class ChessBoardExplorer extends ChessBoard {

	private static ChessBoardExplorer instance;

	private ChessBoardExplorer(GameFace gameFace) {
		super(gameFace);
	}

	public static ChessBoardExplorer getInstance(GameFace gameFace) {
		final Long gameId = gameFace.getGameId();

		if (instance == null || instance.gameId == null || !instance.gameId.equals(gameId)) {
			instance = new ChessBoardExplorer(gameFace);
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