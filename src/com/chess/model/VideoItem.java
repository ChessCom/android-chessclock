package com.chess.model;

import com.chess.core.AppConstants;

import java.util.HashMap;

public class VideoItem {

	public HashMap<String, String> values;

	public VideoItem(String[] values) {
		this.values = new HashMap<String, String>();
		this.values.put(AppConstants.TITLE, values[0]);
		this.values.put("description", values[1].replaceAll("&quot;", "\""));
		this.values.put(AppConstants.VIDEO_CATEGORY, values[2]);
		this.values.put("skill_level", values[3]);
		this.values.put("opening", values[4]);
		this.values.put("author_username", values[5]);
		this.values.put("author_chess_title", values[6]);
		this.values.put("author_first_name", values[7]);
		this.values.put("author_last_name", values[8]);
		this.values.put("minutes", values[9]);
		this.values.put("publish_timestamp", values[10]);
		this.values.put("view_url", values[11].replaceAll("<--->", ""));
	}
}
