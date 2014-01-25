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

	public static final String CLEAR_CHAT_NOTIFICATION = "clear_chat_notification";

	public static final String SAVED_STATE = "saved_state";
	public static final String REQUEST_CODE = "pending_intent_request_code";
	public static final String NAVIGATION_CMD = "navigation_command";

	public static final int NAV_FINISH_2_LOGIN = 55;
	public static final int NAV_FINISH_2_SPLASH = 66;
	public static final long WAKE_SCREEN_TIMEOUT = 2 * 60 * 1000;
//	public static final long WAKE_SCREEN_TIMEOUT = 20*1000;

	/* Notification requests codes */
	public static final int MOVE_REQUEST_CODE = 22;
	//	public static final String SHP_USER_LAST_MOVE_UPDATE_TIME = "user_last_saw_your_move_time";
	public static final String SHARED_DATA_NAME = "sharedData";

	/* After move actions */
	public static final int AFTER_MOVE_GO_TO_NEXT_GAME = 0;
	public static final int AFTER_MOVE_STAY_ON_SAME_GAME = 1;
	public static final int AFTER_MOVE_RETURN_TO_GAME_LIST = 2;

	/* Alarm notification update interval */
	public static final long REMIND_ALARM_INTERVAL = 5 * 60 * 1000; // 5 minutes
	//	public static final long REMIND_ALARM_INTERVAL = 10*1000; // 10 sec
	public static final int YOUR_MOVE_UPDATE_ID = 33;

	public static final int B_WOOD_DARK_ID = 0; // TODO hide to resources
	public static final int B_WOOD_LIGHT_ID = 1;
	public static final int B_BLUE_ID = 2;
	public static final int B_BROWN_ID = 3;
	public static final int B_GREEN_ID = 4;
	public static final int B_GREY_ID = 5;
	public static final int B_MARBLE_ID = 6;
	public static final int B_RED_ID = 7;
	public static final int B_TAN_ID = 8;

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
	public static final boolean USE_TABLETS = true;
	public static final String GIF = ".gif";
}
