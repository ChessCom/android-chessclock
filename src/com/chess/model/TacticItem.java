package com.chess.model;

import com.chess.backend.statics.StaticData;

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

	public int getAvgSecondsInt() {
		return Integer.parseInt(avgSeconds);
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

	public String getSaveString(){
//48566
// :r4rk1/pp2p2p/6p1/3Pq3/2P1P1p1/P4nP1/1R2K2P/3Q1B1R w - - 0 1
// :1. Qc2 Nd4+ 2. Kd1 Nxc2
// :547
// :389
// :633
// :38
		StringBuilder builder = new StringBuilder();
		return builder.append(id).append(StaticData.SYMBOL_COLON)
				.append(fen).append(StaticData.SYMBOL_COLON)
				.append(moveList).append(StaticData.SYMBOL_COLON)
				.append(attemptCnt).append(StaticData.SYMBOL_COLON)
				.append(passedCnt).append(StaticData.SYMBOL_COLON)
				.append(rating).append(StaticData.SYMBOL_COLON)
				.append(avgSeconds).append(StaticData.SYMBOL_COLON).toString();
	}
}
