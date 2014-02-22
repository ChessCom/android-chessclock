package com.chess.ui.engine;

import com.chess.statics.Symbol;
import com.chess.ui.interfaces.boards.PuzzlesBoardFace;
import com.chess.ui.interfaces.game_ui.GameFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.09.13
 * Time: 17:40
 */
public class ChessBoardDiagram extends ChessBoard implements PuzzlesBoardFace {

	private String[] puzzleMoves;

	public ChessBoardDiagram(GameFace gameFace) {
		super(gameFace);
	}

	@Override
	public void setPuzzleMoves(String moveList) {
		moveList = movesParser.replaceSpecialSymbols(moveList);
		puzzleMoves = moveList.replaceAll(MovesParser.MOVE_NUMBERS_PATTERN, Symbol.EMPTY)
				.replaceAll("\\.\\.", Symbol.EMPTY)
				.replaceAll(DOUBLE_SPACE, Symbol.SPACE)
				.replaceAll(DOUBLE_SPACE, Symbol.SPACE)
				.trim().split(Symbol.SPACE);
	}

	@Override
	public String[] getPuzzleMoves() {
		return puzzleMoves;
	}


	@Override
	public boolean isLastPuzzleMoveCorrect() {
		int lastIndex = ply - 1;
		String lastUserMove = getLastMoveSAN();
		if (lastUserMove == null) {
			return false;
		}

		// take back so the board parsing logic will understand that we converting previous move
		takeBack();
		Move userLastMove = convertMoveAlgebraic(lastUserMove);
		Move validMove = convertMoveAlgebraic(puzzleMoves[lastIndex]);

		if (userLastMove == null || validMove == null) {
			return false;
		}
		// step forward to the normal, current state
		takeNext(false);

		return userLastMove.equals(validMove);
	}
}
