package com.chess.model;

/**
 * @author Alexey Schekin (schekin@azoft.com)
 * @created 31.07.12
 * @modified 31.07.12
 */
public abstract class BaseGameOnlineItem extends BaseGameItem{

	private static final long serialVersionUID = 3082794382685984825L;

	protected String opponentName;
	protected String opponentRating;
	protected String gameType;
	protected String lastMoveFromSquare;
	protected String lastMoveToSquare;
	protected boolean isMyTurn;
	protected boolean hasMessage;

}
