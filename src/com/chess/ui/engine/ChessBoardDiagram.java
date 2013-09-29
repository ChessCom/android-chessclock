package com.chess.ui.engine;

import com.chess.ui.interfaces.game_ui.GameFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.09.13
 * Time: 17:40
 */
public class ChessBoardDiagram extends ChessBoard {

	private static ChessBoardDiagram instance;

	private ChessBoardDiagram(GameFace gameFace) {
		super(gameFace);
	}

	public static ChessBoardDiagram getInstance(GameFace gameFace) {
		if (instance == null ) {
			instance = new ChessBoardDiagram(gameFace);
		}
		return instance;
	}

	public static void resetInstance(){
		instance = null;
	}
}
