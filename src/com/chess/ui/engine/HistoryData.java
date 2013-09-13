package com.chess.ui.engine;

final public class HistoryData {
	public Move move;
	public int capture;
	int enPassant;
	int enPassantPrev;
	int fifty;
	boolean castleMask[] = {false, false, false, false};
	/**
	 * {@code 0} - black O-O; {@code 1} - black O-O-O; {@code 2} - white O-O; {@code 3} - white O-O-O;
	 */
	public int castleMaskPosition = -1;
	public String notation;
	public boolean whiteCanCastle = true;
	public boolean blackCanCastle = true;
}
