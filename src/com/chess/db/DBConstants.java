package com.chess.db;

import android.net.Uri;

/**
 * @author alien_roger
 * @created 27.10.12
 * @modified 27.10.12
 */
public class DBConstants {

    static final int DATABASE_VERSION = 15;  // change version on every DB scheme changes


	public static final String PROVIDER_NAME = "com.chess.db_provider";
	/*
	 * DB table names
	 */
    static final String DATABASE_NAME  = "Chess DB";
    public static final String TACTICS_BATCH_TABLE = "tactics_batch";
    public static final String TACTICS_RESULTS_TABLE = "tactics_results";
    public static final String ECHESS_FINISHED_LIST_GAMES_TABLE = "echess_finished_games";
    public static final String ECHESS_CURRENT_LIST_GAMES_TABLE = "echess_current_games";
    public static final String ECHESS_ONLINE_GAMES_TABLE = "echess_online_games";
    public static final String FRIENDS_TABLE = "friends";
    public static final String ARTICLES_TABLE = "articles";
    public static final String ARTICLE_CATEGORIES_TABLE = "article_categories";
    public static final String VIDEOS_TABLE = "videos";
    public static final String VIDEO_CATEGORIES_TABLE = "video_categories";



	// Content URI
    public static final Uri TACTICS_BATCH_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/" + TACTICS_BATCH_TABLE);
    public static final Uri TACTICS_RESULTS_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/" + TACTICS_RESULTS_TABLE);
    public static final Uri ECHESS_FINISHED_LIST_GAMES_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/" + ECHESS_FINISHED_LIST_GAMES_TABLE);
    public static final Uri ECHESS_CURRENT_LIST_GAMES_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/" + ECHESS_CURRENT_LIST_GAMES_TABLE);
    public static final Uri ECHESS_ONLINE_GAMES_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/" + ECHESS_ONLINE_GAMES_TABLE);
    public static final Uri FRIENDS_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/" + FRIENDS_TABLE);
    public static final Uri ARTICLES_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/" + ARTICLES_TABLE);
    public static final Uri ARTICLE_CATEGORIES_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/" + ARTICLE_CATEGORIES_TABLE);
    public static final Uri VIDEOS_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/" + VIDEOS_TABLE);
    public static final Uri VIDEO_CATEGORIES_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/" + VIDEO_CATEGORIES_TABLE);


    // uri paths
    public static final int TACTICS_BATCH = 0;
    public static final int TACTICS_RESULTS = 1;
    public static final int ECHESS_FINISHED_LIST_GAMES = 2;
    public static final int ECHESS_CURRENT_LIST_GAMES = 3;
    public static final int ECHESS_ONLINE_GAMES = 4;
    public static final int FRIENDS = 5;
    public static final int ARTICLES = 6;
    public static final int ARTICLE_CATEGORIES = 7;
    public static final int VIDEOS = 8;
    public static final int VIDEO_CATEGORIES = 9;


    // general fields
    public static final String _ID = "_id";
    public static final String _COUNT = "_count";

    /* TacticsItem Fields */

    public static final String V_USER     		= "user";
    public static final String V_TACTIC_ID 		= "tactic_id";
    public static final String V_FEN      		= "fen";
    public static final String V_MOVE_LIST      = "move_list";
    public static final String V_ATTEMPT_CNT    = "attempt_cnt";
    public static final String V_PASSED_CNT     = "passed_cnt";
    public static final String V_RATING       	= "rating";
    public static final String V_AVG_SECONDS 	= "avg_seconds";
    public static final String V_SECONDS_SPENT 	= "seconds_spent";
    public static final String V_STOP 	        = "stop";
    public static final String V_WAS_SHOWED     = "was_showed";
    public static final String V_IS_RETRY       = "is_retry";

    public static final String V_SCORE                  = "score";
    public static final String V_USER_RATING_CHANGE     = "user_rating_change";
    public static final String V_USER_RATING            = "user_rating";
    public static final String V_PROBLEM_RATING_CHANGE  = "problem_rating_change";
    public static final String V_PROBLEM_RATING         = "problem_rating";


	/* ECHESS_GAMES */
	public static final String V_FINISHED 				= "is_finished";
	public static final String V_GAME_ID 				= "game_id";
	public static final String V_COLOR 					= "color";
	public static final String V_GAME_TYPE 				= "game_type";
	public static final String V_GAME_NAME 				= "game_name";
	public static final String V_WHITE_USER_NAME 		= "white_username";
	public static final String V_BLACK_USER_NAME 		= "black_username";
	public static final String V_FEN_START_POSITION 	= "fen_start_position";
	public static final String V_WHITE_USER_MOVE 		= "white_user_move";
	public static final String V_WHITE_RATING 			= "white_rating";
	public static final String V_BLACK_RATING 			= "black_rating";
	public static final String V_ENCODED_MOVE_STR 		= "encoded_move_str";
	public static final String V_HAS_NEW_MESSAGE 		= "has_new_message";
	public static final String V_SECONDS_REMAIN 		= "seconds_remain";
	public static final String V_RATED 					= "rated";
	public static final String V_DAYS_PER_MOVE 			= "days_per_move";
	public static final String V_USER_OFFERED_DRAW 		= "user_offered_draw";
	public static final String V_OPPONENT_OFFERED_DRAW 	= "opponent_offered_draw";

	public static final String V_OPPONENT_NAME 			= "opponent_name";
	public static final String V_OPPONENT_RATING 		= "opponent_rating";
	public static final String V_TIME_REMAINING 		= "time_remaining";
	public static final String V_TIMESTAMP 				= "timestamp";
	public static final String V_LAST_MOVE_FROM_SQUARE 	= "last_move_from_square";
	public static final String V_LAST_MOVE_TO_SQUARE 	= "last_move_to_square";
	public static final String V_GAME_RESULT 			= "game_result";
	public static final String V_IS_MY_TURN 			= "is_my_turn";

	/*Friends*/
	public static final String V_USERNAME 				= "username";
	public static final String V_USER_ID 				= "user_id";
	public static final String V_LOCATION 				= "location";
	public static final String V_COUNTRY_ID 			= "country_id";
	public static final String V_PHOTO_URL 				= "photo_url";

	/*Articles*/
	public static final String V_ARTICLE_ID 			= "article_id";
	public static final String V_TITLE 					= "title";
	public static final String V_CREATE_DATE 			= "create_date";
	public static final String V_CATEGORY 				= "category";
	public static final String V_CATEGORY_ID 			= "category_id";
	public static final String V_CHESS_TITLE 			= "chess_title";
	public static final String V_FIRST_NAME 			= "first_name";
	public static final String V_LAST_NAME 				= "last_name";

	/*Articles Categories*/
	public static final String V_ID 				= "id";
	public static final String V_NAME 				= "name";
	public static final String V_DISPLAY_ORDER 		= "display_order";
/*
	"id": 11,
	"name": "For Beginners",
	"display_order": 10
*/

	/*Videos*/
	public static final String V_DESCRIPTION 		= "description";
	public static final String V_SKILL_LEVEL 		= "skill_level";
	public static final String V_ECO_NAME 			= "eco_name";
	public static final String V_MINUTES 			= "minutes";
	public static final String V_MOBILE_URL 		= "mobile_url";
	public static final String V_KEY_FEN 			= "key_fen";

/*
		private String chess_video_category_id;
		private long name;
		private String code;
		private int display_order;
 */


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
			+ V_USER 		    + _TEXT_NOT_NULL + _COMMA
			+ V_TACTIC_ID       + _LONG_NOT_NULL + _COMMA
			+ V_FEN 		    + _TEXT_NOT_NULL + _COMMA
			+ V_MOVE_LIST 	    + _TEXT_NOT_NULL + _COMMA
			+ V_ATTEMPT_CNT     + _INT_NOT_NULL + _COMMA
			+ V_PASSED_CNT 	    + _INT_NOT_NULL + _COMMA
			+ V_RATING          + _INT_NOT_NULL + _COMMA
			+ V_STOP 		    + _INT_NOT_NULL + _COMMA
			+ V_WAS_SHOWED	    + _INT_NOT_NULL + _COMMA
			+ V_IS_RETRY	    + _INT_NOT_NULL + _COMMA
			+ V_SECONDS_SPENT	+ _LONG_NOT_NULL + _COMMA
			+ V_AVG_SECONDS     + _TEXT_NOT_NULL + _CLOSE;

    static final String TACTICS_RESULTS_TABLE_CREATE =
            CREATE_TABLE_IF_NOT_EXISTS + TACTICS_RESULTS_TABLE + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
            + V_USER 		        	+ _TEXT_NOT_NULL + _COMMA
            + V_TACTIC_ID           	+ _LONG_NOT_NULL + _COMMA
            + V_SCORE               	+ _TEXT_NOT_NULL + _COMMA
            + V_USER_RATING_CHANGE		+ _INT_NOT_NULL + _COMMA
            + V_USER_RATING         	+ _INT_NOT_NULL + _COMMA
            + V_PROBLEM_RATING_CHANGE 	+ _INT_NOT_NULL + _COMMA
            + V_PROBLEM_RATING      	+ _INT_NOT_NULL + _CLOSE;


    static final String ECHESS_FINISHED_LIST_GAMES_CREATE =
			CREATE_TABLE_IF_NOT_EXISTS + ECHESS_FINISHED_LIST_GAMES_TABLE + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
			+ V_USER 				    	+ _TEXT_NOT_NULL + _COMMA
			+ V_GAME_ID 				    + _LONG_NOT_NULL + _COMMA
			+ V_COLOR 					    + _INT_NOT_NULL + _COMMA
			+ V_GAME_TYPE 				    + _INT_NOT_NULL + _COMMA
			+ V_OPPONENT_NAME 			    + _TEXT_NOT_NULL + _COMMA
			+ V_OPPONENT_RATING 		    + _INT_NOT_NULL + _COMMA
			+ V_TIME_REMAINING 				+ _INT_NOT_NULL + _COMMA
			+ V_TIMESTAMP 				    + _LONG_NOT_NULL + _COMMA
			+ V_LAST_MOVE_FROM_SQUARE 	    + _TEXT_NOT_NULL + _COMMA
			+ V_LAST_MOVE_TO_SQUARE 	    + _TEXT_NOT_NULL + _COMMA
			+ V_GAME_RESULT + _INT_NOT_NULL + _CLOSE;

	static final String ECHESS_CURRENT_LIST_GAMES_CREATE =
			CREATE_TABLE_IF_NOT_EXISTS + ECHESS_CURRENT_LIST_GAMES_TABLE + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
			+ V_USER 				    	+ _TEXT_NOT_NULL + _COMMA
			+ V_GAME_ID 				    + _LONG_NOT_NULL + _COMMA
			+ V_COLOR 					    + _INT_NOT_NULL + _COMMA
			+ V_GAME_TYPE 				    + _INT_NOT_NULL + _COMMA
			+ V_OPPONENT_NAME 			    + _TEXT_NOT_NULL + _COMMA
			+ V_OPPONENT_RATING 		    + _INT_NOT_NULL + _COMMA
			+ V_TIME_REMAINING 				+ _INT_NOT_NULL + _COMMA
			+ V_TIMESTAMP 				    + _LONG_NOT_NULL + _COMMA
			+ V_LAST_MOVE_FROM_SQUARE 	    + _TEXT_NOT_NULL + _COMMA
			+ V_LAST_MOVE_TO_SQUARE 	    + _TEXT_NOT_NULL + _COMMA
			+ V_OPPONENT_OFFERED_DRAW       + _INT_NOT_NULL + _COMMA
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
			+ V_MOVE_LIST		 	    + _TEXT_NOT_NULL + _COMMA
			+ V_WHITE_USER_MOVE 	    + _INT_NOT_NULL + _COMMA
			+ V_WHITE_RATING 		    + _INT_NOT_NULL + _COMMA
			+ V_BLACK_RATING 			+ _INT_NOT_NULL + _COMMA
			+ V_ENCODED_MOVE_STR 	    + _TEXT_NOT_NULL + _COMMA
			+ V_HAS_NEW_MESSAGE 	    + _INT_NOT_NULL + _COMMA
			+ V_SECONDS_REMAIN 	    	+ _LONG_NOT_NULL + _COMMA
			+ V_RATED 	    			+ _INT_NOT_NULL + _COMMA
			+ V_USER_OFFERED_DRAW 	   	+ _INT_NOT_NULL + _COMMA
			+ V_DAYS_PER_MOVE 			+ _INT_NOT_NULL + _CLOSE;

	static final String FRIENDS_CREATE =
			CREATE_TABLE_IF_NOT_EXISTS + FRIENDS_TABLE + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
			+ V_USER 					+ _TEXT_NOT_NULL + _COMMA
			+ V_USERNAME 				+ _TEXT_NOT_NULL + _COMMA
			+ V_LOCATION 	    		+ _TEXT_NOT_NULL + _COMMA
			+ V_COUNTRY_ID 	    		+ _INT_NOT_NULL + _COMMA
			+ V_USER_ID 	    		+ _INT_NOT_NULL + _COMMA
			+ V_PHOTO_URL 	    		+ _TEXT_NOT_NULL + _CLOSE;


/*
            "id": 202,
            "title": "Test article 2nd round",
            "create_date": 1342827342,
            "article_category": "For Beginners",
            "article_category_id": 11,
            "user_id": 5543,
            "username": "deepgreene"
	 */

	static final String ARTICLES_CREATE =
			CREATE_TABLE_IF_NOT_EXISTS + ARTICLES_TABLE + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
			+ V_ARTICLE_ID 				+ _LONG_NOT_NULL + _COMMA
			+ V_TITLE 					+ _TEXT_NOT_NULL + _COMMA
			+ V_CREATE_DATE 			+ _LONG_NOT_NULL + _COMMA
			+ V_CATEGORY 				+ _TEXT_NOT_NULL + _COMMA
			+ V_CATEGORY_ID 			+ _LONG_NOT_NULL + _COMMA
			+ V_USER_ID 				+ _LONG_NOT_NULL + _COMMA
			+ V_USERNAME 	    		+ _TEXT_NOT_NULL + _COMMA
			+ V_COUNTRY_ID 	    		+ _INT_NOT_NULL + " DEFAULT 0"+ _COMMA
			+ V_FIRST_NAME 	    		+ _TEXT_NOT_NULL + " DEFAULT TestFirstName"+ _COMMA
			+ V_LAST_NAME 	    		+ _TEXT_NOT_NULL + " DEFAULT TestLastName"+ _COMMA
			+ V_CHESS_TITLE 	    	+ _TEXT_NOT_NULL + " DEFAULT TestIM"+ _CLOSE;

	static final String ARTICLE_CATEGORIES_CREATE =
			CREATE_TABLE_IF_NOT_EXISTS + ARTICLE_CATEGORIES_TABLE + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
			+ V_NAME 					+ _TEXT_NOT_NULL + _COMMA
			+ V_CATEGORY_ID 			+ _INT_NOT_NULL + _COMMA
			+ V_DISPLAY_ORDER 	    	+ _INT_NOT_NULL + _CLOSE;

	static final String VIDEOS_CREATE =
			CREATE_TABLE_IF_NOT_EXISTS + VIDEOS_TABLE + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
			+ V_NAME 					+ _TEXT_NOT_NULL + _COMMA
			+ V_DESCRIPTION 			+ _TEXT_NOT_NULL + _COMMA
			+ V_CATEGORY 				+ _TEXT_NOT_NULL + _COMMA
			+ V_SKILL_LEVEL 			+ _TEXT_NOT_NULL + _COMMA
			+ V_ECO_NAME 				+ _TEXT_NOT_NULL + _COMMA
			+ V_MINUTES 				+ _INT_NOT_NULL + _COMMA
			+ V_CREATE_DATE 	    	+ _LONG_NOT_NULL + _COMMA
			+ V_MOBILE_URL 	    		+ _TEXT_NOT_NULL + _COMMA
			+ V_KEY_FEN 	    		+ _TEXT_NOT_NULL + _COMMA
			+ V_FIRST_NAME 	    		+ _TEXT_NOT_NULL + _COMMA
			+ V_LAST_NAME 	    		+ _TEXT_NOT_NULL + _COMMA
			+ V_CHESS_TITLE 	    	+ _TEXT_NOT_NULL + _CLOSE;

	static final String VIDEO_CATEGORIES_CREATE =
			CREATE_TABLE_IF_NOT_EXISTS + VIDEO_CATEGORIES_TABLE + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
			+ V_NAME 					+ _TEXT_NOT_NULL + _COMMA
			+ V_CATEGORY_ID 			+ _INT_NOT_NULL + _COMMA
			+ V_DISPLAY_ORDER 	    	+ _INT_NOT_NULL + _CLOSE;


}
