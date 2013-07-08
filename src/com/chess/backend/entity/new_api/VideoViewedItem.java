package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.07.13
 * Time: 6:32
 */
public class VideoViewedItem {

	private long video_id;
	private String username;
	private boolean viewed;

	public VideoViewedItem(long video_id, String username, boolean viewed) {
		this.video_id = video_id;
		this.username = username;
		this.viewed = viewed;
	}

	public long getVideoId() {
		return video_id;
	}

	public String getUsername() {
		return username;
	}

	public boolean isViewed() {
		return viewed;
	}
}
