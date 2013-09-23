package com.chess.ui.engine;

import com.chess.ui.interfaces.game_ui.GameFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.09.13
 * Time: 16:12
 */
public class ChessBoardAnalysis extends ChessBoard {

	private static ChessBoardAnalysis instance;

	private ChessBoardAnalysis(GameFace gameFace) {
		super(gameFace);
	}

	public static ChessBoardAnalysis getInstance(GameFace gameFace) {
		if (instance == null ) {
			instance = new ChessBoardAnalysis(gameFace);
		}
		return instance;
	}

	public static void resetInstance(){
		instance = null;
	}
}