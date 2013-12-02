package com.chess.ui.interfaces.boards;

/**
 * TacticBoardFace class
 *
 * @author alien_roger
 * @created at: 28.09.12 21:45
 */
public interface TacticBoardFace extends BoardFace {

	void setTacticCanceled(boolean tacticCanceled);

	void setTacticMoves(String tacticMoves);

	String[] getTacticMoves();

	boolean isTacticCanceled();

	int getCorrectMovesCnt();

	void increaseTacticsCorrectMoves();

	boolean isLastTacticMoveCorrect();

	public boolean isLatestMoveMadeUser();

	public void setPly(int ply); // need to set correct ply for tactics
}
