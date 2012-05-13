package com.chess.model;

import com.chess.backend.statics.AppConstants;

import java.util.HashMap;

public class TacticResultItem {
	public HashMap<String, String> values;

	public TacticResultItem(String[] values) {
		this.values = new HashMap<String, String>();
		this.values.put(AppConstants.SCORE, values[0]);
		this.values.put(AppConstants.USER_RATING_CHANGE, values[1]);
		this.values.put(AppConstants.USER_RATING, values[2]);
		this.values.put(AppConstants.PROBLEM_RATING_CHANGE, values[3]);
		this.values.put(AppConstants.PROBLEM_RATING, values[4]);
	}
}
