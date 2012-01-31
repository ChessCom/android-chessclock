package com.chess.model;

import java.util.HashMap;

public class GameListElement {
	public int type = 0;
	public HashMap<String, String> values;
  public boolean isLiveChess;

	public GameListElement(int type, String[] values, boolean isLiveChess){
		this.type = type;
		this.values = new HashMap<String, String>();
    this.isLiveChess = isLiveChess;
		switch (type) {
			case 0:{	//Challenges
				this.values.put("game_id", values[0].trim());
				this.values.put("opponent_username", values[1]);
				this.values.put("opponent_rating", values[2]);

        if (isLiveChess)
        {
          this.values.put("opponent_chess_title", values[3]);
				  this.values.put("playas_color", values[4]);
          this.values.put("is_rated", values[5]);
          this.values.put("base_time", values[6]);
          this.values.put("time_increment", values[7]);
          this.values.put("is_direct_challenge", values[8]);
          this.values.put("is_released_by_me", values[9]);
        }
        else
        {
          this.values.put("opponent_win_count", values[3]);
				  this.values.put("opponent_loss_count", values[4]);
				  this.values.put("opponent_draw_count", values[5]);
				  this.values.put("playas_color", values[6]);
				  this.values.put("days_per_move", values[7]);
				  this.values.put("game_type", values[8]);
				  this.values.put("rated", values[9]);
				  this.values.put("initial_setup_fen", values[10]);
				  //this.values.put("initial_setup_fen", values[9]); // api ver 1
        }

				break;
			}
			case 1:{	//Current Games
				this.values.put("game_id", values[0]);
				this.values.put("color", values[1]);
				this.values.put("game_type", values[2]);
				this.values.put("username_string_length", values[3]);
				this.values.put("opponent_username", values[4]);
				this.values.put("opponent_rating", values[5]);
				this.values.put("time_remaining_amount", values[6]);
				this.values.put("time_remaining_units", values[7]);
				this.values.put("fen_string_length", values[8]);
				this.values.put("fen", values[9]);
				this.values.put("timestamp", values[10]);
				this.values.put("last_move_from_square", values[11]);
				this.values.put("last_move_to_square", values[12]);
				this.values.put("is_draw_offer_pending", values[13]);
				this.values.put("is_opponent_online", values[14]);
				this.values.put("is_my_turn", values[15]);
				this.values.put("has_new_message", values[16]);
				break;
			}
			case 2:{	//Finished Games
				this.values.put("game_id", values[0]);
				this.values.put("color", values[1]);
				this.values.put("game_type", values[2]);
				this.values.put("username_string_length", values[3]);
				this.values.put("opponent_username", values[4]);
				this.values.put("opponent_rating", values[5]);
				this.values.put("time_remaining_amount", values[6]);
				this.values.put("time_remaining_units", values[7]);
				this.values.put("fen_string_length", values[8]);
				this.values.put("fen", values[9]);
				this.values.put("timestamp", values[10]);
				this.values.put("last_move_from_square", values[11]);
				this.values.put("last_move_to_square", values[12]);
				this.values.put("is_opponent_online", values[13]);
				this.values.put("game_result", values[14]);
				break;
			}
			default: break;
		}
	}
}
