package com.chess.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.12.13
 * Time: 4:53
 */
public class GameExplorerItem implements Parcelable {

	private String fen;
	private String movesList;
	private int gameType;

	public GameExplorerItem() {}

	public String getFen() {
		return fen;
	}

	public void setFen(String fen) {
		this.fen = fen;
	}

	public String getMovesList() {
		return movesList;
	}

	public void setMovesList(String movesList) {
		this.movesList = movesList;
	}

	public int getGameType() {
		return gameType;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public GameExplorerItem(Parcel in) {
		fen = in.readString();
		movesList = in.readString();
		gameType = in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(fen);
		dest.writeString(movesList);
		dest.writeInt(gameType);
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<GameExplorerItem> CREATOR = new Parcelable.Creator<GameExplorerItem>() {
		@Override
		public GameExplorerItem createFromParcel(Parcel in) {
			return new GameExplorerItem(in);
		}

		@Override
		public GameExplorerItem[] newArray(int size) {
			return new GameExplorerItem[size];
		}
	};
}
