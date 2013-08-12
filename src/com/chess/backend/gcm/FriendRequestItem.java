package com.chess.backend.gcm;

import com.chess.backend.statics.StaticData;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.08.13
 * Time: 5:53
 */
public class FriendRequestItem {
/*
        'sender' => $params['sender'],
        'message' => $params['message'],
        'created_at' => $params['createdAt'],
*/
	private String username;
	private String message;
	private long createdAt;
	private String avatar;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	public String getAvatar() {
		return avatar == null ? StaticData.SYMBOL_EMPTY : avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
}
