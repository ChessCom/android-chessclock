package com.chess.ui.interfaces.boards;

import com.chess.ui.engine.HistoryData;
import com.chess.ui.engine.Move;

import java.util.TreeSet;

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

	TreeSet<Move> gen();

	int getSide();

	boolean isAnalysis();

	void setMovesCount(int movesCount);

	void setSubmit(boolean submit);

	int getMovesCount();

	HistoryData[] getHistDat();

	void updateMoves(String newMove, boolean playSound);

	boolean makeMove(Move m);

	boolean makeMove(Move m, boolean playSound);

	boolean inCheck(int s);

	int[] getPieces();

	int getPiece(int pieceId);

	int[] getColor();

	int getColor(int i, int j);

	int getHply();

	int eval();

	int[][] getHistory();

	int reps();

	TreeSet<Move> genCaps();

	int getFifty();

	boolean isReside();

	void setReside(boolean reside);

	boolean isSubmit();

	void setXside(int xside);

	void setSide(int side);

	int getwKing();

	int[] getwKingMoveOO();

	int getbKing();

	int[] getbKingMoveOO();

	int[] getwKingMoveOOO();

	int[] getbKingMoveOOO();

	int[] getBoardColor();

	void setChess960(boolean chess960);

	int[] genCastlePos(String fen);

	void takeNext();

	CharSequence getMoveListSAN();

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

	boolean isWhite(int piecePosition);

	boolean isWhiteToMove();

	void setFinished(boolean finished);

	boolean isFinished();

	Move convertMove(int[] moveFT);

	Move convertMoveAlgebraic(String move);

	Move convertMoveCoordinate(String move);
}
