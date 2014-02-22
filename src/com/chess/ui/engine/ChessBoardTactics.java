package com.chess.ui.engine;

import com.chess.statics.Symbol;
import com.chess.ui.interfaces.boards.TacticBoardFace;
import com.chess.ui.interfaces.game_ui.GameFace;

/**
 * ChessBoardTactics class
 *
 * @author alien_roger
 * @created at: 27.09.12 21:00
 */
public class ChessBoardTactics extends ChessBoard implements TacticBoardFace {

	private boolean tacticCanceled;

	private int correctMovesCnt;
	private String[] tacticMoves;

	public ChessBoardTactics(GameFace gameFace) {
		super(gameFace);
		gameId = gameFace.getGameId();
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
		tacticMoves = removeCommentsAndAlternatesFromMovesList(tacticMoves);
		this.tacticMoves = MovesParser.removeNumbers(tacticMoves)
				.replaceAll("  ", Symbol.SPACE)
				.substring(1).split(Symbol.SPACE);
	}

	@Override
	public String[] getTacticMoves() {
		return tacticMoves;
	}


	@Override
	public boolean isLastTacticMoveCorrect() {
		int lastIndex = ply - 1;

		String lastUserMove = getLastMoveSAN();
		if (lastUserMove == null) {
			return false;
		}
		// take back so the board parsing logic will understand that we converting previous move
		takeBack();
		Move userLastMove = convertMoveAlgebraic(lastUserMove);
		Move validMove = convertMoveAlgebraic(tacticMoves[lastIndex]);

		if (userLastMove == null || validMove == null) {
			return false;
		}
		// step forward to the normal, current state
		takeNext(false);

		return userLastMove.equals(validMove);
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
	public boolean isLastMoveMadeWhitePlayer() {
		return ply >= 0 && ply % 2 == 1;
	}

	@Override
	public boolean isLastMoveMadeBlackPlayer() {
		return ply > 0 && ply % 2 == 0; // side == WHITE_SIDE
	}
}
