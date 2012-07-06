package com.chess.ui.interfaces;

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

	void setHistDat(HistoryData[] histDat);

	boolean makeMove(Move m);

	boolean makeMove(Move m, boolean playSound);

	boolean inCheck(int s);

	int[] getPieces();

	int getPiece(int pieceId);

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

	void setInit(boolean init);

	boolean isInit();

	void setRetry(boolean retry);

	boolean isRetry();

	void setTacticCanceled(boolean tacticCanceled);

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
//    List<String> getMoveListSAN();

	String convertMoveLive();

	void setAnalysis(boolean analysis);

	void decreaseMovesCount();

	void increaseMovesCount();

	String convertMoveEchess();

	void setTacticMoves(String[] tacticMoves);

	String[] getTacticMoves();

	void setSec(int sec);

	int getSec();

	void setLeft(int left);

	void decreaseLeft();

	int getLeft();

	void increaseSec();

	boolean isTacticCanceled();

	int getTacticsCorrectMoves();

	void increaseTacticsCorrectMoves();

	boolean toggleAnalysis();

	boolean lastMoveContains(String piece, String moveTo);

	boolean isPossibleToMakeMoves();
}
