package com.chess.ui.engine;

import com.chess.statics.Symbol;
import com.chess.ui.interfaces.boards.LessonsBoardFace;
import com.chess.ui.interfaces.game_ui.GameFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.07.13
 * Time: 12:45
 */
public class ChessBoardLessons extends ChessBoard implements LessonsBoardFace {

	private static ChessBoardLessons instance;

	private ChessBoardLessons(GameFace gameFace) {
		super(gameFace);
	}

	public static ChessBoardLessons getInstance(GameFace gameFace) {
		final Long gameId = gameFace.getGameId();

		if (instance == null || instance.gameId == null || !instance.gameId.equals(gameId)) {
			instance = new ChessBoardLessons(gameFace);
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

	@Override
	public boolean isLatestMoveMadeUser() {
		return ply > 0 && ply % 2 == 0;
	}

	@Override
	public String getLastMoveStr() {
		int lastIndex = ply - 1;
		if (/*lastIndex >= tacticMoves.length || */lastIndex >= histDat.length) {
			return "Pe4"; // TODO invent logic here , we use hardcode to pass possibly invalid move
		}

		Move move = histDat[lastIndex].move; // get last move
		String piece = Symbol.EMPTY;
		int pieceCode = pieces[move.to];
		String moveStr;
		if (pieceCode == 1) { // set piece name
			piece = MovesParser.BLACK_KNIGHT;
		} else if (pieceCode == 2) {
			piece = MovesParser.BLACK_BISHOP;
		} else if (pieceCode == 3) {
			piece = MovesParser.BLACK_ROOK;
		} else if (pieceCode == 4) {
			piece = MovesParser.BLACK_QUEEN;
		} else if (pieceCode == 5) {
			piece = MovesParser.BLACK_KING;
		}

		String capture = Symbol.EMPTY;
		if (histDat[lastIndex].capture != 6) {
			capture = "x";
		}

		moveStr = piece + capture + movesParser.positionToString(move.to);   // Rxa7 is failed!!!
		if (moveStr.equalsIgnoreCase(MovesParser.B_KINGSIDE_MOVE_CASTLING)) {
			moveStr = MovesParser.KINGSIDE_CASTLING;
		} else if (moveStr.equalsIgnoreCase(MovesParser.B_QUEENSIDE_MOVE_CASTLING)) {
			moveStr = MovesParser.QUEENSIDE_CASTLING;
		}
		return moveStr.toLowerCase();
	}

}
