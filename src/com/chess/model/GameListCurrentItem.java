package com.chess.model;

public class GameListCurrentItem extends BaseGameOnlineItem {

	private static final long serialVersionUID = -7231705372370548056L;

	public GameListCurrentItem(String[] values) {

		gameId = Long.parseLong(values[0]);
		color = values[1];
		gameType =  values[2];
		userNameStrLength = values[3];
		opponentName = values[4];
		opponentRating = values[5];
		timeRemainingAmount = values[6];
		timeRemainingUnits = values[7];
		fenStrLength = values[8];
		fen = values[9];
		timestamp = Long.parseLong(values[10]);
		lastMoveFromSquare =  values[11];
		lastMoveToSquare = values[12];
		isDrawOfferPending = values[13].equals("p");
		isOpponentOnline = values[14].equals("1");
		isMyTurn = values[15].equals("1");
		hasMessage = values[16].equals("1");

	}

	public String getGameType() {
		return gameType;
	}

	public String getUsernameStringLength() {
		return userNameStrLength;
	}

	public String getOpponentUsername() {
		return opponentName;
	}

	public String getOpponentRating() {
		return opponentRating;
	}

	public String getFenStringLength() {
		return fenStrLength;
	}

	public String getLastMoveFromSquare() {
		return lastMoveFromSquare;
	}

	public String getLastMoveToSquare() {
		return lastMoveToSquare;
	}

	public boolean getIsDrawOfferPending() {
		return isDrawOfferPending;
	}

	public boolean getIsOpponentOnline() {
		return isOpponentOnline;
	}

	public boolean getHasNewMessage() {
		return hasMessage;
	}

	public boolean getIsMyTurn() {
		return isMyTurn;
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
