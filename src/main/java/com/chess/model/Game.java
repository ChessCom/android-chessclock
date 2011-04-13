package com.chess.model;

import java.io.Serializable;
import java.util.HashMap;

public class Game implements Serializable {

  public static int GAME_DATA_ELEMENTS_COUNT = 14;

  public HashMap<String, String> values;

	public Game(String[] values, boolean isLiveChess){
		this.values = new HashMap<String, String>();
    final String gameId = isLiveChess ? values[0] : values[0].split("[+]")[1];
		this.values.put("game_id", gameId);
		this.values.put("game_type", values[1]);
		this.values.put("timestamp", values[2]);
		this.values.put("game_name", values[3]);
		this.values.put("white_username", values[4]);
		this.values.put("black_username", values[5]);
		this.values.put("starting_fen_position", values[6]);
		this.values.put("move_list", values[7]);
		this.values.put("user_to_move", values[8]);
		this.values.put("white_rating", values[9]);
		this.values.put("black_rating", values[10]);
		this.values.put("encoded_move_string", values[11]);
		this.values.put("has_new_message", values[12]);
		this.values.put("seconds_remaining", values[13]);
    //this.values.put("move_list_coordinate", values[14]);
	}
}
