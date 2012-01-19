/*
 * GameEvent.java
 */

package com.chess.lcc.android;

public class GameEvent {
	public enum Event {Move, DrawOffer, EndOfGame}

	;
	Event event;
	public Long gameId;
	public int moveIndex;
	public String drawOffererUsername;
	public String gameEndedMessage;
}
