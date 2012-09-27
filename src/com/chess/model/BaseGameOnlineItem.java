package com.chess.model;

import android.os.Parcel;

/**
 * @author Alexey Schekin (schekin@azoft.com)
 * @created 31.07.12
 * @modified 31.07.12
 */
public abstract class BaseGameOnlineItem extends BaseGameItem{

//	private static final long serialVersionUID = 3082794382685984825L;

	protected String opponentName;
	protected String opponentRating;
	protected String gameType;
	protected String lastMoveFromSquare;
	protected String lastMoveToSquare;
	protected boolean isMyTurn;
	protected boolean hasMessage;


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
		in.readBooleanArray(new boolean[]{isMyTurn, hasMessage});
	}

}
