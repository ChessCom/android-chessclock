package com.chess.model;

import java.io.Serializable;

/**
 * BaseGameItem class
 *
 * @author alien_roger
 * @created at: 31.07.12 6:59
 */
public abstract class BaseGameItem implements Serializable {

	protected long gameId;
	protected String color;


	protected String whiteUserName;
	protected String blackUserName;
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


}
