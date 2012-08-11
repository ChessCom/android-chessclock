package com.chess.model;

public class GameListFinishedItem extends BaseGameOnlineItem{

	private String gameResults;

	public GameListFinishedItem(String[] values) {

		gameId = Long.parseLong(values[0]);
		color = values[1];
		gameType = values[2];
		userNameStrLength = values[3];
		opponentName = values[4];
		opponentRating = values[5];
		timeRemainingAmount = values[6];
		timeRemainingUnits = values[7];
		fenStrLength = values[8];
		fen = values[9];
		timestamp = Long.parseLong(values[10]);
		lastMoveFromSquare = values[11];
		lastMoveToSquare = values[12];
		isOpponentOnline = values[13].equals("1");
		gameResults = values[14];

	}

	public long getGameId(){
		return gameId;
	}

	public String getColor() {
		return color;
	}

	public String getFen() {
		return fen;
	}

	public String getFenStringLength() {
		return fenStrLength;
	}

	public String getGameResult() {
		return gameResults;
	}

	public String getGameType() {
		return gameType;
	}

	public boolean getIsOpponentOnline() {
		return isOpponentOnline;
	}

	public String getLastMoveFromSquare() {
		return lastMoveFromSquare;
	}

	public String getLastMoveToSquare() {
		return lastMoveToSquare;
	}

	public String getOpponentRating() {
		return opponentRating;
	}

	public String getOpponentUsername() {
		return opponentName;
	}

	public String getTimeRemainingAmount() {
		return timeRemainingAmount;
	}

	public String getTimeRemainingUnits() {
		return timeRemainingUnits;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getUsernameStringLength() {
		return userNameStrLength;
	}

//	@Override
//	public String toString() {
//		StringBuilder builder = new StringBuilder();
//		for (String string : values.keySet()) {
//			builder.append(" key = ").append(string).append(" value = ").append(values.get(string)).append("\n");
//		}
//		return builder.toString();
//	}



}
