package com.chess.model;

import android.os.Parcel;
import android.os.Parcelable;

public class GameListFinishedItem extends BaseGameOnlineItem{


	private String gameResults;

	public GameListFinishedItem() {
	}

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
//		fen = values[9];
		timestamp = Long.parseLong(values[10]);
		lastMoveFromSquare = values[11];
		lastMoveToSquare = values[12];
		isOpponentOnline = values[13].equals("1");
		gameResults = values[14];
	}

	public String getFenStringLength() {
		return fenStrLength;
	}

	public void setGameResults(String gameResults) {
		this.gameResults = gameResults;
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

	public String getUsernameStringLength() {
		return userNameStrLength;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		writeBaseGameParcel(parcel);

		writeBaseGameOnlineParcel(parcel);
		parcel.writeString(gameResults);
	}



	public static final Parcelable.Creator<GameListFinishedItem> CREATOR = new Parcelable.Creator<GameListFinishedItem>() {
		public GameListFinishedItem createFromParcel(Parcel in) {
			return new GameListFinishedItem(in);
		}

		public GameListFinishedItem[] newArray(int size) {
			return new GameListFinishedItem[size];
		}
	};

	private GameListFinishedItem(Parcel in) {
		readBaseGameParcel(in);

		readBaseGameOnlineParcel(in);
		gameResults = in.readString();
	}



}
