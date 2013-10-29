package com.chess.ui.interfaces.boards;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.10.13
 * Time: 20:50
 */
public interface PuzzlesBoardFace extends BoardFace {

	void setPuzzleMoves(String tacticMoves);

	String[] getPuzzleMoves();

	boolean isLastPuzzleMoveCorrect();

}
