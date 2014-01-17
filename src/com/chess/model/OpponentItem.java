package com.chess.model;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 17.01.14
 * Time: 16:49
 */
public class OpponentItem {

	private String name;
	private String avatarUrl;

	public OpponentItem(String name, String avatarUrl) {
		this.name = name;
		this.avatarUrl = avatarUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
}
