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

	private static ChessBoardDiagram instance;
	private String[] puzzleMoves;

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

	@Override
	public void setPuzzleMoves(String moveList) {
		moveList =  movesParser.replaceSpecialSymbols(moveList);
		puzzleMoves = moveList.replaceAll(MovesParser.MOVE_NUMBERS_PATTERN, Symbol.EMPTY)
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

		Move lastUsersMove = convertMoveAlgebraic(getLastMoveSAN());
		Move tacticMove = convertMoveAlgebraic(puzzleMoves[lastIndex]);
		return lastUsersMove.equals(tacticMove);
	}
}
