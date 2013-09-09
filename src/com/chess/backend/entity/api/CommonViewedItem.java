package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.07.13
 * Time: 6:32
 */
public class CommonViewedItem {

	private long id;
	private String username;
	private boolean viewed;

	public CommonViewedItem(long id, String username) {
		this.id = id;
		this.username = username;
		this.viewed = true; // created item is always viewed by default
	}

	public long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public boolean isViewed() {
		return viewed;
	}
}
