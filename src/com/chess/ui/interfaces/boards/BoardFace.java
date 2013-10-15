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

	List<Move> generateLegalMoves();

	/**
	 *
	 * @return {@code 0} if == ChessBoard.WHITE_SIDE, else {@code 1} if == ChessBoard.BLACK_SIDE
	 */
	int getSide();

	boolean isAnalysis();

	void setMovesCount(int movesCount);

	void setSubmit(boolean submit);

	int getMovesCount();

	HistoryData[] getHistDat();

	boolean makeMove(String newMove, boolean playSound);

	boolean makeMove(Move m);

	boolean makeMove(Move m, boolean playSound);

	boolean takeBack();

	boolean takeNext();

	boolean isPerformCheck(int s);

	int getPiece(int pieceId);

	int getColor(int color);

	int getColor(int i, int j);

	int getPly();

	String generateBaseFen();

	int getRepetitions();

	boolean isReside();

	void setReside(boolean reside);

	boolean isSubmit();

	void setOppositeSide(int xside);

	void setSide(int side);

	int[] getBoardColor();

	void setChess960(boolean chess960);

	void setupCastlingPositions(String fen);

	String getMoveListSAN();

	String[] getFullNotationsArray();

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

	String getLastMoveSAN();

	Move getLastMove();

	Move getNextMove();

	boolean isWhiteToMove();

	void setFinished(boolean finished);

	boolean isFinished();

	int[] parseCoordinate(String actualMove);

	Move convertMove(int[] moveFT);

	Move convertMoveAlgebraic(String move);

	Move convertMoveCoordinate(String move);

	void switchSides();

	void switchEnPassant();

	CopyOnWriteArrayList<Move> generateValidMoves(boolean forceSwitchSide);

	String generateFullFen();

	boolean checkAndParseMovesList(String moveList);

	boolean isPromote(int from, int to);
}
