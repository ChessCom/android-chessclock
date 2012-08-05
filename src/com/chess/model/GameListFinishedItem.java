package com.chess.model;

public class GameListFinishedItem extends BaseGameOnlineItem{

	public static final String GAME_TYPE = "game_type";
	public static final String GAME_ID = "game_id";
	public static final String TIMESTAMP = "time_stamp";
	public static final String OPPONENT_USERNAME = "opponent_username";
	public static final String OPPONENT_RATING = "opponent_rating";
	public static final String COLOR = "color";
	public static final String USERNAME_STRING_LENGTH = "username_string_length";
	public static final String TIME_REMAINING_AMOUNT = "time_remaining_amount";
	public static final String TIME_REMAINING_UNITS = "time_remaining_units";
	public static final String FEN_STRING_LENGTH = "fen_string_length";
	public static final String LAST_MOVE_FROM_SQUARE = "last_move_from_square";
	public static final String LAST_MOVE_TO_SQUARE = "last_move_to_square";
	public static final String IS_OPPONENT_ONLINE = "is_opponent_online";
	public static final String GAME_RESULT = "game_result";
	private static final String FEN = "fen";
	private String gameResults;

	public GameListFinishedItem(String[] values) {

		gameId = Long.parseLong(values[0]);
		color = values[1];
		gameType = values[2];
		userNameStrLength = values[3];
		opponentName = values[4];
		opponentRating = values[5];
		timeRemainingAmount = values[6];
		timeRemainingUnits = values[7];
		fenStrLength = values[8];
		fen = values[9];
		timestamp = Long.parseLong(values[10]);
		lastMoveFromSquare = values[11];
		lastMoveToSquare = values[12];
		isOpponentOnline = values[13].equals("1");
		gameResults = values[14];

	}

	public long getGameId(){
		return gameId;
	}

	public String getColor() {
		return color;
	}

	public String getFen() {
		return fen;
	}

	public String getFenStringLength() {
		return fenStrLength;
	}

	public String getGameResult() {
		return gameResults;
	}

	public String getGameType() {
		return gameType;
	}

	public boolean getIsOpponentOnline() {
		return isOpponentOnline;
	}

	public String getLastMoveFromSquare() {
		return lastMoveFromSquare;
	}

	public String getLastMoveToSquare() {
		return lastMoveToSquare;
	}

	public String getOpponentRating() {
		return opponentRating;
	}

	public String getOpponentUsername() {
		return opponentName;
	}

	public String getTimeRemainingAmount() {
		return timeRemainingAmount;
	}

	public String getTimeRemainingUnits() {
		return timeRemainingUnits;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getUsernameStringLength() {
		return userNameStrLength;
	}

//	@Override
//	public String toString() {
//		StringBuilder builder = new StringBuilder();
//		for (String string : values.keySet()) {
//			builder.append(" key = ").append(string).append(" value = ").append(values.get(string)).append("\n");
//		}
//		return builder.toString();
//	}





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
