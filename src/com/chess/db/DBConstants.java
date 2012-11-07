package com.chess.db;

import android.net.Uri;

/**
 * @author alien_roger
 * @created 27.10.12
 * @modified 27.10.12
 */
public class DBConstants {

    static final int DATABASE_VERSION 	= 4;  // change version on every DB scheme changes


	public static final String PROVIDER_NAME = "com.chess.db_provider";
	/*
	 * DB table names
	 */
    static final String DATABASE_NAME  = "Chess DB";
    public static final String TACTICS_BATCH_TABLE = "tactics_batch";
    public static final String ECHESS_FINISHED_LIST_GAMES_TABLE = "echess_finished_games";
    public static final String ECHESS_CURRENT_LIST_GAMES_TABLE = "echess_current_games";
    public static final String ECHESS_ONLINE_GAMES_TABLE = "echess_online_games";



	// Content URI
    public static final Uri TACTICS_BATCH_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/" + TACTICS_BATCH_TABLE);
    public static final Uri ECHESS_FINISHED_LIST_GAMES_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/" + ECHESS_FINISHED_LIST_GAMES_TABLE);
    public static final Uri ECHESS_CURRENT_LIST_GAMES_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/" + ECHESS_CURRENT_LIST_GAMES_TABLE);
    public static final Uri ECHESS_ONLINE_GAMES_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/" + ECHESS_ONLINE_GAMES_TABLE);


    // uri paths
    public static final int TACTICS_BATCH = 0;
    public static final int ECHESS_FINISHED_LIST_GAMES = 1;
    public static final int ECHESS_CURRENT_LIST_GAMES = 2;
    public static final int ECHESS_ONLINE_GAMES = 3;


    // general fields
    public static final String _ID = "_id";
    public static final String _COUNT = "_count";

    /* TacticsItem Fields */

    public static final String V_USER     		= "user";
    public static final String V_TACTIC_ID 		= "tactic_id";
    public static final String V_FEN      		= "fen";
    public static final String V_MOVE_LIST      = "moveList";
    public static final String V_ATTEMPT_CNT    = "attemptCnt";
    public static final String V_PASSED_CNT     = "passedCnt";
    public static final String V_RATING       	= "rating";
    public static final String V_AVG_SECONDS 	= "avgSeconds";

	/* ECHESS_GAMES */
	public static final String V_FINISHED 				= "isFinished";
	public static final String V_GAME_ID 				= "gameId";
	public static final String V_COLOR 					= "color";
	public static final String V_GAME_TYPE 				= "gameType";
	public static final String V_GAME_NAME 				= "gameName";
	public static final String V_WHITE_USER_NAME 		= "whiteUsername";
	public static final String V_BLACK_USER_NAME 		= "blackUsername";
	public static final String V_FEN_START_POSITION 	= "fenStartPosition";
	public static final String V_WHITE_USER_MOVE 		= "whiteUserMove";
	public static final String V_WHITE_RATING 			= "whiteRating";
	public static final String V_BLACK_RATING 			= "blackRating";
	public static final String V_ENCODED_MOVE_STR 		= "encodedMoveStr";
	public static final String V_HAS_NEW_MESSAGE 		= "hasNewMessage";
	public static final String V_SECONDS_REMAIN 		= "secondsRemain";
	public static final String V_RATED 					= "rated";
	public static final String V_DAYS_PER_MOVE 			= "daysPerMove";
	public static final String V_USER_OFFERED_DRAW 		= "userOfferedDraw";

	public static final String V_USER_NAME_STR_LENGTH 	= "userNameStrLength";
	public static final String V_OPPONENT_NAME 			= "opponentName";
	public static final String V_OPPONENT_RATING 		= "opponentRating";
	public static final String V_TIME_REMAINING_AMOUNT 	= "timeRemainingAmount";
	public static final String V_TIME_REMAINING_UNITS 	= "timeRemainingUnits";
	public static final String V_FEN_STR_LENGTH 		= "fenStrLength";
	public static final String V_TIMESTAMP 				= "timestamp";
	public static final String V_LAST_MOVE_FROM_SQUARE 	= "lastMoveFromSquare";
	public static final String V_LAST_MOVE_TO_SQUARE 	= "lastMoveToSquare";
	public static final String V_IS_OPPONENT_ONLINE 	= "isOpponentOnline";
	public static final String V_GAME_RESULTS 			= "gameResults";
	public static final String V_IS_MY_TURN 			= "isMyTurn";

    /* common commands */
    private static final String CREATE_TABLE_IF_NOT_EXISTS = "create table if not exists ";
    private static final String _INT_NOT_NULL 		= " INT not null";
    private static final String _LONG_NOT_NULL 		= " LONG not null";
    private static final String _DOUBLE_NOT_NULL 	= " DOUBLE not null";
    private static final String _TEXT_NOT_NULL 		= " TEXT not null";
    private static final String _COMMA 				= ",";
    private static final String _CLOSE 				= ");";
    private static final String ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT = " (_id integer primary key autoincrement, ";


    static final String TACTICS_BATCH_TABLE_CREATE =
            CREATE_TABLE_IF_NOT_EXISTS + TACTICS_BATCH_TABLE + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
			+ V_USER 		+ _TEXT_NOT_NULL + _COMMA
			+ V_TACTIC_ID   + _TEXT_NOT_NULL + _COMMA
			+ V_FEN 		+ _TEXT_NOT_NULL + _COMMA
			+ V_MOVE_LIST 	+ _TEXT_NOT_NULL + _COMMA
			+ V_ATTEMPT_CNT + _TEXT_NOT_NULL + _COMMA
			+ V_PASSED_CNT 	+ _TEXT_NOT_NULL + _COMMA
			+ V_RATING 		+ _TEXT_NOT_NULL + _COMMA
			+ V_AVG_SECONDS + _TEXT_NOT_NULL + _CLOSE;



	static final String ECHESS_FINISHED_LIST_GAMES_CREATE =
			CREATE_TABLE_IF_NOT_EXISTS + ECHESS_FINISHED_LIST_GAMES_TABLE + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
			+ V_USER 				    	+ _TEXT_NOT_NULL + _COMMA
			+ V_GAME_ID 				    + _LONG_NOT_NULL + _COMMA
			+ V_COLOR 					    + _INT_NOT_NULL + _COMMA
			+ V_GAME_TYPE 				    + _INT_NOT_NULL + _COMMA
			+ V_USER_NAME_STR_LENGTH 	    + _INT_NOT_NULL + _COMMA
			+ V_OPPONENT_NAME 			    + _TEXT_NOT_NULL + _COMMA
			+ V_OPPONENT_RATING 		    + _INT_NOT_NULL + _COMMA
			+ V_TIME_REMAINING_AMOUNT 	    + _INT_NOT_NULL + _COMMA
			+ V_TIME_REMAINING_UNITS 	    + _TEXT_NOT_NULL + _COMMA
			+ V_FEN_STR_LENGTH 		        + _INT_NOT_NULL + _COMMA
			+ V_TIMESTAMP 				    + _LONG_NOT_NULL + _COMMA
			+ V_LAST_MOVE_FROM_SQUARE 	    + _TEXT_NOT_NULL + _COMMA
			+ V_LAST_MOVE_TO_SQUARE 	    + _TEXT_NOT_NULL + _COMMA
			+ V_IS_OPPONENT_ONLINE 	        + _INT_NOT_NULL + _COMMA
			+ V_GAME_RESULTS 			    + _TEXT_NOT_NULL + _CLOSE;

	static final String ECHESS_CURRENT_LIST_GAMES_CREATE =
			CREATE_TABLE_IF_NOT_EXISTS + ECHESS_CURRENT_LIST_GAMES_TABLE + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
			+ V_USER 				    	+ _TEXT_NOT_NULL + _COMMA
			+ V_GAME_ID 				    + _LONG_NOT_NULL + _COMMA
			+ V_COLOR 					    + _INT_NOT_NULL + _COMMA
			+ V_GAME_TYPE 				    + _INT_NOT_NULL + _COMMA
			+ V_USER_NAME_STR_LENGTH 	    + _INT_NOT_NULL + _COMMA
			+ V_OPPONENT_NAME 			    + _TEXT_NOT_NULL + _COMMA
			+ V_OPPONENT_RATING 		    + _INT_NOT_NULL + _COMMA
			+ V_TIME_REMAINING_AMOUNT 	    + _INT_NOT_NULL + _COMMA
			+ V_TIME_REMAINING_UNITS 	    + _TEXT_NOT_NULL + _COMMA
			+ V_FEN_STR_LENGTH 		        + _INT_NOT_NULL + _COMMA
			+ V_TIMESTAMP 				    + _LONG_NOT_NULL + _COMMA
			+ V_LAST_MOVE_FROM_SQUARE 	    + _TEXT_NOT_NULL + _COMMA
			+ V_LAST_MOVE_TO_SQUARE 	    + _TEXT_NOT_NULL + _COMMA
			+ V_IS_OPPONENT_ONLINE 	        + _INT_NOT_NULL + _COMMA
			+ V_IS_MY_TURN 	        		+ _INT_NOT_NULL + _COMMA
			+ V_HAS_NEW_MESSAGE 			+ _INT_NOT_NULL + _CLOSE;

	static final String ECHESS_ONLINE_GAMES_CREATE =
			CREATE_TABLE_IF_NOT_EXISTS + ECHESS_ONLINE_GAMES_TABLE + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
			+ V_FINISHED 				+ _INT_NOT_NULL + _COMMA
			+ V_USER 					+ _TEXT_NOT_NULL + _COMMA
			+ V_GAME_ID 				+ _LONG_NOT_NULL + _COMMA
			+ V_GAME_TYPE 				+ _INT_NOT_NULL + _COMMA
			+ V_TIMESTAMP 	    		+ _LONG_NOT_NULL + _COMMA
			+ V_GAME_NAME 	    		+ _TEXT_NOT_NULL + _COMMA
			+ V_WHITE_USER_NAME 		+ _TEXT_NOT_NULL + _COMMA
			+ V_BLACK_USER_NAME 		+ _TEXT_NOT_NULL + _COMMA
			+ V_FEN_START_POSITION 	    + _TEXT_NOT_NULL + _COMMA
			+ V_WHITE_USER_MOVE 	    + _INT_NOT_NULL + _COMMA
			+ V_WHITE_RATING 		    + _INT_NOT_NULL + _COMMA
			+ V_BLACK_RATING 			+ _INT_NOT_NULL + _COMMA
			+ V_ENCODED_MOVE_STR 	    + _TEXT_NOT_NULL + _COMMA
			+ V_HAS_NEW_MESSAGE 	    + _INT_NOT_NULL + _COMMA
			+ V_SECONDS_REMAIN 	    	+ _LONG_NOT_NULL + _COMMA
			+ V_RATED 	    			+ _INT_NOT_NULL + _COMMA
			+ V_USER_OFFERED_DRAW 	   	+ _INT_NOT_NULL + _COMMA
			+ V_DAYS_PER_MOVE 			+ _INT_NOT_NULL + _CLOSE;

}
