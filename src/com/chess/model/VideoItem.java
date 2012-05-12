package com.chess.model;

import com.chess.backend.statics.StaticData;
import com.chess.ui.core.AppConstants;

import java.util.HashMap;

public class VideoItem {

	public HashMap<String, String> values;

	public VideoItem(String[] values) {
		this.values = new HashMap<String, String>();
		this.values.put(AppConstants.TITLE, values[0]);
		this.values.put(AppConstants.DESCRIPTION, values[1].replaceAll("&quot;", "\""));
		this.values.put(AppConstants.VIDEO_CATEGORY, values[2]);
		this.values.put(AppConstants.SKILL_LEVEL, values[3]);
		this.values.put(AppConstants.OPENING, values[4]);
		this.values.put(AppConstants.AUTHOR_USERNAME, values[5]);
		this.values.put(AppConstants.AUTHOR_CHESS_TITLE, values[6]);
		this.values.put(AppConstants.AUTHOR_FIRST_GAME, values[7]);
		this.values.put(AppConstants.AUTHOR_LAST_NAME, values[8]);
		this.values.put(AppConstants.MINUTES, values[9]);
		this.values.put(AppConstants.PUBLISH_TIMESTAMP, values[10]);
		this.values.put(AppConstants.VIEW_URL, values[11].replaceAll("<--->", StaticData.SYMBOL_EMPTY));
	}
}
