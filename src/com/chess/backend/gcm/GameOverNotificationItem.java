package com.chess.backend.gcm;

import com.chess.backend.statics.StaticData;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.08.13
 * Time: 5:39
 */
public class GameOverNotificationItem  {
/*
        'game_id' => $params['gameId'],
        'message' => $params['message'],
*/
	private long gameId;
	private String message;
	private String avatar_url;

	public long getGameId() {
		return gameId;
	}

	public void setGameId(long gameId) {
		this.gameId = gameId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getAvatar() {
		return avatar_url == null ? StaticData.SYMBOL_EMPTY : avatar_url;
	}

	public void setAvatar(String avatar) {
		this.avatar_url = avatar;
	}
}
