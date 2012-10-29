package com.chess.model;

import com.chess.backend.statics.StaticData;

public class VideoItem {


	private String title;
	private String description;
	private String prefVideoCategory;
	private String skillLevel;
	private String opening;
	private String authorUsername;
	private String authorChessTitle;
	private String authorFirstGame;
	private String authorLastName;
	private String minutes;
	private long publishTimestamp;
	private String viewUrl;

	public VideoItem(String[] values) {
		title = values[0];
		description = values[1].replaceAll("&quot;", "\"");
		prefVideoCategory = values[2];
		skillLevel = values[3];
		opening = values[4];
		authorUsername = values[5];
		authorChessTitle = values[6];
		authorFirstGame = values[7];
		authorLastName = values[8];
		minutes = values[9];
		publishTimestamp = Long.parseLong(values[10]);
		viewUrl = values[11].replaceAll("<--->", StaticData.SYMBOL_EMPTY);
	}

	public String getAuthorChessTitle() {
		return authorChessTitle;
	}

	public String getAuthorFirstGame() {
		return authorFirstGame;
	}

	public String getAuthorLastName() {
		return authorLastName;
	}

	public String getAuthorUsername() {
		return authorUsername;
	}

	public String getDescription() {
		return description;
	}

	public String getMinutes() {
		return minutes;
	}

	public String getOpening() {
		return opening;
	}

	public String getPrefVideoCategory() {
		return prefVideoCategory;
	}

	public long getPublishTimestamp() {
		return publishTimestamp;
	}

	public String getSkillLevel() {
		return skillLevel;
	}

	public String getTitle() {
		return title;
	}

	public String getViewUrl() {
		return viewUrl;
	}
}
