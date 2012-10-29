package com.chess.model;

import com.chess.backend.statics.StaticData;

public class TacticResultItem {
	private String score;
	private String userRatingChange;
	private String userRating;
	private String problemRatingChange;
	private String problemRating;

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

	public String getSaveString(){
//		6.6:
//		-65
//		:1273
//		:78
//		:1385

		StringBuilder builder = new StringBuilder();
		return builder.append(score).append(StaticData.SYMBOL_COLON)
				.append(userRatingChange).append(StaticData.SYMBOL_COLON)
				.append(userRating).append(StaticData.SYMBOL_COLON)
				.append(problemRatingChange).append(StaticData.SYMBOL_COLON)
				.append(problemRating).append(StaticData.SYMBOL_COLON).toString();
	}
}
