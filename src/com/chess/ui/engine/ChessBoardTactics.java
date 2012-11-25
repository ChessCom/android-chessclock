package com.chess.ui.engine;

import com.chess.backend.statics.StaticData;
import com.chess.ui.interfaces.GameActivityFace;
import com.chess.ui.interfaces.TacticBoardFace;

/**
 * ChessBoardTactics class
 *
 * @author alien_roger
 * @created at: 27.09.12 21:00
 */
public class ChessBoardTactics extends ChessBoard implements TacticBoardFace {

	private static ChessBoardTactics instance;
//	private boolean retry;
	private boolean tacticCanceled;
	private int secondsPassed = 0;
	private int secondsLeft = 0;
	private int tacticsCorrectMoves = 0;
	private String[] tacticMoves;

	private ChessBoardTactics(GameActivityFace gameActivityFace) {
		super(gameActivityFace);
	}

	public static ChessBoardTactics getInstance(GameActivityFace gameActivityFace) {
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

	public static void resetInstance(){
		instance = null;
	}

//	@Override
//	public int getSecondsPassed() {
//		return secondsPassed;
//	}
//
//	@Override
//	public void setSecondsPassed(int secondsPassed) {
//		this.secondsPassed = secondsPassed;
//	}
//
//	@Override
//	public int getSecondsLeft() {
//		return secondsLeft;
//	}
//
//	@Override
//	public void increaseSecondsPassed() {
//		secondsPassed++;
//
//		if(secondsLeft > 0)
//			secondsLeft--;
//	}
//
//	@Override
//	public void setSecondsLeft(int secondsLeft) {
//		this.secondsLeft = secondsLeft;
//	}

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
		this.tacticMoves = tacticMoves.replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
				.replaceAll("[.]", StaticData.SYMBOL_EMPTY)
				.replaceAll("  ", StaticData.SYMBOL_SPACE)
				.substring(1).split(StaticData.SYMBOL_SPACE);
	}

	@Override
	public String[] getTacticMoves() {
		return tacticMoves;
	}


	@Override
	public boolean lastTacticMoveIsCorrect() {
		int lastIndex = hply - 1;
		Move move = histDat[lastIndex].move; // get last move
		String piece = StaticData.SYMBOL_EMPTY;
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

		return tacticMoves[lastIndex].contains(piece) && tacticMoves[lastIndex].contains(moveTo);
	}

//	@Override
//	public boolean isRetry() {
//		return retry;
//	}
//
//	@Override
//	public void setRetry(boolean retry) {
//		this.retry = retry;
//	}

	@Override
	public int getTacticsCorrectMoves() {
		return tacticsCorrectMoves;
	}

	@Override
	public void increaseTacticsCorrectMoves() {
		tacticsCorrectMoves++;
	}

	@Override
	public boolean isLatestMoveMadeUser() {
		return hply > 0 && hply %2 == 0;
	}

	/*public void setTacticsCorrectMoves(int tacticsCorrectMoves) {
		this.tacticsCorrectMoves = tacticsCorrectMoves;
	}*/
}
