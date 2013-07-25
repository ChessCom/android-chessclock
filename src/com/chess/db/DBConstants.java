package com.chess.db;

import android.net.Uri;

/**
 * @author alien_roger
 * @created 27.10.12
 * @modified 27.10.12
 */
public class DBConstants {

    static final int DATABASE_VERSION = 40;  // change version on every DB scheme changes


	public static final String PROVIDER_NAME = "com.chess.db_provider";

	public static final String CONTENT_PATH = "content://";
	public static final String SLASH = "/";

	/*
	 * DB table names
	 */
    static final String DATABASE_NAME  = "Chess DB";

	public enum Tables {
		TACTICS_BATCH,
		TACTICS_RESULTS,
		DAILY_FINISHED_GAMES,
		DAILY_CURRENT_GAMES,
		FRIENDS,

		ARTICLES,
		ARTICLE_CATEGORIES,
		VIDEOS,
		VIDEO_CATEGORIES,

		USER_STATS_LIVE_STANDARD,
		USER_STATS_LIVE_BLITZ,
		USER_STATS_LIVE_LIGHTNING,
		USER_STATS_DAILY_CHESS,
		USER_STATS_DAILY_CHESS960,
		USER_STATS_TACTICS,
		USER_STATS_LESSONS,

		GAME_STATS_LIVE_STANDARD,
		GAME_STATS_LIVE_BLITZ,
		GAME_STATS_LIVE_LIGHTNING,
		GAME_STATS_DAILY_CHESS,
		GAME_STATS_DAILY_CHESS960,

		VIDEO_VIEWED,

		FORUM_TOPICS,
		FORUM_CATEGORIES,
		FORUM_POSTS,

		LESSONS_CATEGORIES,
		LESSONS_COURSES
	}

	// Content URI
	public static final Uri[] uriArray = new Uri[Tables.values().length];
	String[] createTablesArray;

	DBConstants() {
		createTablesArray = new String[Tables.values().length];
	}

	static {
		for (int i = 0; i < Tables.values().length; i++) {
			String table = Tables.values()[i].name();
			uriArray[i] = Uri.parse(CONTENT_PATH + PROVIDER_NAME + SLASH + table);
		}
	}

    // general fields
    public static final String _ID = "_id";

    /* TacticsItem Fields */

    public static final String V_USER     		= "user";
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
	public static final String V_I_PLAY_AS 				= "i_play_as";
	public static final String V_GAME_TYPE 				= "game_type";
	public static final String V_GAME_NAME 				= "game_name";
	public static final String V_WHITE_USERNAME 		= "white_username";
	public static final String V_BLACK_USERNAME 		= "black_username";
	public static final String V_FEN_START_POSITION 	= "fen_start_position";
	public static final String V_WHITE_RATING 			= "white_rating";
	public static final String V_BLACK_RATING 			= "black_rating";
	public static final String V_WHITE_AVATAR 			= "white_avatar";
	public static final String V_BLACK_AVATAR 			= "black_avatar";
	public static final String V_WHITE_PREMIUM_STATUS	= "white_premium_status";
	public static final String V_BLACK_PREMIUM_STATUS	= "black_premium_status";
	public static final String V_HAS_NEW_MESSAGE 		= "has_new_message";
	public static final String V_IS_OPPONENT_ONLINE		= "is_opponent_online";
	public static final String V_RATED 					= "rated";
	public static final String V_DAYS_PER_MOVE 			= "days_per_move";
	public static final String V_OPPONENT_OFFERED_DRAW 	= "opponent_offered_draw";

	public static final String V_TIME_REMAINING 		= "time_remaining";
	public static final String V_TIMESTAMP 				= "timestamp";
	public static final String V_LAST_MOVE_FROM_SQUARE 	= "last_move_from_square";
	public static final String V_LAST_MOVE_TO_SQUARE 	= "last_move_to_square";
	public static final String V_GAME_SCORE 			= "game_score";
	public static final String V_RESULT_MESSAGE 		= "result_message";
	public static final String V_IS_MY_TURN 			= "is_my_turn";

	/*Friends*/
	public static final String V_USERNAME 				= "username";
	public static final String V_USER_ID 				= "user_id";
	public static final String V_LOCATION 				= "location";
	public static final String V_COUNTRY_ID 			= "country_id";
	public static final String V_PHOTO_URL 				= "photo_url";
	public static final String V_PREMIUM_STATUS			= "premium_status";
	public static final String V_LAST_LOGIN_DATE		= "last_login_date";

	/*Articles*/
	public static final String V_TITLE 					= "title";
	public static final String V_CREATE_DATE 			= "create_date";
	public static final String V_CATEGORY 				= "category";
	public static final String V_CATEGORY_ID 			= "category_id";
	public static final String V_CHESS_TITLE 			= "chess_title";
	public static final String V_FIRST_NAME 			= "first_name";
	public static final String V_LAST_NAME 				= "last_name";
	public static final String V_THUMB_CONTENT 			= "thumb_in_content";

	/*Articles Categories*/
	public static final String V_ID 				= "id";
	public static final String V_NAME 				= "name";
	public static final String V_DISPLAY_ORDER 		= "display_order";

	/*Videos*/
	public static final String V_DESCRIPTION 		= "description";
	public static final String V_SKILL_LEVEL 		= "skill_level";
	public static final String V_MINUTES 			= "minutes";
	public static final String V_URL 				= "url";
	public static final String V_KEY_FEN 			= "key_fen";
	public static final String V_USER_AVATAR 		= "user_avatar";
	public static final String V_VIEW_COUNT 		= "view_count";
	public static final String V_COMMENT_COUNT 		= "comment_count";
	public static final String V_VIDEO_VIEWED 		= "video_viewed";

	/* Forums */
	public static final String V_LAST_POST_USERNAME = "last_post_username";
	public static final String V_LAST_POST_DATE = "last_post_date";
	public static final String V_POST_COUNT 		= "post_count";
	public static final String V_TOPIC_COUNT 		= "topic_count";
	public static final String V_MIN_MEMBERSHIP 	= "min_membership_lvl";
	public static final String V_COMMENT_NUMBER 	= "comment_number";
	public static final String V_PAGE 				= "page";

	/* Lessons*/
	public static final String V_COURSE_COMPLETED 	= "course_completed";
	public static final String V_LESSON_COMPLETED 	= "lesson_completed";

	/* common commands */
    private static final String CREATE_TABLE_IF_NOT_EXISTS = "create table if not exists ";

    private static final String _INT_NOT_NULL 		= " INT not null";
    private static final String _LONG_NOT_NULL 		= " LONG not null";
    private static final String _TEXT_NOT_NULL 		= " TEXT not null";
    private static final String _COMMA 				= ",";
    private static final String _CLOSE 				= ");";
    private static final String _SPACE 				= " ";
    private static final String ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT = " (_id integer primary key autoincrement, ";

	void createMainTables() {

		createTablesArray[Tables.TACTICS_BATCH.ordinal()] = createTableForName(Tables.TACTICS_BATCH)
				+ addField_Text(V_USER)
				+ addField_Long(V_ID)
				+ addField_Text(V_FEN)
				+ addField_Text(V_MOVE_LIST)
				+ addField_Int(V_ATTEMPT_CNT)
				+ addField_Int(V_PASSED_CNT)
				+ addField_Int(V_RATING)
				+ addField_Int(V_STOP)
				+ addField_Int(V_WAS_SHOWED)
				+ addField_Int(V_IS_RETRY)
				+ addField_Long(V_SECONDS_SPENT)
				+ addField_Text(V_AVG_SECONDS, true);

		createTablesArray[Tables.TACTICS_RESULTS.ordinal()] = createTableForName(Tables.TACTICS_RESULTS)
				+ addField_Text(V_USER)
				+ addField_Long(V_ID)
				+ addField_Text(V_SCORE)
				+ addField_Int(V_USER_RATING_CHANGE)
				+ addField_Int(V_USER_RATING)
				+ addField_Int(V_PROBLEM_RATING_CHANGE)
				+ addField_Int(V_PROBLEM_RATING, true);

		/* Daily Games */
		createTablesArray[Tables.DAILY_CURRENT_GAMES.ordinal()] = createTableForName(Tables.DAILY_CURRENT_GAMES)
				+ addField_Text(V_USER)
				+ addField_Long(V_ID)
				+ addField_Int(V_FINISHED)
				+ addField_Int(V_RATED)
				+ addField_Int(V_I_PLAY_AS)
				+ addField_Int(V_GAME_TYPE)
				+ addField_Int(V_DAYS_PER_MOVE)
				+ addField_Text(V_FEN)
				+ addField_Long(V_TIMESTAMP)
				+ addField_Text(V_GAME_NAME)
				+ addField_Text(V_LAST_MOVE_FROM_SQUARE)
				+ addField_Text(V_LAST_MOVE_TO_SQUARE)
				+ addField_Int(V_IS_OPPONENT_ONLINE)
				+ addField_Int(V_HAS_NEW_MESSAGE)
				+ addField_Text(V_WHITE_USERNAME)
				+ addField_Text(V_BLACK_USERNAME)
				+ addField_Int(V_WHITE_RATING)
				+ addField_Int(V_BLACK_RATING)
				+ addField_Int(V_WHITE_PREMIUM_STATUS)
				+ addField_Int(V_BLACK_PREMIUM_STATUS)
				+ addField_Text(V_WHITE_AVATAR)
				+ addField_Text(V_BLACK_AVATAR)
				+ addField_Int(V_TIME_REMAINING)
				+ addField_Text(V_FEN_START_POSITION)
				+ addField_Text(V_MOVE_LIST)
				+ addField_Int(V_OPPONENT_OFFERED_DRAW)
				+ addField_Int(V_IS_MY_TURN, true);

		createTablesArray[Tables.DAILY_FINISHED_GAMES.ordinal()] = createTableForName(Tables.DAILY_FINISHED_GAMES)
				+ addField_Text(V_USER)
				+ addField_Long(V_ID)
				+ addField_Int(V_FINISHED)
				+ addField_Int(V_RATED)
				+ addField_Int(V_I_PLAY_AS)
				+ addField_Int(V_GAME_TYPE)
				+ addField_Int(V_DAYS_PER_MOVE)
				+ addField_Text(V_FEN)
				+ addField_Long(V_TIMESTAMP)
				+ addField_Text(V_GAME_NAME)
				+ addField_Text(V_LAST_MOVE_FROM_SQUARE)
				+ addField_Text(V_LAST_MOVE_TO_SQUARE)
				+ addField_Int(V_IS_OPPONENT_ONLINE)
				+ addField_Int(V_HAS_NEW_MESSAGE)
				+ addField_Text(V_WHITE_USERNAME)
				+ addField_Text(V_BLACK_USERNAME)
				+ addField_Text(V_WHITE_AVATAR)
				+ addField_Text(V_BLACK_AVATAR)
				+ addField_Int(V_WHITE_RATING)
				+ addField_Int(V_BLACK_RATING)
				+ addField_Int(V_WHITE_PREMIUM_STATUS)
				+ addField_Int(V_BLACK_PREMIUM_STATUS)
				+ addField_Int(V_TIME_REMAINING)
				+ addField_Text(V_FEN_START_POSITION)
				+ addField_Text(V_MOVE_LIST)
				+ addField_Text(V_RESULT_MESSAGE)
				+ addField_Int(V_GAME_SCORE, true);

		createTablesArray[Tables.FRIENDS.ordinal()] = createTableForName(Tables.FRIENDS)
				+ addField_Text(V_USER)
				+ addField_Text(V_USERNAME)
				+ addField_Text(V_LOCATION)
				+ addField_Int(V_COUNTRY_ID)
				+ addField_Int(V_PREMIUM_STATUS)
				+ addField_Int(V_IS_OPPONENT_ONLINE)
				+ addField_Long(V_LAST_LOGIN_DATE)
				+ addField_Int(V_USER_ID)
				+ addField_Text(V_PHOTO_URL, true);

		/* Articles */
		createTablesArray[Tables.ARTICLES.ordinal()] = createTableForName(Tables.ARTICLES)
				+ addField_Long(V_ID)
				+ addField_Long(V_CATEGORY_ID)
				+ addField_Long(V_USER_ID)
				+ addField_Long(V_CREATE_DATE)
				+ addField_Int(V_COUNTRY_ID)
				+ addField_Text(V_TITLE)
				+ addField_Text(V_CATEGORY)
				+ addField_Text(V_USERNAME)
				+ addField_Text(V_FIRST_NAME)
				+ addField_Text(V_LAST_NAME)
				+ addField_Text(V_USER_AVATAR)
				+ addField_Text(V_PHOTO_URL)
				+ addField_Text(V_THUMB_CONTENT)
				+ addField_Text(V_CHESS_TITLE, true);

		createTablesArray[Tables.ARTICLE_CATEGORIES.ordinal()] = createTableForName(Tables.ARTICLE_CATEGORIES)
				+ addField_Text(V_NAME)
				+ addField_Int(V_CATEGORY_ID)
				+ addField_Int(V_DISPLAY_ORDER, true);

		/* Videos */
		createTablesArray[Tables.VIDEOS.ordinal()] = createTableForName(Tables.VIDEOS)
				+ addField_Long(V_CREATE_DATE)
				+ addField_Int(V_CATEGORY_ID)
				+ addField_Int(V_ID)
				+ addField_Int(V_MINUTES)
				+ addField_Int(V_VIEW_COUNT)
				+ addField_Int(V_COUNTRY_ID)
				+ addField_Int(V_COMMENT_COUNT)
				+ addField_Text(V_TITLE)
				+ addField_Text(V_DESCRIPTION)
				+ addField_Text(V_CATEGORY)
				+ addField_Text(V_SKILL_LEVEL)
				+ addField_Text(V_USERNAME)
				+ addField_Text(V_USER_AVATAR)
				+ addField_Text(V_URL)
				+ addField_Text(V_KEY_FEN)
				+ addField_Text(V_FIRST_NAME)
				+ addField_Text(V_LAST_NAME)
				+ addField_Text(V_CHESS_TITLE, true);

		createTablesArray[Tables.VIDEO_VIEWED.ordinal()] = createTableForName(Tables.VIDEO_VIEWED)
				+ addField_Text(V_USER)
				+ addField_Int(V_ID)
				+ addField_Int(V_VIDEO_VIEWED, true);

		createTablesArray[Tables.VIDEO_CATEGORIES.ordinal()] = createTableForName(Tables.VIDEO_CATEGORIES)
				+ addField_Text(V_NAME)
				+ addField_Int(V_CATEGORY_ID)
				+ addField_Int(V_DISPLAY_ORDER, true);

		/* Forums */
		createTablesArray[Tables.FORUM_CATEGORIES.ordinal()] = createTableForName(Tables.FORUM_CATEGORIES)
				+ addField_Long(V_CREATE_DATE)
				+ addField_Long(V_LAST_POST_DATE)
				+ addField_Int(V_ID)
				+ addField_Int(V_DISPLAY_ORDER)
				+ addField_Int(V_TOPIC_COUNT)
				+ addField_Text(V_POST_COUNT)
				+ addField_Text(V_MIN_MEMBERSHIP)
				+ addField_Text(V_NAME)
				+ addField_Text(V_DESCRIPTION, true);

		createTablesArray[Tables.FORUM_TOPICS.ordinal()] = createTableForName(Tables.FORUM_TOPICS)
				+ addField_Long(V_LAST_POST_DATE)
				+ addField_Int(V_ID)
				+ addField_Int(V_CATEGORY_ID)
				+ addField_Int(V_POST_COUNT)
				+ addField_Int(V_PAGE)
				+ addField_Text(V_TITLE)
				+ addField_Text(V_CATEGORY)
				+ addField_Text(V_URL)
				+ addField_Text(V_USERNAME)
				+ addField_Text(V_LAST_POST_USERNAME, true);

		createTablesArray[Tables.FORUM_POSTS.ordinal()] = createTableForName(Tables.FORUM_POSTS)
				+ addField_Long(V_CREATE_DATE)
				+ addField_Int(V_COUNTRY_ID)
				+ addField_Int(V_PREMIUM_STATUS)
				+ addField_Int(V_COMMENT_NUMBER)
				+ addField_Int(V_PAGE)
				+ addField_Int(V_ID)
				+ addField_Text(V_USERNAME)
				+ addField_Text(V_DESCRIPTION)
				+ addField_Text(V_PHOTO_URL, true);

		/* Lessons */
		createTablesArray[Tables.LESSONS_CATEGORIES.ordinal()] = createTableForName(Tables.LESSONS_CATEGORIES)
				+ addField_Text(V_NAME)
				+ addField_Int(V_CATEGORY_ID)
				+ addField_Int(V_DISPLAY_ORDER, true);

		createTablesArray[Tables.LESSONS_COURSES.ordinal()] = createTableForName(Tables.LESSONS_COURSES)
				+ addField_Int(V_ID)
				+ addField_Int(V_CATEGORY_ID)
				+ addField_Int(V_COURSE_COMPLETED)
				+ addField_Text(V_USER)
				+ addField_Text(V_NAME, true);
	}

	/* ==================== User Stats ============================== */
	 /*Rating*/
	public static final String V_CURRENT 				= "current";
	public static final String V_HIGHEST_RATING 		= "highest_rating";
	public static final String V_HIGHEST_TIMESTAMP 		= "highest_timestamp";
	public static final String V_BEST_WIN_RATING 		= "best_win_rating";
	public static final String V_BEST_WIN_GAME_ID 		= "best_win_game_id";
	public static final String V_BEST_WIN_USERNAME 		= "best_win_username";
	public static final String V_AVERAGE_OPPONENT 		= "average_opponent";
	/*Games*/
	public static final String V_GAMES_TOTAL 			= "games_total";
	public static final String V_GAMES_WINS 			= "games_wins";
	public static final String V_GAMES_LOSSES 			= "games_losses";
	public static final String V_GAMES_DRAWS 			= "games_draws";
	/* Daily */
	public static final String V_RANK 					= "rank";
	public static final String V_TOTAL_PLAYER_COUNT 	= "total_player_count";
	public static final String V_TIMEOUTS 				= "timeouts";
	public static final String V_TIME_PER_MOVE 			= "time_per_move";

	/* Tactics */
	public static final String V_LOWEST_RATING 			= "lowest_rating";
	public static final String V_LOWEST_TIMESTAMP 		= "lowest_timestamp";
	public static final String V_ATTEMPT_COUNT 			= "attempt_count";
	public static final String V_PASSED_COUNT 			= "passed_count";
	public static final String V_FAILED_COUNT 			= "failed_count";
	public static final String V_TOTAL_SECONDS 			= "total_seconds";
	public static final String V_TODAYS_ATTEMPTS 		= "todays_attempts";
	public static final String V_TODAYS_AVG_SCORE		= "todays_average_score";

	/* Lessons*/
	public static final String V_LESSONS_TRIED 				= "lessons_tried";
	public static final String V_TOTAL_LESSON_COUNT 		= "total_lesson_count";
	public static final String V_LESSON_COMPLETE_PERCENTAGE = "lesson_complete_percentage";
	public static final String V_TOTAL_TRAINING_SECONDS 	= "total_training_seconds";

	void createUserStatsTables() {
		createTablesArray[Tables.USER_STATS_LIVE_STANDARD.ordinal()] = createTableForName(Tables.USER_STATS_LIVE_STANDARD)
				+ addField_Long(V_HIGHEST_TIMESTAMP)
				+ addField_Int(V_CURRENT)
				+ addField_Int(V_HIGHEST_RATING)
				+ addField_Int(V_BEST_WIN_RATING)
				+ addField_Int(V_AVERAGE_OPPONENT)
				+ addField_Int(V_GAMES_TOTAL)
				+ addField_Int(V_GAMES_WINS)
				+ addField_Int(V_GAMES_LOSSES)
				+ addField_Int(V_GAMES_DRAWS)
				+ addField_Text(V_USER)
				+ addField_Text(V_BEST_WIN_USERNAME, true);

		createTablesArray[Tables.USER_STATS_LIVE_LIGHTNING.ordinal()] = createTableForName(Tables.USER_STATS_LIVE_LIGHTNING)
				+ addField_Long(V_HIGHEST_TIMESTAMP)
				+ addField_Int(V_CURRENT)
				+ addField_Int(V_HIGHEST_RATING)
				+ addField_Int(V_BEST_WIN_RATING)
				+ addField_Int(V_AVERAGE_OPPONENT)
				+ addField_Int(V_GAMES_TOTAL)
				+ addField_Int(V_GAMES_WINS)
				+ addField_Int(V_GAMES_LOSSES)
				+ addField_Int(V_GAMES_DRAWS)
				+ addField_Text(V_USER)
				+ addField_Text(V_BEST_WIN_USERNAME, true);

		createTablesArray[Tables.USER_STATS_LIVE_BLITZ.ordinal()] = createTableForName(Tables.USER_STATS_LIVE_BLITZ)
				+ addField_Long(V_HIGHEST_TIMESTAMP)
				+ addField_Int(V_CURRENT)
				+ addField_Int(V_HIGHEST_RATING)
				+ addField_Int(V_BEST_WIN_RATING)
				+ addField_Int(V_AVERAGE_OPPONENT)
				+ addField_Int(V_GAMES_TOTAL)
				+ addField_Int(V_GAMES_WINS)
				+ addField_Int(V_GAMES_LOSSES)
				+ addField_Int(V_GAMES_DRAWS)
				+ addField_Text(V_USER)
				+ addField_Text(V_BEST_WIN_USERNAME, true);

		createTablesArray[Tables.USER_STATS_DAILY_CHESS.ordinal()] = createTableForName(Tables.USER_STATS_DAILY_CHESS)
				+ addField_Long(V_HIGHEST_TIMESTAMP)
				+ addField_Long(V_BEST_WIN_GAME_ID)
				+ addField_Long(V_TIME_PER_MOVE)
				+ addField_Int(V_CURRENT)
				+ addField_Int(V_HIGHEST_RATING)
				+ addField_Int(V_BEST_WIN_RATING)
				+ addField_Int(V_AVERAGE_OPPONENT)
				+ addField_Int(V_TOTAL_PLAYER_COUNT)
				+ addField_Int(V_GAMES_TOTAL)
				+ addField_Int(V_GAMES_WINS)
				+ addField_Int(V_GAMES_LOSSES)
				+ addField_Int(V_GAMES_DRAWS)
				+ addField_Text(V_USER)
				+ addField_Text(V_BEST_WIN_USERNAME)
				+ addField_Text(V_RANK)
				+ addField_Text(V_TIMEOUTS, true);

		createTablesArray[Tables.USER_STATS_DAILY_CHESS960.ordinal()] = createTableForName(Tables.USER_STATS_DAILY_CHESS960)
				+ addField_Long(V_HIGHEST_TIMESTAMP)
				+ addField_Long(V_BEST_WIN_GAME_ID)
				+ addField_Long(V_TIME_PER_MOVE)
				+ addField_Int(V_CURRENT)
				+ addField_Int(V_HIGHEST_RATING)
				+ addField_Int(V_BEST_WIN_RATING)
				+ addField_Int(V_AVERAGE_OPPONENT)
				+ addField_Int(V_TOTAL_PLAYER_COUNT)
				+ addField_Int(V_GAMES_TOTAL)
				+ addField_Int(V_GAMES_WINS)
				+ addField_Int(V_GAMES_LOSSES)
				+ addField_Int(V_GAMES_DRAWS)
				+ addField_Text(V_USER)
				+ addField_Text(V_BEST_WIN_USERNAME)
				+ addField_Text(V_RANK)
				+ addField_Text(V_TIMEOUTS, true);

		createTablesArray[Tables.USER_STATS_TACTICS.ordinal()] = createTableForName(Tables.USER_STATS_TACTICS)
				+ addField_Long(V_HIGHEST_TIMESTAMP)
				+ addField_Long(V_LOWEST_TIMESTAMP)
				+ addField_Int(V_CURRENT)
				+ addField_Int(V_HIGHEST_RATING)
				+ addField_Int(V_LOWEST_RATING)
				+ addField_Int(V_ATTEMPT_COUNT)
				+ addField_Int(V_PASSED_COUNT)
				+ addField_Int(V_FAILED_COUNT)
				+ addField_Int(V_TODAYS_ATTEMPTS)
				+ addField_Int(V_TODAYS_AVG_SCORE)
				+ addField_Int(V_TOTAL_SECONDS)
				+ addField_Text(V_USER, true);

		createTablesArray[Tables.USER_STATS_LESSONS.ordinal()] = createTableForName(Tables.USER_STATS_LESSONS)
				+ addField_Long(V_HIGHEST_TIMESTAMP)
				+ addField_Long(V_LOWEST_TIMESTAMP)
				+ addField_Long(V_TOTAL_TRAINING_SECONDS)
				+ addField_Int(V_CURRENT)
				+ addField_Int(V_HIGHEST_RATING)
				+ addField_Int(V_LOWEST_RATING)
				+ addField_Int(V_LESSONS_TRIED)
				+ addField_Int(V_TOTAL_LESSON_COUNT)
				+ addField_Text(V_USER)
				+ addField_Text(V_LESSON_COMPLETE_PERCENTAGE, true);


	}

	/* ==================== Game Stats ============================== */

	public static final String V_GAMES_WHITE 			= "games_white";
	public static final String V_GAMES_BLACK 			= "games_black";
	public static final String V_PERCENTILE 			= "percentile";
	public static final String V_GLICKO_RD 				= "glicko_rd";
	public static final String V_AVG_OPPONENT_RATING_WIN = "average_opponent_rating_when_i_win";
	public static final String V_AVG_OPPONENT_RATING_LOSE = "average_opponent_rating_when_i_lose";
	public static final String V_AVG_OPPONENT_RATING_DRAW = "average_opponent_rating_when_i_draw";
	public static final String V_UNRATED 				= "unrated";
	public static final String V_IN_PROGRESS 			= "in_progress";

	public static final String V_WINS_TOTAL 			= "wins_total";
	public static final String V_WINS_WHITE 			= "wins_white";
	public static final String V_WINS_BLACK 			= "wins_black";
	public static final String V_LOSSES_TOTAL 			= "losses_total";
	public static final String V_LOSSES_WHITE 			= "losses_white";
	public static final String V_LOSSES_BLACK 			= "losses_black";
	public static final String V_DRAWS_TOTAL 			= "draws_total";
	public static final String V_DRAWS_WHITE 			= "draws_white";
	public static final String V_DRAWS_BLACK 			= "draws_black";
	public static final String V_WINNING_STREAK 		= "winning_streak";
	public static final String V_LOSING_STREAK 			= "losing_streak";
	public static final String V_FREQUENT_OPPONENT_NAME 			= "frequent_opponent_name";
	public static final String V_FREQUENT_OPPONENT_GAMES_PLAYED 	= "frequent_opponent_games_played";

	public static final String V_TOURNAMENTS_LEADERBOARD_POINTS 	= "tournaments_leaderboard_points";
	public static final String V_TOURNAMENTS_EVENTS_ENTERED 		= "tournaments_events_entered";
	public static final String V_TOURNAMENTS_FIRST_PLACE_FINISHES 	= "tournaments_first_place_finishes";
	public static final String V_TOURNAMENTS_SECOND_PLACE_FINISHES 	= "tournaments_second_place_finishes";
	public static final String V_TOURNAMENTS_THIRD_PLACE_FINISHES 	= "tournaments_third_place_finishes";
	public static final String V_TOURNAMENTS_WITHDRAWALS 			= "tournaments_withdrawals";
	public static final String V_TOURNAMENTS_HOSTED 				= "tournaments_hosted";
	public static final String V_TOTAL_COUNT_PLAYERS_HOSTED 		= "total_count_players_hosted";

	public static final String V_TOURNAMENTS_GAMES_TOTAL 			= "tournaments_games_total";
	public static final String V_TOURNAMENTS_GAMES_WON 				= "tournaments_games_won";
	public static final String V_TOURNAMENTS_GAMES_LOST 			= "tournaments_games_lost";
	public static final String V_TOURNAMENTS_GAMES_DRAWN 			= "tournaments_games_drawn";
	public static final String V_TOURNAMENTS_GAMES_IN_PROGRESS 		= "tournaments_games_in_progress";

	void createGameStatsTables() {
		createTablesArray[Tables.GAME_STATS_LIVE_STANDARD.ordinal()] = createTableForName(Tables.GAME_STATS_LIVE_STANDARD)
				+ addField_Long(V_HIGHEST_TIMESTAMP)
				+ addField_Long(V_LOWEST_TIMESTAMP)
				+ addField_Long(V_BEST_WIN_GAME_ID)
				+ addField_Int(V_CURRENT)
				+ addField_Int(V_TOTAL_PLAYER_COUNT)
				+ addField_Int(V_GLICKO_RD)
				+ addField_Int(V_HIGHEST_RATING)
				+ addField_Int(V_LOWEST_RATING)
				+ addField_Int(V_AVERAGE_OPPONENT)
				+ addField_Int(V_BEST_WIN_RATING)
				+ addField_Int(V_AVG_OPPONENT_RATING_WIN)
				+ addField_Int(V_AVG_OPPONENT_RATING_LOSE)
				+ addField_Int(V_AVG_OPPONENT_RATING_DRAW)
				+ addField_Int(V_UNRATED)
				+ addField_Int(V_IN_PROGRESS)
				+ addField_Text(V_USER)
				+ addField_Text(V_RANK)
				+ addField_Text(V_PERCENTILE)
				+ addField_Text(V_BEST_WIN_USERNAME)
				+ addField_Text(V_TIMEOUTS)
				/* Games */
				+ addField_Int(V_GAMES_TOTAL)
				+ addField_Int(V_GAMES_WHITE)
				+ addField_Int(V_GAMES_BLACK)
				+ addField_Int(V_WINS_TOTAL)
				+ addField_Int(V_WINS_WHITE)
				+ addField_Int(V_WINS_BLACK)
				+ addField_Int(V_LOSSES_TOTAL)
				+ addField_Int(V_LOSSES_WHITE)
				+ addField_Int(V_LOSSES_BLACK)
				+ addField_Int(V_DRAWS_TOTAL)
				+ addField_Int(V_DRAWS_WHITE)
				+ addField_Int(V_DRAWS_BLACK)
				+ addField_Int(V_WINNING_STREAK)
				+ addField_Int(V_LOSING_STREAK)
				+ addField_Int(V_FREQUENT_OPPONENT_GAMES_PLAYED)
				+ addField_Text(V_FREQUENT_OPPONENT_NAME, true);

		createTablesArray[Tables.GAME_STATS_LIVE_BLITZ.ordinal()] = createTableForName(Tables.GAME_STATS_LIVE_BLITZ)
				+ addField_Long(V_HIGHEST_TIMESTAMP)
				+ addField_Long(V_LOWEST_TIMESTAMP)
				+ addField_Long(V_BEST_WIN_GAME_ID)
				+ addField_Int(V_CURRENT)
				+ addField_Int(V_TOTAL_PLAYER_COUNT)
				+ addField_Int(V_GLICKO_RD)
				+ addField_Int(V_HIGHEST_RATING)
				+ addField_Int(V_LOWEST_RATING)
				+ addField_Int(V_AVERAGE_OPPONENT)
				+ addField_Int(V_BEST_WIN_RATING)
				+ addField_Int(V_AVG_OPPONENT_RATING_WIN)
				+ addField_Int(V_AVG_OPPONENT_RATING_LOSE)
				+ addField_Int(V_AVG_OPPONENT_RATING_DRAW)
				+ addField_Int(V_UNRATED)
				+ addField_Int(V_IN_PROGRESS)
				+ addField_Text(V_USER)
				+ addField_Text(V_RANK)
				+ addField_Text(V_PERCENTILE)
				+ addField_Text(V_BEST_WIN_USERNAME)
				+ addField_Text(V_TIMEOUTS)
				/* Games */
				+ addField_Int(V_GAMES_TOTAL)
				+ addField_Int(V_GAMES_WHITE)
				+ addField_Int(V_GAMES_BLACK)
				+ addField_Int(V_WINS_TOTAL)
				+ addField_Int(V_WINS_WHITE)
				+ addField_Int(V_WINS_BLACK)
				+ addField_Int(V_LOSSES_TOTAL)
				+ addField_Int(V_LOSSES_WHITE)
				+ addField_Int(V_LOSSES_BLACK)
				+ addField_Int(V_DRAWS_TOTAL)
				+ addField_Int(V_DRAWS_WHITE)
				+ addField_Int(V_DRAWS_BLACK)
				+ addField_Int(V_WINNING_STREAK)
				+ addField_Int(V_LOSING_STREAK)
				+ addField_Int(V_FREQUENT_OPPONENT_GAMES_PLAYED)
				+ addField_Text(V_FREQUENT_OPPONENT_NAME, true);

		createTablesArray[Tables.GAME_STATS_LIVE_LIGHTNING.ordinal()] = createTableForName(Tables.GAME_STATS_LIVE_LIGHTNING)
				+ addField_Long(V_HIGHEST_TIMESTAMP)
				+ addField_Long(V_LOWEST_TIMESTAMP)
				+ addField_Long(V_BEST_WIN_GAME_ID)
				+ addField_Int(V_CURRENT)
				+ addField_Int(V_TOTAL_PLAYER_COUNT)
				+ addField_Int(V_GLICKO_RD)
				+ addField_Int(V_HIGHEST_RATING)
				+ addField_Int(V_LOWEST_RATING)
				+ addField_Int(V_AVERAGE_OPPONENT)
				+ addField_Int(V_BEST_WIN_RATING)
				+ addField_Int(V_AVG_OPPONENT_RATING_WIN)
				+ addField_Int(V_AVG_OPPONENT_RATING_LOSE)
				+ addField_Int(V_AVG_OPPONENT_RATING_DRAW)
				+ addField_Int(V_UNRATED)
				+ addField_Int(V_IN_PROGRESS)
				+ addField_Text(V_USER)
				+ addField_Text(V_RANK)
				+ addField_Text(V_PERCENTILE)
				+ addField_Text(V_BEST_WIN_USERNAME)
				+ addField_Text(V_TIMEOUTS)
				/* Games */
				+ addField_Int(V_GAMES_TOTAL)
				+ addField_Int(V_GAMES_WHITE)
				+ addField_Int(V_GAMES_BLACK)
				+ addField_Int(V_WINS_TOTAL)
				+ addField_Int(V_WINS_WHITE)
				+ addField_Int(V_WINS_BLACK)
				+ addField_Int(V_LOSSES_TOTAL)
				+ addField_Int(V_LOSSES_WHITE)
				+ addField_Int(V_LOSSES_BLACK)
				+ addField_Int(V_DRAWS_TOTAL)
				+ addField_Int(V_DRAWS_WHITE)
				+ addField_Int(V_DRAWS_BLACK)
				+ addField_Int(V_WINNING_STREAK)
				+ addField_Int(V_LOSING_STREAK)
				+ addField_Int(V_FREQUENT_OPPONENT_GAMES_PLAYED)
				+ addField_Text(V_FREQUENT_OPPONENT_NAME, true);

		createTablesArray[Tables.GAME_STATS_DAILY_CHESS.ordinal()] = createTableForName(Tables.GAME_STATS_DAILY_CHESS)
				+ addField_Long(V_HIGHEST_TIMESTAMP)
				+ addField_Long(V_LOWEST_TIMESTAMP)
				+ addField_Long(V_BEST_WIN_GAME_ID)
				+ addField_Int(V_CURRENT)
				+ addField_Int(V_TOTAL_PLAYER_COUNT)
				+ addField_Int(V_GLICKO_RD)
				+ addField_Int(V_HIGHEST_RATING)
				+ addField_Int(V_LOWEST_RATING)
				+ addField_Int(V_AVERAGE_OPPONENT)
				+ addField_Int(V_BEST_WIN_RATING)
				+ addField_Int(V_AVG_OPPONENT_RATING_WIN)
				+ addField_Int(V_AVG_OPPONENT_RATING_LOSE)
				+ addField_Int(V_AVG_OPPONENT_RATING_DRAW)
				+ addField_Int(V_UNRATED)
				+ addField_Int(V_IN_PROGRESS)
				+ addField_Text(V_USER)
				+ addField_Text(V_RANK)
				+ addField_Text(V_PERCENTILE)
				+ addField_Text(V_BEST_WIN_USERNAME)
				+ addField_Text(V_TIMEOUTS)
				/* Games */
				+ addField_Int(V_GAMES_TOTAL)
				+ addField_Int(V_GAMES_WHITE)
				+ addField_Int(V_GAMES_BLACK)
				+ addField_Int(V_WINS_TOTAL)
				+ addField_Int(V_WINS_WHITE)
				+ addField_Int(V_WINS_BLACK)
				+ addField_Int(V_LOSSES_TOTAL)
				+ addField_Int(V_LOSSES_WHITE)
				+ addField_Int(V_LOSSES_BLACK)
				+ addField_Int(V_DRAWS_TOTAL)
				+ addField_Int(V_DRAWS_WHITE)
				+ addField_Int(V_DRAWS_BLACK)
				+ addField_Int(V_WINNING_STREAK)
				+ addField_Int(V_LOSING_STREAK)
				+ addField_Int(V_FREQUENT_OPPONENT_GAMES_PLAYED)
				+ addField_Text(V_FREQUENT_OPPONENT_NAME)
				/* Tournaments */
				+ addField_Int(V_TOURNAMENTS_LEADERBOARD_POINTS)
				+ addField_Int(V_TOURNAMENTS_EVENTS_ENTERED)
				+ addField_Int(V_TOURNAMENTS_FIRST_PLACE_FINISHES)
				+ addField_Int(V_TOURNAMENTS_SECOND_PLACE_FINISHES)
				+ addField_Int(V_TOURNAMENTS_THIRD_PLACE_FINISHES)
				+ addField_Int(V_TOURNAMENTS_WITHDRAWALS)
				+ addField_Int(V_TOURNAMENTS_HOSTED)
				+ addField_Int(V_TOTAL_COUNT_PLAYERS_HOSTED)
				+ addField_Int(V_TOURNAMENTS_GAMES_TOTAL)
				+ addField_Int(V_TOURNAMENTS_GAMES_WON)
				+ addField_Int(V_TOURNAMENTS_GAMES_LOST)
				+ addField_Int(V_TOURNAMENTS_GAMES_DRAWN)
				+ addField_Int(V_TOURNAMENTS_GAMES_IN_PROGRESS, true);

		createTablesArray[Tables.GAME_STATS_DAILY_CHESS960.ordinal()] = createTableForName(Tables.GAME_STATS_DAILY_CHESS960)
				+ addField_Long(V_HIGHEST_TIMESTAMP)
				+ addField_Long(V_LOWEST_TIMESTAMP)
				+ addField_Long(V_BEST_WIN_GAME_ID)
				+ addField_Int(V_CURRENT)
				+ addField_Int(V_TOTAL_PLAYER_COUNT)
				+ addField_Int(V_GLICKO_RD)
				+ addField_Int(V_HIGHEST_RATING)
				+ addField_Int(V_LOWEST_RATING)
				+ addField_Int(V_AVERAGE_OPPONENT)
				+ addField_Int(V_BEST_WIN_RATING)
				+ addField_Int(V_AVG_OPPONENT_RATING_WIN)
				+ addField_Int(V_AVG_OPPONENT_RATING_LOSE)
				+ addField_Int(V_AVG_OPPONENT_RATING_DRAW)
				+ addField_Int(V_UNRATED)
				+ addField_Int(V_IN_PROGRESS)
				+ addField_Text(V_USER)
				+ addField_Text(V_RANK)
				+ addField_Text(V_PERCENTILE)
				+ addField_Text(V_BEST_WIN_USERNAME)
				+ addField_Text(V_TIMEOUTS)
				/* Games */
				+ addField_Int(V_GAMES_TOTAL)
				+ addField_Int(V_GAMES_WHITE)
				+ addField_Int(V_GAMES_BLACK)
				+ addField_Int(V_WINS_TOTAL)
				+ addField_Int(V_WINS_WHITE)
				+ addField_Int(V_WINS_BLACK)
				+ addField_Int(V_LOSSES_TOTAL)
				+ addField_Int(V_LOSSES_WHITE)
				+ addField_Int(V_LOSSES_BLACK)
				+ addField_Int(V_DRAWS_TOTAL)
				+ addField_Int(V_DRAWS_WHITE)
				+ addField_Int(V_DRAWS_BLACK)
				+ addField_Int(V_WINNING_STREAK)
				+ addField_Int(V_LOSING_STREAK)
				+ addField_Int(V_FREQUENT_OPPONENT_GAMES_PLAYED)
				+ addField_Text(V_FREQUENT_OPPONENT_NAME)
				/* Tournaments */
				+ addField_Int(V_TOURNAMENTS_LEADERBOARD_POINTS)
				+ addField_Int(V_TOURNAMENTS_EVENTS_ENTERED)
				+ addField_Int(V_TOURNAMENTS_FIRST_PLACE_FINISHES)
				+ addField_Int(V_TOURNAMENTS_SECOND_PLACE_FINISHES)
				+ addField_Int(V_TOURNAMENTS_THIRD_PLACE_FINISHES)
				+ addField_Int(V_TOURNAMENTS_WITHDRAWALS)
				+ addField_Int(V_TOURNAMENTS_HOSTED)
				+ addField_Int(V_TOTAL_COUNT_PLAYERS_HOSTED)
				+ addField_Int(V_TOURNAMENTS_GAMES_TOTAL)
				+ addField_Int(V_TOURNAMENTS_GAMES_WON)
				+ addField_Int(V_TOURNAMENTS_GAMES_LOST)
				+ addField_Int(V_TOURNAMENTS_GAMES_DRAWN)
				+ addField_Int(V_TOURNAMENTS_GAMES_IN_PROGRESS, true);
	}

	private String createTableForName(Tables  tableName){
		return CREATE_TABLE_IF_NOT_EXISTS + Tables.values()[tableName.ordinal()] + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT;
	}

	private String addField_Int(String columnName){
		return addField_Int(columnName, false);
	}

	private String addField_Int(String columnName, boolean last){
		return _SPACE + columnName + _INT_NOT_NULL + (last ? _CLOSE : _COMMA);
	}

	private String addField_Text(String columnName){
		return addField_Text(columnName, false);
	}

	private String addField_Text(String columnName, boolean last){
		return _SPACE + columnName + _TEXT_NOT_NULL + (last ? _CLOSE : _COMMA);
	}

	private String addField_Long(String columnName){
		return addFieldLong(columnName, false);
	}

	private String addFieldLong(String columnName, boolean last){
		return _SPACE + columnName + _LONG_NOT_NULL + (last ? _CLOSE : _COMMA);
	}
}
