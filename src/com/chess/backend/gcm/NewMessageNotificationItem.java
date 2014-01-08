package com.chess.backend.gcm;

import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.01.14
 * Time: 22:26
 */
public class NewMessageNotificationItem {
/*
	"message" -> "content "
	"avatar_url" -> "//d1lalstwiwz2br.cloudfront.net/images_users/avatars/anotherRoger_origin.1.png"
	"collapse_key" -> "do_not_collapse"
	"owner" -> "alien_roger"
	"created_at" -> "1389194745"
	"sender_user_id" -> "7264281"
	"from" -> "27129061667"
	"type" -> "NOTIFICATION_NEW_MESSAGE"
	"sender_username" -> "anotherRoger"
*/

	private String message;
	private String avatar_url;
	private String owner;
	private long createdAt;
	private long senderUserId;
	private String from;
	private String type;
	private String senderUsername;

	public NewMessageNotificationItem() {
	}

	public NewMessageNotificationItem(Intent intent) {
		message = intent.getStringExtra("message");
		avatar_url = intent.getStringExtra("avatar_url");
		owner = intent.getStringExtra("owner");
		createdAt = Long.parseLong(intent.getStringExtra("created_at"));
		senderUserId = Long.parseLong(intent.getStringExtra("sender_user_id"));
		from = intent.getStringExtra("from");
		type = intent.getStringExtra("type");
		senderUsername = intent.getStringExtra("sender_username");

	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getAvatarUrl() {
		return avatar_url;
	}

	public void setAvatar_url(String avatar_url) {
		this.avatar_url = avatar_url;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	public long getSenderUserId() {
		return senderUserId;
	}

	public void setSenderUserId(long senderUserId) {
		this.senderUserId = senderUserId;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSenderUsername() {
		return senderUsername;
	}

	public void setSenderUsername(String senderUsername) {
		this.senderUsername = senderUsername;
	}
}
