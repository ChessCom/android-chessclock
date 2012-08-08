package com.chess.model;

public class GameListCurrentItem extends BaseGameOnlineItem {

//	public static final String GAME_ID = "game_id";

	public GameListCurrentItem(String[] values) {

		gameId = Long.parseLong(values[0]);
		color = values[1];
		gameType =  values[2];
		userNameStrLength = values[3];
		opponentName = values[4];
		opponentRating = values[5];
		timeRemainingAmount = values[6];
		timeRemainingUnits = values[7];
		fenStrLength = values[8];
		fen = values[9];
		timestamp = Long.parseLong(values[10]);
		lastMoveFromSquare =  values[11];
		lastMoveToSquare = values[12];
		isDrawOfferPending = values[13].equals("p");
		isOpponentOnline = values[14].equals("1");
		isMyTurn = values[15].equals("1");
		hasMessage = values[16].equals("1");

	}

	public long getGameId(){
		return gameId;
	}

	public String getColor() {
		return color;
	}

	public String getGameType() {
		return gameType;
	}

	public String getUsernameStringLength() {
		return userNameStrLength;
	}

	public String getOpponentUsername() {
		return opponentName;
	}

	public String getOpponentRating() {
		return opponentRating;
	}

	public String getFenStringLength() {
		return fenStrLength;
	}

	public String getLastMoveFromSquare() {
		return lastMoveFromSquare;
	}

	public String getLastMoveToSquare() {
		return lastMoveToSquare;
	}

	public boolean getIsDrawOfferPending() {
		return isDrawOfferPending;
	}

	public boolean getIsOpponentOnline() {
		return isOpponentOnline;
	}

	public boolean getHasNewMessage() {
		return hasMessage;
	}

	public boolean getIsMyTurn() {
		return isMyTurn;
	}

//	@Override
//	public String toString() {
//		StringBuilder builder = new StringBuilder();
//		for (String string : values.keySet()) {
//			builder.append(" key = ").append(string).append(" value = ").append(values.get(string)).append("\n");
//		}
//		return builder.toString();
//	}




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
}
