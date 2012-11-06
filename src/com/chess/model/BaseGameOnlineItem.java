package com.chess.model;

import android.os.Parcel;
import com.chess.backend.statics.StaticData;

/**
 * @author alien_roger
 * @created 31.07.12
 * @modified 31.07.12
 */
public abstract class BaseGameOnlineItem extends BaseGameItem{

	protected String opponentName;
	protected int opponentRating;
	protected int gameType;
	protected String lastMoveFromSquare;
	protected String lastMoveToSquare;
	protected boolean isMyTurn;
	protected boolean hasMessage;

	protected BaseGameOnlineItem() {
		opponentName = StaticData.SYMBOL_EMPTY;
		opponentRating = 0;
		gameType = 1;
		lastMoveFromSquare = StaticData.SYMBOL_EMPTY;
		lastMoveToSquare = StaticData.SYMBOL_EMPTY;
	}

	protected void writeBaseGameOnlineParcel(Parcel parcel) {
		parcel.writeString(opponentName);
		parcel.writeInt(opponentRating);
		parcel.writeInt(gameType);
		parcel.writeString(lastMoveFromSquare);
		parcel.writeString(lastMoveToSquare);
		parcel.writeBooleanArray(new boolean[]{isMyTurn, hasMessage});
	}

	protected void readBaseGameOnlineParcel(Parcel in) {
		opponentName = in.readString();
		opponentRating = in.readInt();
		gameType = in.readInt();
		lastMoveFromSquare = in.readString();
		lastMoveToSquare = in.readString();
		boolean[] booleans = new boolean[2];
		in.readBooleanArray(booleans);
		isMyTurn = booleans[0];
		hasMessage = booleans[1];
	}

	public void setOpponentName(String opponentName) {
		this.opponentName = opponentName;
	}

	public void setOpponentRating(int opponentRating) {
		this.opponentRating = opponentRating;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public void setLastMoveFromSquare(String lastMoveFromSquare) {
		this.lastMoveFromSquare = lastMoveFromSquare;
	}

	public void setLastMoveToSquare(String lastMoveToSquare) {
		this.lastMoveToSquare = lastMoveToSquare;
	}
}
