package com.chess.ui.engine;

import com.chess.backend.statics.StaticData;
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
		return hply > 0 && hply % 2 == 0;
	}

	@Override
	public String getLastMoveStr() {
		int lastIndex = hply - 1;
		if (/*lastIndex >= tacticMoves.length || */lastIndex >= histDat.length) {
			return "Pe4"; // TODO invent logic here , we use hardcode to pass possibly invalid move
		}

		Move move = histDat[lastIndex].move; // get last move
		String piece = StaticData.SYMBOL_EMPTY;
		int pieceCode = pieces[move.to];
		String moveStr;
		if (isReside()) {
			if (pieceCode == 1) { // set piece name
				piece = MoveParser.BLACK_KNIGHT;
			} else if (pieceCode == 2) {
				piece = MoveParser.BLACK_BISHOP;
			} else if (pieceCode == 3) {
				piece = MoveParser.BLACK_ROOK;
			} else if (pieceCode == 4) {
				piece = MoveParser.BLACK_QUEEN;
			} else if (pieceCode == 5) {
				piece = MoveParser.BLACK_KING;
			}
			moveStr = piece + MoveParser.positionToString(move.to);
			if (moveStr.equalsIgnoreCase(MoveParser.B_KINGSIDE_MOVE_CASTLING)) {
				moveStr = MoveParser.KINGSIDE_CASTLING;
			} else if (moveStr.equalsIgnoreCase(MoveParser.B_QUEENSIDE_MOVE_CASTLING)) {
				moveStr = MoveParser.QUEENSIDE_CASTLING;
			}
		} else {
			if (pieceCode == 1) { // set piece name
				piece = MoveParser.WHITE_KNIGHT;
			} else if (pieceCode == 2) {
				piece = MoveParser.WHITE_BISHOP;
			} else if (pieceCode == 3) {
				piece = MoveParser.WHITE_ROOK;
			} else if (pieceCode == 4) {
				piece = MoveParser.WHITE_QUEEN;
			} else if (pieceCode == 5) {
				piece = MoveParser.WHITE_KING;
			}
			moveStr = piece + MoveParser.positionToString(move.to);
			if (moveStr.equalsIgnoreCase(MoveParser.W_KINGSIDE_MOVE_CASTLING)) {
				moveStr = MoveParser.KINGSIDE_CASTLING;
			} else if (moveStr.equalsIgnoreCase(MoveParser.W_QUEENSIDE_MOVE_CASTLING)) {
				moveStr = MoveParser.QUEENSIDE_CASTLING;
			}
		}
		return moveStr;
	}

}
