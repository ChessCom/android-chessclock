package com.chess.model;

import java.util.HashMap;

// TODO eliminate hashmaps and create Objects
// TODO divide to different game instance live/online
public class GameListItem {

	public final static int LIST_TYPE_CURRENT = 0;
	public final static int LIST_TYPE_CHALLENGES = 1;
	public final static int LIST_TYPE_FINISHED = 2;
	public static final String V_ONE = "1";

	public static final String GAME_TYPE = "game_type";
	public static final String GAME_ID = "game_id";
	public static final String TIMESTAMP = "time_stamp";
	public static final String IS_MY_TURN = "is_my_turn";
	public static final String OPPONENT_USERNAME = "opponent_username";
	public static final String OPPONENT_RATING = "opponent_rating";
	public static final String OPPONENT_CHESS_TITLE = "opponent_chess_title";
	public static final String PLAYAS_COLOR = "playas_color";
	public static final String IS_RATED = "is_rated";
	public static final String BASE_TIME = "base_time";
	public static final String TIME_INCREMENT = "time_increment";
	public static final String IS_DIRECT_CHALLENGE = "is_direct_challenge";
	public static final String IS_RELEASED_BY_ME = "is_released_by_me";
	public static final String OPPONENT_WIN_COUNT = "opponent_win_count";
	public static final String OPPONENT_LOSS_COUNT = "opponent_loss_count";
	public static final String OPPONENT_DRAW_COUNT = "opponent_draw_count";
	public static final String DAYS_PER_MOVE = "days_per_move";
	public static final String COLOR = "color";
	public static final String USERNAME_STRING_LENGTH = "username_string_length";
	public static final String TIME_REMAINING_AMOUNT = "time_remaining_amount";
	public static final String TIME_REMAINING_UNITS = "time_remaining_units";
	public static final String FEN_STRING_LENGTH = "fen_string_length";
	public static final String LAST_MOVE_FROM_SQUARE = "last_move_from_square";
	public static final String LAST_MOVE_TO_SQUARE = "last_move_to_square";
	public static final String IS_DRAW_OFFER_PENDING = "is_draw_offer_pending";
	public static final String IS_OPPONENT_ONLINE = "is_opponent_online";
	public static final String GAME_RESULT = "game_result";

	public int type = 0;
	public HashMap<String, String> values;
	public boolean isLiveChess;

	public GameListItem(int type, String[] values, boolean isLiveChess) {
		this.type = type;
		this.values = new HashMap<String, String>();
		this.isLiveChess = isLiveChess;
		switch (type) {
			case LIST_TYPE_CHALLENGES: {
				this.values.put(GAME_ID, values[0].trim());
				this.values.put(OPPONENT_USERNAME, values[1]);
				this.values.put(OPPONENT_RATING, values[2]);

				if (isLiveChess) {
					this.values.put(OPPONENT_CHESS_TITLE, values[3]);
					this.values.put(PLAYAS_COLOR, values[4]);
					this.values.put(IS_RATED, values[5]);
					this.values.put(BASE_TIME, values[6]);
					this.values.put(TIME_INCREMENT, values[7]);
					this.values.put(IS_DIRECT_CHALLENGE, values[8]);
					this.values.put(IS_RELEASED_BY_ME, values[9]);
				} else {
					this.values.put(OPPONENT_WIN_COUNT, values[3]);
					this.values.put(OPPONENT_LOSS_COUNT, values[4]);
					this.values.put(OPPONENT_DRAW_COUNT, values[5]);
					this.values.put(PLAYAS_COLOR, values[6]);
					this.values.put(DAYS_PER_MOVE, values[7]);
					this.values.put(GAME_TYPE, values[8]);
					this.values.put("rated", values[9]);
					this.values.put("initial_setup_fen", values[10]);
					//this.values.put("initial_setup_fen", values[9]); // api ver 1
				}

				break;
			}
			case LIST_TYPE_CURRENT: {
				this.values.put(GAME_ID, values[0]);
				this.values.put(COLOR, values[1]);
				this.values.put(GAME_TYPE, values[2]);
				this.values.put(USERNAME_STRING_LENGTH, values[3]);
				this.values.put(OPPONENT_USERNAME, values[4]);
				this.values.put(OPPONENT_RATING, values[5]);
				this.values.put(TIME_REMAINING_AMOUNT, values[6]);
				this.values.put(TIME_REMAINING_UNITS, values[7]);
				this.values.put(FEN_STRING_LENGTH, values[8]);
				this.values.put("fen", values[9]);
				this.values.put(TIMESTAMP, values[10]);
				this.values.put(LAST_MOVE_FROM_SQUARE, values[11]);
				this.values.put(LAST_MOVE_TO_SQUARE, values[12]);
				this.values.put(IS_DRAW_OFFER_PENDING, values[13]);
				this.values.put(IS_OPPONENT_ONLINE, values[14]);
				this.values.put(IS_MY_TURN, values[15]);
				this.values.put(GameItem.HAS_NEW_MESSAGE, values[16]);
				break;
			}
			case LIST_TYPE_FINISHED: {
				this.values.put(GAME_ID, values[0]);
				this.values.put(COLOR, values[1]);
				this.values.put(GAME_TYPE, values[2]);
				this.values.put(USERNAME_STRING_LENGTH, values[3]);
				this.values.put(OPPONENT_USERNAME, values[4]);
				this.values.put(OPPONENT_RATING, values[5]);
				this.values.put(TIME_REMAINING_AMOUNT, values[6]);
				this.values.put(TIME_REMAINING_UNITS, values[7]);
				this.values.put(FEN_STRING_LENGTH, values[8]);
				this.values.put("fen", values[9]);
				this.values.put(TIMESTAMP, values[10]);
				this.values.put(LAST_MOVE_FROM_SQUARE, values[11]);
				this.values.put(LAST_MOVE_TO_SQUARE, values[12]);
				this.values.put(IS_OPPONENT_ONLINE, values[13]);
				this.values.put(GAME_RESULT, values[14]);
				break;
			}
			default:
				break;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (String string : values.keySet()) {
			builder.append(" key = ").append(string).append(" value = ").append(values.get(string)).append("\n");
		}
		return builder.toString();
	}


	public long getGameId(){
		return Long.parseLong(values.get(GameListItem.GAME_ID));
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
//	The echess current games response looks like the following:
//	<
//	<game_id>: The game id
//	<player_color>: The users color he/she will play as, w = white, b = black
//	<game_type>: The chess game type.  1 = chess, 2 = chess960
//	<game_name>: The games name, can be null
//	<white_username>: The username for white
//	<black_username>: The username for black
//	<white_rating>: Whites rating
//	<black_rating>: Blacks rating
//	<time_stamp>: The timestamp for the game
//	<time_remaining_amount>: Remaining time for the game
//	<time_remaining_units>: the units, d for day and h for hours
//	<initial_fen>: the initial starting position, can be null
//	<last_move_from_square>: the last move, from square
//	<last_move_to_square>: the last move, to square
//	<is_draw_offer_pending>: Draw offer pending.  n = no, p = pending offer
//	<is_opponent_online>: Is the opponent online. 1 = yes, 0 = no
//	<is_my_turn>: is it the users turn to move. 1 = yes, 0 = no
//	<has_new_message>: new messages for the game. 1 = yes, 0 = no
//	<move_list>: the moves from the start of the game (all moves after <intial_fen>)
//	<days_per_move>: how many days per move.
//	>
//
//	The echess finished games response looks like the following:
//	<
//	<game_id>: The game id
//	<player_color>: The users color he/she played as, w = white, b = black
//	<game_type>: The chess game type.  1 = chess, 2 = chess960
//	<game_name>: The games name, can be null
//	<white_username>: The username for white
//	<black_username>: The username for black
//	<white_rating>: Whites rating
//	<black_rating>: Blacks rating
//	<time_stamp>: The timestamp for the game
//	<initial_fen>: the initial starting position, can be null
//	<last_move_from_square>: the last move, from square
//	<last_move_to_square>: the last move, to square
//	<has_new_message>: new messages for the game. 1 = yes, 0 = no
//	<move_list>: the moves from the start of the game (all moves after <intial_fen>)
//	<game_result>: The result of the game.  1 = win, 0 = loss, 2 = draw
//	>
}
