package com.chess.model;

/**
 * BaseGameItem class
 *
 * @author alien_roger
 * @created at: 31.07.12 6:59
 */
public class BaseGameItem {

	private String gameId;
	private String color;
	private String gameType;
	private String userNameStrLength;
	private String opponentName;
	private String opponentRating;
	private String timeRemainingAmount;
	private String timeRemainingUnits;
	private String fenStrLength;
	private String fen;
	private String timestamp;
	private String lastMoveFrom;
	private String lastMoveTo;
	private boolean isDrawOfferPending;
	private boolean isOpponentOnline;
	private boolean isMyTurn;
	private boolean hasNewMessage;

}
