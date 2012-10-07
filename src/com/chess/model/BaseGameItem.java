package com.chess.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * BaseGameItem class
 *
 * @author alien_roger
 * @created at: 31.07.12 6:59
 */
public abstract class BaseGameItem implements Parcelable {

	public static final String GAME_ID = "game_id";
	public static final String TIMESTAMP = "time_stamp";
	public static final String GAME_INFO_ITEM = "game_info_item";

	protected long gameId;
	protected String color;


	protected String whiteUsername;
	protected String blackUsername;
	protected String userNameStrLength;

	protected String timeRemainingAmount;
	protected String timeRemainingUnits;
	protected boolean isDrawOfferPending;
	protected boolean isOpponentOnline;
	protected String fenStrLength;
	protected String fen;
	protected long timestamp;
	protected String moveList;
	protected String whiteRating;
	protected String blackRating;
	protected String secondsRemain;
	protected boolean hasNewMessage;



	public String getBlackRating() {
		return blackRating;
	}

	public String getBlackUsername() {
		return blackUsername;
	}

	public String getColor() {
		return color;
	}

	public String getFen() {
		return fen;
	}

	public String getFenStrLength() {
		return fenStrLength;
	}

	public long getGameId() {
		return gameId;
	}

	public boolean isDrawOfferPending() {
		return isDrawOfferPending;
	}

	public boolean isOpponentOnline() {
		return isOpponentOnline;
	}

	public String getMoveList() {
		return moveList;
	}

	public String getSecondsRemain() {
		return secondsRemain;
	}

	public String getTimeRemainingAmount() {
		return timeRemainingAmount;
	}

	public String getTimeRemainingUnits() {
		return timeRemainingUnits;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getTimestampStr() {
		return String.valueOf(timestamp);
	}

	public String getUserNameStrLength() {
		return userNameStrLength;
	}

	public String getWhiteRating() {
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
	 * @param parcel
	 */
	protected void writeBaseGameParcel(Parcel parcel) {
		parcel.writeLong(gameId);
		parcel.writeString(color);

		parcel.writeString(whiteUsername);
		parcel.writeString(blackUsername);
		parcel.writeString(userNameStrLength);

		parcel.writeString(timeRemainingAmount);
		parcel.writeString(timeRemainingUnits);
		parcel.writeBooleanArray(new boolean[]{isDrawOfferPending, isOpponentOnline, hasNewMessage});
		parcel.writeString(fenStrLength);
		parcel.writeString(fen);
		parcel.writeLong(timestamp);
		parcel.writeString(moveList);
		parcel.writeString(whiteRating);
		parcel.writeString(blackRating);
		parcel.writeString(secondsRemain);
	}

	/**
	 * Fill values in abstract class
	 * @param in
	 */
	protected void readBaseGameParcel(Parcel in) {
		gameId = in.readLong();
		color = in.readString();

		whiteUsername = in.readString();
		blackUsername = in.readString();
		userNameStrLength = in.readString();

		timeRemainingAmount = in.readString();
		timeRemainingUnits = in.readString();
		in.readBooleanArray(new boolean[]{isDrawOfferPending, isOpponentOnline, hasNewMessage});
		fenStrLength = in.readString();
		fen = in.readString();
		timestamp = in.readLong();
		moveList = in.readString();
		whiteRating = in.readString();
		blackRating = in.readString();
		secondsRemain = in.readString();
	}

}
