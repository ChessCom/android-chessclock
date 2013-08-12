package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.12.12
 * Time: 8:40
 */
public class ChatItem {
	private boolean is_mine;
	private String content;
	private long timestamp;
	private String avatar;

	public boolean isMine() {
		return is_mine;
	}

	public void setIsMine(boolean is_mine) {
		this.is_mine = is_mine;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
}
