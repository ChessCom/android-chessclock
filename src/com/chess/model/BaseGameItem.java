package com.chess.model;

import java.io.Serializable;

/**
 * BaseGameItem class
 *
 * @author alien_roger
 * @created at: 31.07.12 6:59
 */
public abstract class BaseGameItem implements Serializable {

	public static final String GAME_ID = "game_id";
	public static final String TIMESTAMP = "time_stamp";
	public static final String GAME_INFO_ITEM = "game_info_item";
	private static final long serialVersionUID = -752315798923143602L;

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
}
