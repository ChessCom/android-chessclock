package com.chess.ui.interfaces.boards;

import com.chess.ui.engine.HistoryData;
import com.chess.ui.engine.Move;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * BoardFace class
 *
 * @author alien_roger
 * @created at: 05.03.12 5:16
 */
public interface BoardFace {

	int getMode();

	void setMode(int mode);

	void takeBack();

	List<Move> generateLegalMoves();

	int getSide();

	boolean isAnalysis();

	void setMovesCount(int movesCount);

	void setSubmit(boolean submit);

	int getMovesCount();

	HistoryData[] getHistDat();

	boolean makeMove(String newMove, boolean playSound);

	boolean makeMove(Move m);

	boolean makeMove(Move m, boolean playSound);

	boolean inCheck(int s);

	int[] getPieces();

	int getPiece(int pieceId);

	int[] getColor();

	int getColor(int i, int j);

	int getPly();

	int eval();

	int[][] getHistory();

	String generateBaseFen();

	int reps();

	List<Move> generateCapturesAndPromotes();

	int getFifty();

	boolean isReside();

	void setReside(boolean reside);

	boolean isSubmit();

	void setOppositeSide(int xside);

	void setSide(int side);

	int getWhiteKing();

	int[] getWhiteKingMoveOO();

	int getBlackKing();

	int[] getBlackKingMoveOO();

	int[] getWhiteKingMoveOOO();

	int[] getBlackKingMoveOOO();

	int[] getBoardColor();

	void setChess960(boolean chess960);

	int[] buildBoardFromFen(String fen);

	void takeNext();

	String getMoveListSAN();

	String[] getNotationArray();

	String convertMoveLive();

	void setAnalysis(boolean analysis);

	void decreaseMovesCount();

	String convertMoveEchess();

	boolean toggleAnalysis();

	boolean isPossibleToMakeMoves();

	void setupBoard(String fen);

	boolean isJustInitialized();

	void setJustInitialized(boolean justInitialized);

	Move getLastMove();

	Move getNextMove();

	boolean isWhiteToMove();

	void setFinished(boolean finished);

	boolean isFinished();

	Move convertMove(int[] moveFT);

	Move convertMoveAlgebraic(String move);

	Move convertMoveCoordinate(String move);

	void switchSides();

	void switchEnPassant();

	CopyOnWriteArrayList<Move> generateValidMoves(boolean forceSwitchSide);

	String generateFullFen();
}
