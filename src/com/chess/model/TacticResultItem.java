package com.chess.model;

public class TacticResultItem {
	private String score;
	private String userRatingChange;
	private String userRating;
	private String problemRatingChange;
	private String problemRating;

//	public HashMap<String, String> values;

	public TacticResultItem(String[] values) {

		score = values[0];
		userRatingChange = values[1];
		userRating = values[2];
		problemRatingChange = values[3];
		problemRating = values[4];
	}

	public String getScore() {
		return score;
	}

	public String getUserRatingChange() {
		return userRatingChange;
	}

	public String getUserRating() {
		return userRating;
	}

	public String getProblemRatingChange() {
		return problemRatingChange;
	}

	public String getProblemRating() {
		return problemRating;
	}
}
