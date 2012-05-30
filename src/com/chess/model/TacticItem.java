package com.chess.model;

import com.chess.backend.statics.AppConstants;

import java.util.HashMap;

public class TacticItem {

	public HashMap<String, String> values;

	public TacticItem(String[] values) {
		this.values = new HashMap<String, String>();
		this.values.put(AppConstants.ID, values[0]);
		this.values.put(AppConstants.FEN, values[1]);
		this.values.put(AppConstants.MOVE_LIST, values[2]);
		this.values.put(AppConstants.ATTEMPT_CNT, values[3]);
		this.values.put(AppConstants.PASSED_CNT, values[4]);
		this.values.put(AppConstants.RATING, values[5]);
		this.values.put(AppConstants.AVG_SECONDS, values[6]);
		this.values.put(AppConstants.STOP, "0");
	}
}
