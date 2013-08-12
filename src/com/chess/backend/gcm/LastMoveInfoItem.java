package com.chess.backend.gcm;

/**
 * LastMoveInfoItem class
 *
 * @author alien_roger
 * @created at: 29.09.12 18:34
 */
public class LastMoveInfoItem {
	private String lastMoveSan;
	private String gameId;

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getLastMoveSan() {
		return lastMoveSan;
	}

	public void setLastMoveSan(String lastMoveSan) {
		this.lastMoveSan = lastMoveSan;
	}
}
