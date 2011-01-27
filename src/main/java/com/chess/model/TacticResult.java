package com.chess.model;

import java.util.HashMap;

public class TacticResult {
	public HashMap<String, String> values;
	public TacticResult(String[] values){
		this.values = new HashMap<String, String>();
		this.values.put("score", values[0]);
		this.values.put("user_rating_change", values[1]);
		this.values.put("user_rating", values[2]);
		this.values.put("problem_rating_change", values[3]);
		this.values.put("problem_rating", values[4]);
	}
}
