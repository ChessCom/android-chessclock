package com.chess.model;

import android.os.Parcel;
import com.chess.statics.Symbol;

/**
 * @author alien_roger
 * @created 31.07.12
 * @modified 31.07.12
 */
public abstract class BaseGameOnlineItem extends BaseGameItem {

	protected String opponentName;
	protected int opponentRating;
	protected int gameType;
	protected String lastMoveFromSquare;
	protected String lastMoveToSquare;
	protected boolean isMyTurn;

	protected BaseGameOnlineItem() {
		opponentName = Symbol.EMPTY;
		opponentRating = 0;
		gameType = 1;
		lastMoveFromSquare = Symbol.EMPTY;
		lastMoveToSquare = Symbol.EMPTY;
	}

	protected BaseGameOnlineItem(String[] values) {
		gameId = Long.parseLong(values[0]);
		color = Integer.parseInt(values[1]);
		gameType = Integer.parseInt(values[2]);
		userNameStrLength = Integer.parseInt(values[3]);
		opponentName = values[4];
		opponentRating = Integer.parseInt(values[5]);
		timeRemainingAmount = Integer.parseInt(values[6]);
		timeRemainingUnits = values[7];
		fenStrLength = Integer.parseInt(values[8]);
//		fen = values[9];
		timestamp = Long.parseLong(values[10]);
		lastMoveFromSquare = values[11];
		lastMoveToSquare = values[12];
		isDrawOfferPending = values[13].equals("p");
		isOpponentOnline = values[14].equals("1");
	}

	protected void writeBaseGameOnlineParcel(Parcel out) {
		out.writeString(opponentName);
		out.writeInt(opponentRating);
		out.writeInt(gameType);
		out.writeString(lastMoveFromSquare);
		out.writeString(lastMoveToSquare);
		out.writeByte((byte) (isMyTurn ? 1 : 0));
		out.writeByte((byte) (hasNewMessage ? 1 : 0));
	}

	protected void readBaseGameOnlineParcel(Parcel in) {
		opponentName = in.readString();
		opponentRating = in.readInt();
		gameType = in.readInt();
		lastMoveFromSquare = in.readString();
		lastMoveToSquare = in.readString();
		isMyTurn = in.readByte() == 1;
		hasNewMessage = in.readByte() == 1;
	}

	public void setOpponentName(String opponentName) {
		this.opponentName = opponentName;
	}

	public String getOpponentUsername() {
		return opponentName;
	}

	public void setOpponentRating(int opponentRating) {
		this.opponentRating = opponentRating;
	}

	public int getOpponentRating() {
		return opponentRating;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public int getGameType() {
		return gameType;
	}

	public void setLastMoveFromSquare(String lastMoveFromSquare) {
		this.lastMoveFromSquare = lastMoveFromSquare;
	}

	public void setLastMoveToSquare(String lastMoveToSquare) {
		this.lastMoveToSquare = lastMoveToSquare;
	}


	public int getUsernameStringLength() {
		return userNameStrLength;
	}

	public int getFenStringLength() {
		return fenStrLength;
	}

	public String getLastMoveFromSquare() {
		return lastMoveFromSquare;
	}

	public String getLastMoveToSquare() {
		return lastMoveToSquare;
	}

	public boolean isDrawOfferPending() {
		return isDrawOfferPending;
	}

	public boolean getIsOpponentOnline() {
		return isOpponentOnline;
	}

	public boolean isMyTurn() {
		return isMyTurn;
	}

	public void setMyTurn(boolean myTurn) {
		isMyTurn = myTurn;
	}
}
