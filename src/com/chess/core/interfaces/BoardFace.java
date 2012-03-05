package com.chess.core.interfaces;

import com.chess.engine.Board2;
import com.chess.engine.HistoryData;
import com.chess.engine.Move;

import java.util.TreeSet;

/**
 * BoardFace class
 *
 * @author alien_roger
 * @created at: 05.03.12 5:16
 */
public interface BoardFace {
	int getBoardMode();
	void takeBack();
	TreeSet<Move> gen();
	int getSide();
	boolean isAnalysis();
	void setMovesCount(int movesCount);
	void setSubmit(boolean submit);
	int getMovesCount();

	HistoryData[] getHistDat();
	void setHistDat(HistoryData[] histDat);

	boolean makeMove(Move m) ;
	boolean makeMove(Move m, boolean playSound);
	boolean inCheck(int s);
	int[] getPiece();
	int[] getColor();
	int getColor(int i, int j);
	int getPiece(int i, int j);
	int getHply();

	int eval();
	int[][] getHistory();
	int reps();
	TreeSet<Move> genCaps();
	int getFifty();
	boolean isReside();
	void setReside(boolean reside);
	boolean isSubmit();
	Board2 getBoard();
	void setInit(boolean init);
	boolean isInit();
}
