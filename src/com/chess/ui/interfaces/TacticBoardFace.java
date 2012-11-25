package com.chess.ui.interfaces;

/**
 * TacticBoardFace class
 *
 * @author alien_roger
 * @created at: 28.09.12 21:45
 */
public interface TacticBoardFace extends BoardFace {
//	void setRetry(boolean retry);
//
//	boolean isRetry();

	void setTacticCanceled(boolean tacticCanceled);

	void setTacticMoves(String tacticMoves);

	String[] getTacticMoves();

//	void setSecondsPassed(int sec);
//
//	int getSecondsPassed();
//
//	void setSecondsLeft(int left);
//
//	int getSecondsLeft();
//
//	void increaseSecondsPassed();

	boolean isTacticCanceled();

	int getTacticsCorrectMoves();

	void increaseTacticsCorrectMoves();

	boolean lastTacticMoveIsCorrect();

	public boolean isLatestMoveMadeUser();
}
