package com.chess.backend.gcm;

import com.chess.backend.entity.api.BaseResponseItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.08.13
 * Time: 5:39
 */
public class GameOverNotificationItem {
	/*
		"message" -> "Black wins by checkmate!"
		"username" -> "anotherRoger"
		"avatar_url" -> "//d1lalstwiwz2br.cloudfront.net/images_users/avatars/anotherRoger_origin.1.png"
		"collapse_key" -> "do_not_collapse"
		"game_id" -> "86221626"
		"owner" -> "alien_roger"
		"from" -> "27129061667"
		"type" -> "NOTIFICATION_GAME_OVER"
	*/
	private String username;
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
		return BaseResponseItem.getSafeValue(avatar_url);
	}

	public void setAvatar(String avatar) {
		this.avatar_url = avatar;
	}

	public String getUsername() {
		return BaseResponseItem.getSafeValue(username);
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
