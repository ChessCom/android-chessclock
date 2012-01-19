package com.chess.engine;

final public class HistoryData {
	public Move m;
	public int capture;
	int ep;
	int fifty;
	boolean castleMask[] = {false, false, false, false};
	public int what = -1;
	public String notation;
}
