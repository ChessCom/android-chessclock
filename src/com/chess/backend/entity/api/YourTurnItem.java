package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.12.13
 * Time: 22:13
 */
public class YourTurnItem {

	private String lastMove;
	private String username;
	private String opponent;
	private long gameId;

	public YourTurnItem(String lastMove, String username, long gameId) {
		this.lastMove = lastMove;
		this.username = username;
		this.gameId = gameId;
	}

	public String getOpponent() {
		return BaseResponseItem.getSafeValue(opponent);
	}

	public void setOpponent(String opponent) {
		this.opponent = opponent;
	}

	public String getLastMove() {
		return BaseResponseItem.getSafeValue(lastMove);
	}

	public void setLastMove(String lastMove) {
		this.lastMove = lastMove;
	}

	public String getUsername() {
		return BaseResponseItem.getSafeValue(username);
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public long getGameId() {
		return gameId;
	}

	public void setGameId(long gameId) {
		this.gameId = gameId;
	}
}
