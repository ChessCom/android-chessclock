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
		this.tacticMoves = movesParser.removeNumbers(tacticMoves)
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

		String tacticCorrectMove = tacticMoves[lastIndex];
		tacticCorrectMove = tacticCorrectMove.replaceAll(MovesParser.SPECIAL_SYMBOLS_PATTERN, Symbol.EMPTY); // remove special symbols
		String lastMoveSAN = getLastMoveSAN().replaceAll(MovesParser.SPECIAL_SYMBOLS_PATTERN, Symbol.EMPTY); // remove special symbols
		return tacticCorrectMove.equals(lastMoveSAN);
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
