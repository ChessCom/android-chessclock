package com.chess.model;

/**
 * GamePlayingItem class
 *
 * @author alien_roger
 * @created at: 27.09.12 8:19
 */
public class GamePlayingItem {
	private long gameId;
	private boolean isBoardOpen;

	public long getGameId() {
		return gameId;
	}

	public void setGameId(long gameId) {
		this.gameId = gameId;
	}

	public boolean isBoardOpen() {
		return isBoardOpen;
	}

	public void setBoardOpen(boolean boardOpen) {
		isBoardOpen = boardOpen;
	}
}
