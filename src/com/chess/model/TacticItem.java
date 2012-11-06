package com.chess.model;

import com.chess.backend.statics.StaticData;

public class TacticItem {

	private long id;
	private String fen;
	private String moveList;
	private int attemptCnt;
	private int passedCnt;
	private int rating;
	private int avgSeconds;
	private boolean stop;
	private String user;

	public TacticItem() {
	}

	public TacticItem(String[] values) {
		id = Long.parseLong(values[0]);
		fen = values[1];
		moveList = values[2];
		attemptCnt = Integer.parseInt(values[3]);
		passedCnt = Integer.parseInt(values[4]);
		rating = Integer.parseInt(values[5]);
		avgSeconds = Integer.parseInt(values[6]);
	}



	public int getAttemptCnt() {
		return attemptCnt;
	}

	public int getAvgSeconds() {
		return avgSeconds;
	}

	public String getFen() {
		return fen;
	}

	public long getId() {
		return id;
	}

	public String getMoveList() {
		return moveList;
	}

	public int getPassedCnt() {
		return passedCnt;
	}

	public int getRating() {
		return rating;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public void setAttemptCnt(int attemptCnt) {
		this.attemptCnt = attemptCnt;
	}

	public void setAvgSeconds(int avgSeconds) {
		this.avgSeconds = avgSeconds;
	}

	public void setFen(String fen) {
		this.fen = fen;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setMoveList(String moveList) {
		this.moveList = moveList;
	}

	public void setPassedCnt(int passedCnt) {
		this.passedCnt = passedCnt;
	}

	public void setRating(int rating) {
		this.rating = rating;
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

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}
