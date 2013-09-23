package com.chess.model;

import android.os.Parcel;
import android.os.Parcelable;

public class GameAnalysisItem implements Parcelable {
	private String fen;
	private String opponent;
	private int gameType;
	private int userColor;
	private String movesList;

	public GameAnalysisItem() { }

	public String getFen() {
		return fen;
	}

	public void setFen(String fen) {
		this.fen = fen;
	}

	public String getOpponent() {
		return opponent;
	}

	public void setOpponent(String opponent) {
		this.opponent = opponent;
	}

	public int getGameType() {
		return gameType;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public int getUserColor() {
		return userColor;
	}

	public void setUserColor(int userColor) {
		this.userColor = userColor;
	}

	public String getMovesList() {
		return movesList;
	}

	public void setMovesList(String movesList) {
		this.movesList = movesList;
	}

	protected GameAnalysisItem(Parcel in) {
		fen = in.readString();
		opponent = in.readString();
		gameType = in.readInt();
		userColor = in.readInt();
		movesList = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(fen);
		dest.writeString(opponent);
		dest.writeInt(gameType);
		dest.writeInt(userColor);
		dest.writeString(movesList);

	}

	public static final Creator<com.chess.model.GameAnalysisItem> CREATOR = new Creator<com.chess.model.GameAnalysisItem>() {
		@Override
		public com.chess.model.GameAnalysisItem createFromParcel(Parcel in) {
			return new com.chess.model.GameAnalysisItem(in);
		}

		@Override
		public com.chess.model.GameAnalysisItem[] newArray(int size) {
			return new com.chess.model.GameAnalysisItem[size];
		}
	};
}