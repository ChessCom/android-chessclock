package com.chess.model;

public class TacticItem {

	private String id;
	private String fen;
	private String moveList;
	private String attemptCnt;
	private String passedCnt;
	private String rating;
	private String avgSeconds;
	private boolean stop;


	public TacticItem(String[] values) {
		id = values[0];
		fen = values[1];
		moveList = values[2];
		attemptCnt = values[3];
		passedCnt = values[4];
		rating = values[5];
		avgSeconds = values[6];
	}

	public String getAttemptCnt() {
		return attemptCnt;
	}

	public String getAvgSeconds() {
		return avgSeconds;
	}

	public String getFen() {
		return fen;
	}

	public String getId() {
		return id;
	}

	public String getMoveList() {
		return moveList;
	}

	public String getPassedCnt() {
		return passedCnt;
	}

	public String getRating() {
		return rating;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}
}
