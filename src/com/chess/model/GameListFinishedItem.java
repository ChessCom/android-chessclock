package com.chess.model;

import android.os.Parcel;
import android.os.Parcelable;

public class GameListFinishedItem extends BaseGameOnlineItem{

	private String gameResults;

	public GameListFinishedItem() {
	}

	public GameListFinishedItem(String[] values) {
		super(values);
		gameResults = values[14];
	}

	public void setGameResults(String gameResults) {
		this.gameResults = gameResults;
	}

	public String getGameResult() {
		return gameResults;
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
