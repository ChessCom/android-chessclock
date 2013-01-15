/*
 * GameEvent.java
 */

package com.chess.lcc.android;

public class LiveGameEvent {
	public enum Event {MOVE, DRAW_OFFER, END_OF_GAME}

	private Event event;
	private Long gameId;
	//private int moveIndex;
	private String drawOffererUsername;
	private String gameEndedMessage;

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public String getDrawOffererUsername() {
		return drawOffererUsername;
	}

	public void setDrawOffererUsername(String drawOffererUsername) {
		this.drawOffererUsername = drawOffererUsername;
	}

	public String getGameEndedMessage() {
		return gameEndedMessage;
	}

	public void setGameEndedMessage(String gameEndedMessage) {
		this.gameEndedMessage = gameEndedMessage;
	}

	/*public int getMoveIndex() {
		return moveIndex;
	}

	public void setMoveIndex(int moveIndex) {
		this.moveIndex = moveIndex;
	}*/

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}
}