package com.chess.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.chess.backend.statics.StaticData;

/**
 * BaseGameItem class
 *
 * @author alien_roger
 * @created at: 31.07.12 6:59
 */
public abstract class BaseGameItem implements Parcelable {

	public static final int CLASSIC_CHESS = 1;
	public static final int CHESS_960 = 2;
	public static final int GAME_WON = 1;
	public static final int GAME_LOSS = 0;
	public static final int GAME_DRAW = 2;

	public static final String FIRST_MOVE_INDEX = "1.";

	public static final String GAME_ID = "game_id";
	public static final String TIMESTAMP = "time_stamp";
	// TODO use gameId to pass info about game to load
//	public static final String GAME_INFO_ITEM = "game_info_item";

	protected long gameId;
	protected int color;


	protected String whiteUsername;
	protected String blackUsername;
	protected int userNameStrLength;

	protected int timeRemainingAmount;
	protected String timeRemainingUnits;
	protected boolean isDrawOfferPending;
	protected boolean isOpponentOnline;
	protected int fenStrLength;
//	protected String fen;
	protected long timestamp;
	protected String moveList;
	protected int whiteRating;
	protected int blackRating;
	protected long secondsRemain;
	protected boolean hasNewMessage;

	protected BaseGameItem() {
		color = 0;
		whiteUsername = StaticData.SYMBOL_EMPTY;
		blackUsername = StaticData.SYMBOL_EMPTY;
		userNameStrLength = 0;
		timeRemainingAmount = 0;
		timeRemainingUnits = StaticData.SYMBOL_EMPTY;
		fenStrLength = 0;
		moveList = StaticData.SYMBOL_EMPTY;
		whiteRating = 0;
		blackRating = 0;
		secondsRemain = 0;
	}

	public int getBlackRating() {
		return blackRating;
	}

	public String getBlackUsername() {
		return blackUsername;
	}

	public int getColor() {
		return color;
	}

	/*public String getFen() {
		return fen;
	}

	public String getFenStrLength() {
		return fenStrLength;
	}*/

	public long getGameId() {
		return gameId;
	}

	/*public boolean isDrawOffered() {
		return isDrawOffered;
	}

	public boolean isOpponentOnline() {
		return isOpponentOnline;
	}*/


	public void setGameId(long gameId) {
		this.gameId = gameId;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public void setWhiteUsername(String whiteUsername) {
		this.whiteUsername = whiteUsername;
	}

	public void setBlackUsername(String blackUsername) {
		this.blackUsername = blackUsername;
	}

	public void setUserNameStrLength(int userNameStrLength) {
		this.userNameStrLength = userNameStrLength;
	}

	public void setTimeRemainingAmount(int timeRemainingAmount) {
		this.timeRemainingAmount = timeRemainingAmount;
	}

	public void setTimeRemainingUnits(String timeRemainingUnits) {
		this.timeRemainingUnits = timeRemainingUnits;
	}

	public void setDrawOfferPending(boolean drawOfferPending) {
		isDrawOfferPending = drawOfferPending;
	}

	public void setOpponentOnline(boolean opponentOnline) {
		isOpponentOnline = opponentOnline;
	}

	public void setFenStrLength(int fenStrLength) {
		this.fenStrLength = fenStrLength;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setMoveList(String moveList) {
		this.moveList = moveList;
	}

	public void setWhiteRating(int whiteRating) {
		this.whiteRating = whiteRating;
	}

	public void setBlackRating(int blackRating) {
		this.blackRating = blackRating;
	}

	public void setSecondsRemain(long secondsRemain) {
		this.secondsRemain = secondsRemain;
	}

	public String getMoveList() {
		return moveList;
	}

	public long getSecondsRemain() {
		return secondsRemain;
	}

	public int getTimeRemainingAmount() {
		return timeRemainingAmount;
	}

	public String getTimeRemainingUnits() {
		return timeRemainingUnits;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getUserNameStrLength() {
		return userNameStrLength;
	}

	public int getWhiteRating() {
		return whiteRating;
	}

	public String getWhiteUsername() {
		return whiteUsername;
	}

	public boolean hasNewMessage() {
		return hasNewMessage;
	}

	public void setHasNewMessage(boolean hasNewMessage) {
		this.hasNewMessage = hasNewMessage;
	}


	@Override
	public int describeContents() {
		return hashCode();
	}

	/**
	 * Write BaseGameItem fields to parcel
	 * @param out
	 */
	protected void writeBaseGameParcel(Parcel out) {
		out.writeLong(gameId);
		out.writeInt(color);

		out.writeString(whiteUsername);
		out.writeString(blackUsername);
		out.writeInt(userNameStrLength);

		out.writeInt(timeRemainingAmount);
		out.writeString(timeRemainingUnits);

        out.writeByte((byte) (isDrawOfferPending ? 1 : 0));
        out.writeByte((byte) (isOpponentOnline ? 1 : 0));
        out.writeByte((byte) (hasNewMessage ? 1 : 0));

		out.writeInt(fenStrLength);
//		parcel.writeString(fen);
		out.writeLong(timestamp);
		out.writeString(moveList);
		out.writeInt(whiteRating);
		out.writeInt(blackRating);
		out.writeLong(secondsRemain);
	}

	/**
	 * Fill values in abstract class
	 * @param in parcelable as input
	 */
	protected void readBaseGameParcel(Parcel in) {
		gameId = in.readLong();
		color = in.readInt();

		whiteUsername = in.readString();
		blackUsername = in.readString();
		userNameStrLength = in.readInt();

		timeRemainingAmount = in.readInt();
		timeRemainingUnits = in.readString();

        isDrawOfferPending = in.readByte() == 1;
        isOpponentOnline = in.readByte() == 1;
        hasNewMessage = in.readByte() == 1;

		fenStrLength = in.readInt();
//		fen = in.readString();
		timestamp = in.readLong();
		moveList = in.readString();
		whiteRating = in.readInt();
		blackRating = in.readInt();
		secondsRemain = in.readLong();
	}

}
