package com.chess.model;

import com.chess.backend.statics.AppConstants;

import java.io.Serializable;
import java.util.HashMap;

// TODO divide to different game instance live/online
public class GameItem implements Serializable { // TODO make an Object, not hashmap

    public static final int STARTING_FEN_POSITION_NUMB = 6;
    public static final int MOVE_LIST_NUMB = 7;

	public static int GAME_DATA_ELEMENTS_COUNT = 14;

	public static final String STARTING_FEN_POSITION = "starting_fen_position";
	public static final String WHITE_RATING = "white_rating";
	public static final String BLACK_RATING = "black_rating";
	public static final String ENCODED_MOVE_STRING = "encoded_move_string";
	public static final String HAS_NEW_MESSAGE = "has_new_message";
	public static final String USER_TO_MOVE = "user_to_move";
	public static final String SECONDS_REMAINING = "seconds_remaining";
	public static final String GAME_NAME = "game_name";

	public HashMap<String, String> values;

    public GameItem(String[] values, boolean isLiveChess) {
		this.values = new HashMap<String, String>();
		final String gameId = isLiveChess ? values[0] : values[0].split("[+]")[1];
		this.values.put(GameListItem.GAME_ID, gameId);
		this.values.put(GameListItem.GAME_TYPE, values[1]);
		this.values.put(GameListItem.TIMESTAMP, values[2]);
		this.values.put(GAME_NAME, values[3]);
		this.values.put(AppConstants.WHITE_USERNAME, values[4].trim());
		this.values.put(AppConstants.BLACK_USERNAME, values[5].trim());
		this.values.put(STARTING_FEN_POSITION, values[STARTING_FEN_POSITION_NUMB]);
		this.values.put(AppConstants.MOVE_LIST, values[MOVE_LIST_NUMB]);
		this.values.put(USER_TO_MOVE, values[8]);
		this.values.put(WHITE_RATING, values[9]);
		this.values.put(BLACK_RATING, values[10]);
		this.values.put(ENCODED_MOVE_STRING, values[11]);
		this.values.put(HAS_NEW_MESSAGE, values[12]);
		this.values.put(SECONDS_REMAINING, values[13]);
		//this.values.put("move_list_coordinate", values[14]);
	}
}
