package com.chess.model;

import java.io.Serializable;
import java.util.HashMap;

// TODO divide to different game instance live/online
public class GameItem implements Serializable { // TODO make an Object, not hashmap

    public static final int STARTING_FEN_POSITION_NUMB = 6;
    public static final int MOVE_LIST_NUMB = 7;

	public static int GAME_DATA_ELEMENTS_COUNT = 14;

	public static final String GAME_TYPE = "game_type";
	public static final String GAME_ID = "game_id";
	public static final String TIMESTAMP = "time_stamp";
	public static final String STARTING_FEN_POSITION = "starting_fen_position";
	public static final String WHITE_RATING = "white_rating";
	public static final String BLACK_RATING = "black_rating";
	public static final String ENCODED_MOVE_STRING = "encoded_move_string";
	public static final String HAS_NEW_MESSAGE = "has_new_message";
	public static final String USER_TO_MOVE = "user_to_move";
	public static final String SECONDS_REMAINING = "seconds_remaining";
	public static final String GAME_NAME = "game_name";
	public static final String WHITE_USERNAME = "white_username";
	public static final String BLACK_USERNAME = "black_username";
	public static final String MOVE_LIST = "move_list";
	private HashMap<String, String> values;

    public GameItem(String[] values, boolean isLiveChess) {
		this.values = new HashMap<String, String>();
		final String gameId = isLiveChess ? values[0] : values[0].split("[+]")[1];
		this.values.put(GAME_ID, gameId);
		this.values.put(GAME_TYPE, values[1]);
		this.values.put(TIMESTAMP, values[2]);
		this.values.put(GAME_NAME, values[3]);
		this.values.put(WHITE_USERNAME, values[4].trim());
		this.values.put(BLACK_USERNAME, values[5].trim());
		this.values.put(STARTING_FEN_POSITION, values[STARTING_FEN_POSITION_NUMB]);
		this.values.put(MOVE_LIST, values[MOVE_LIST_NUMB]);
		this.values.put(USER_TO_MOVE, values[8]);
		this.values.put(WHITE_RATING, values[9]);
		this.values.put(BLACK_RATING, values[10]);
		this.values.put(ENCODED_MOVE_STRING, values[11]);
		this.values.put(HAS_NEW_MESSAGE, values[12]);
		this.values.put(SECONDS_REMAINING, values[13]);
		//this.values.put("move_list_coordinate", values[14]);
	}

	public String getBlackRating() {
		return values.get(BLACK_RATING);
	}

	public String getBlackUsername() {
		return values.get(BLACK_USERNAME);
	}

	public String getEncodedMoveString() {
		return values.get(ENCODED_MOVE_STRING);
	}

	public String getGameId() {
		return values.get(GAME_ID);
	}

	public String getGameName() {
		return values.get(GAME_NAME);
	}

	public String getGameType() {
		return values.get(GAME_TYPE);
	}

	public String getHasNewMessage() {
		return values.get(HAS_NEW_MESSAGE);
	}

	public void setHasNewMessage(String value){
		values.put(HAS_NEW_MESSAGE, value);
	}
	
	public String getMoveList() {
		return values.get(MOVE_LIST);
	}

	public String getSecondsRemaining() {
		return values.get(SECONDS_REMAINING);
	}

	public String getStartingFenPosition() {
		return values.get(STARTING_FEN_POSITION);
	}

	public String getTimestamp() {
		return values.get(TIMESTAMP);
	}

	public String getUserToMove() {
		return values.get(USER_TO_MOVE);
	}

	public String getWhiteRating() {
		return values.get(WHITE_RATING);
	}

	public String getWhiteUsername() {
		return values.get(WHITE_USERNAME);
	}
}
