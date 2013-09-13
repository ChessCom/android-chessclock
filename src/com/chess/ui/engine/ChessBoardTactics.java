package com.chess.ui.engine;

import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.Symbol;
import com.chess.ui.interfaces.boards.TacticBoardFace;
import com.chess.ui.interfaces.game_ui.GameFace;

/**
 * ChessBoardTactics class
 *
 * @author alien_roger
 * @created at: 27.09.12 21:00
 */
public class ChessBoardTactics extends ChessBoard implements TacticBoardFace {

	private static ChessBoardTactics instance;
	private boolean tacticCanceled;

	private int correctMovesCnt;
	private String[] tacticMoves;

	private ChessBoardTactics(GameFace gameFace) {
		super(gameFace);
	}

	public static ChessBoardTactics getInstance(GameFace gameFace) {
		final Long gameId = gameFace.getGameId();

		if (instance == null || instance.gameId == null || !instance.gameId.equals(gameId)) {
			instance = new ChessBoardTactics(gameFace);
			instance.gameId = gameId;
			instance.justInitialized = true;
		} else {
			instance.justInitialized = false;
		}
		return instance;
	}

	public static void resetInstance(){
		instance = null;
	}

	@Override
	public boolean isTacticCanceled() {
		return tacticCanceled;
	}

	@Override
	public void setTacticCanceled(boolean tacticCanceled) {
		this.tacticCanceled = tacticCanceled;
	}

	@Override
	public void setTacticMoves(String tacticMoves) {
		this.tacticMoves = tacticMoves.replaceAll(AppConstants.MOVE_NUMBERS_PATTERN, Symbol.EMPTY)
				.replaceAll("[.]", Symbol.EMPTY)
				.replaceAll("  ", Symbol.SPACE)
				.substring(1).split(Symbol.SPACE);
	}

	@Override
	public String[] getTacticMoves() {
		return tacticMoves;
	}


	@Override
	public boolean lastTacticMoveIsCorrect() {
		int lastIndex = ply - 1;
		if (lastIndex >= tacticMoves.length || lastIndex >= histDat.length) {
			return false;
		}

		Move move = histDat[lastIndex].move; // get last move
		String piece = Symbol.EMPTY;
		int pieceCode = pieces[move.to];
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
		String moveTo = MoveParser.positionToString(move.to);
//		Log.d("TEST_MOVE", "piece " + piece + " | move to " + moveTo + " : tactic last move = " + tacticMoves[lastIndex]);
		if (move.isCastling()){
			moveTo = "O-O"; // TODO verify queen side castling
		}
		return tacticMoves[lastIndex].contains(piece) && tacticMoves[lastIndex].contains(moveTo);
	}

	@Override
	public int getCorrectMovesCnt() {
		return correctMovesCnt;
	}

	@Override
	public void increaseTacticsCorrectMoves() {
		correctMovesCnt++;
	}

	@Override
	public boolean isLatestMoveMadeUser() {
		return ply > 0 && ply %2 == 0;
	}
}
