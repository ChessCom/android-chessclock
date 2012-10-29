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
	protected String opponentRating;
	protected String gameType;
	protected String lastMoveFromSquare;
	protected String lastMoveToSquare;
	protected boolean isMyTurn;
	protected boolean hasMessage;

	protected BaseGameOnlineItem() {
		opponentName = StaticData.SYMBOL_EMPTY;
		opponentRating = StaticData.SYMBOL_EMPTY;
		gameType = StaticData.SYMBOL_EMPTY;
		lastMoveFromSquare = StaticData.SYMBOL_EMPTY;
		lastMoveToSquare = StaticData.SYMBOL_EMPTY;
	}

	protected void writeBaseGameOnlineParcel(Parcel parcel) {
		parcel.writeString(opponentName);
		parcel.writeString(opponentRating);
		parcel.writeString(gameType);
		parcel.writeString(lastMoveFromSquare);
		parcel.writeString(lastMoveToSquare);
		parcel.writeBooleanArray(new boolean[]{isMyTurn, hasMessage});
	}

	protected void readBaseGameOnlineParcel(Parcel in) {
		opponentName = in.readString();
		opponentRating = in.readString();
		gameType = in.readString();
		lastMoveFromSquare = in.readString();
		lastMoveToSquare = in.readString();
		boolean[] booleans = new boolean[2];
		in.readBooleanArray(booleans);
		isMyTurn = booleans[0];
		hasMessage = booleans[1];
	}

}
