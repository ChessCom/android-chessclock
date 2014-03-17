package com.chess.statics;

/**
 * StaticData class
 *
 * @author alien_roger
 * @created at: 20.03.12 5:36
 */
public class StaticData {

	public static final boolean USE_SWITCH_API = false;
//	public static final boolean USE_SWITCH_API = com.chess.BuildConfig.DEBUG;

	/*Result constants*/
	public static final int NO_NETWORK = -4;
	public static final int VALUE_NOT_EXIST = -3;
	public static final int TASK_CANCELED = -2;
	public static final int UNKNOWN_ERROR = -1;
	public static final int RESULT_OK = 0;
	public static final int EMPTY_DATA = 1;
	public static final int DATA_EXIST = 2;
	public static final int MAX_REACHED = 3;
	public static final int INTERNAL_ERROR = 15; // used in combination with server int error codes
	public static final int ILLEGAL_MOVE = 16;


	public static final long WAKE_SCREEN_TIMEOUT = 2 * 60 * 1000;

	/* Notification requests codes */
	public static final int MOVE_REQUEST_CODE = 22;
	//	public static final String SHP_USER_LAST_MOVE_UPDATE_TIME = "user_last_saw_your_move_time";
	public static final String SHARED_DATA_NAME = "sharedData";

	/* After move actions */
	public static final int AFTER_MOVE_GO_TO_NEXT_GAME = 0;
	public static final int AFTER_MOVE_STAY_ON_SAME_GAME = 1;
	public static final int AFTER_MOVE_RETURN_TO_GAME_LIST = 2;

	/* User types */
	public static final int BASIC_USER = 0;
	public static final int GOLD_USER = 1;
	public static final int PLATINUM_USER = 2;
	public static final int DIAMOND_USER = 3;

	/* Membership levels */
//	public static final int GUEST_LEVEL = 0;
//	public static final int CHEATER_LEVEL = 5;
//	public static final int BASIC_LEVEL = 10;
//	public static final int SILVER_LEVEL = 20;
//	public static final int GOLD_LEVEL = 30;
//	public static final int PLATINUM_LEVEL = 40;
//	public static final int DIAMOND_LEVEL = 50;
//	public static final int STAFF_LEVEL = 90;

	public static final String ALL = "All";
	public static final String LOCALE_EN = "en";
	public static final String GIF = ".gif";
	public static String USERNAME = "guest";

	public static final int CURRICULUM_VIDEOS_CATEGORY_ID = 99999;
	public static final int CURRICULUM_LESSONS_CATEGORY_ID = 99998;
}
