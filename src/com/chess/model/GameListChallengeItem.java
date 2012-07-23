package com.chess.model;

import java.util.HashMap;

// TODO eliminate hashmaps and create Objects
public class GameListChallengeItem {

	public static final String GAME_TYPE = "game_type";
	public static final String GAME_ID = "game_id";
	public static final String TIMESTAMP = "time_stamp";
	public static final String OPPONENT_USERNAME = "opponent_username";
	public static final String OPPONENT_RATING = "opponent_rating";
	public static final String PLAYAS_COLOR = "playas_color";
	public static final String OPPONENT_WIN_COUNT = "opponent_win_count";
	public static final String OPPONENT_LOSS_COUNT = "opponent_loss_count";
	public static final String OPPONENT_DRAW_COUNT = "opponent_draw_count";
	public static final String DAYS_PER_MOVE = "days_per_move";
	public static final String RATED = "rated";
	public static final String INITIAL_SETUP_FEN = "initial_setup_fen";


	private HashMap<String, String> values;

	public GameListChallengeItem(String[] values) {
		this.values = new HashMap<String, String>();
		this.values.put(GAME_ID, values[0].trim());
		this.values.put(OPPONENT_USERNAME, values[1]);
		this.values.put(OPPONENT_RATING, values[2]);

		this.values.put(OPPONENT_WIN_COUNT, values[3]);
		this.values.put(OPPONENT_LOSS_COUNT, values[4]);
		this.values.put(OPPONENT_DRAW_COUNT, values[5]);
		this.values.put(PLAYAS_COLOR, values[6]);
		this.values.put(DAYS_PER_MOVE, values[7]);
		this.values.put(GAME_TYPE, values[8]);
		this.values.put(RATED, values[9]);
		this.values.put(INITIAL_SETUP_FEN, values[10]);
		//this.values.put("initial_setup_fen", values[9]); // api ver 1
	}

	public long getGameId(){
		return Long.parseLong(values.get(GAME_ID));
	}
	
	public String getOpponentUsername(){
		return values.get(OPPONENT_USERNAME);
	}
	
	public String getOpponentRating(){
		return values.get(OPPONENT_RATING);
	}

	public String getOpponentWinCount(){
		return values.get(OPPONENT_WIN_COUNT);
	}

	public String getOpponentLossCount(){
		return values.get(OPPONENT_LOSS_COUNT);
	}

	public String getOpponentDrawCount() {
		return values.get(OPPONENT_DRAW_COUNT);
	}

	public String getPlayAsColor() {
		return values.get(PLAYAS_COLOR);
	}

	public String getGameType() {
		return values.get(GAME_TYPE);
	}

	public String getRated() {
		return values.get(RATED);
	}

	public String getInitialSetupFen() {
		return values.get(INITIAL_SETUP_FEN);
	}

	public String getDaysPerMove() {
		return values.get(DAYS_PER_MOVE);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (String string : values.keySet()) {
			builder.append(" key = ").append(string).append(" value = ").append(values.get(string)).append("\n");
		}
		return builder.toString();
	}



//	The eches challenges response looks like the following:
//	<
//	<game_seek_id>: The game id
//	<game_name>: The seek name - can be null
//	<opponent_username>: The opponent username
//	<opponent_rating>: The opponent rating
//	<opponent_win_count>: The opponent win count
//	<opponent_loss_count>: The opponent loss count
//	<opponent_draw_count>: The opponent Draw count
//	<player_color>:  The users color he/she will play as, 1 = white, 2 = black, 0 = random
//	<days_per_move>: The days per move for the seek
//	<game_type>: The chess game type.  1 = chess, 2 = chess960
//	<is_rated>: Is the seek rated or unrated?, 1 for rated, 2 for not rated
//	<initial_setup_fen>: The initial starting position.  This field can be null
//	>
//
}
