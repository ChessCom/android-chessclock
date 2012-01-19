package com.chess.model;

import java.util.HashMap;

public class Tactic {

	public HashMap<String, String> values;

	public Tactic(String[] values) {
		this.values = new HashMap<String, String>();
		this.values.put("id", values[0]);
		this.values.put("fen", values[1]);
		this.values.put("move_list", values[2]);
		this.values.put("attempt_count", values[3]);
		this.values.put("passed_count", values[4]);
		this.values.put("rating", values[5]);
		this.values.put("average_seconds", values[6]);
		this.values.put("stop", "0");
	}
}
