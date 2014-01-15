package com.chess.statics;

public class AppConstants {
	public static final String FACEBOOK_APP_ID = "2427617054";
	public static final String BUGSENSE_API_KEY = "189b9851";
	public static final String YOUTUBE_DEVELOPER_KEY = "AIzaSyDunSUhieh0QurCov5Pl0nwJUpURTYiEsw";
	public static final int DEFAULT_COMP_LEVEL = 2;

	/*Screen Features*/
	public static final String SMALL_SCREEN = "small_screen";

	public static final String GAME_MODE = "game_mode";

	public static final String USER_TOKEN = "user_token_v3";
	public static final String USER_TOKEN_SAVE_TIME = "user_token_v3_save_time";
	public static final String IS_LIVE_CHESS_ON = "is_live_chess_mode_on";
	public static final String IS_LIVE_CONNECTED = "is_live_connected";
	public static final String CHALLENGE_INITIAL_TIME = "initial_time";
	public static final String CHALLENGE_BONUS_TIME = "bonus_time";
	public static final String CHALLENGE_MIN_RATING = "min_rating";
	public static final String CHALLENGE_MAX_RATING = "max_rating";
	public static final String SAVED_COMPUTER_GAME = "saving";
	public static final int CHALLENGE_ISSUE_DELAY = 2000;
	public static final int COMPUTER_THINK_TIME = 500;
	static final String LIVE_CONNECT_ATTEMPTS = "live_connect_attempts";

	/* Online games*/
	public static final String USER_OFFERED_DRAW_FOR_GAME = "user offered draw for game";

	public static final String FIRST_NAME = "first_name";
	public static final String LAST_NAME = "last_name";
	public static final String LOCATION = "location";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String FACEBOOK_TOKEN = "facebook_token";
	public static final String GUEST_NAME = "guest user";
	public static final String LIVE_SESSION_ID = "live_session_id";
	public static final String LIVE_SESSION_ID_SAVE_TIME = "live_session_id_save_time"; // used to detect if it is expired
//	public static final String API_VERSION = "api_version";
	public static final String PREF_COMPUTER_DELAY = "computer think delay";
	public static final String PREF_COMPUTER_MODE = "vs computer game mode";
	public static final String PREF_AUTO_FLIP = "auto flip for 2 players mode";
	public static final String PREF_ACTION_AFTER_MY_MOVE = "aim";
	public static final String USER_PREMIUM_STATUS = "premium_status";
	public static final String USER_PREMIUM_SKU = "premium_sku";
	public static final String USER_INFO_SAVED = "user_info_saved";
	public static final String USER_CREATE_DATE = "user_create_date";
	public static final String USER_SAW_HELP_FOR_PULL_TO_UPDATE = "user_saw_help_for_pull_to_update";
	public static final String USER_SAW_HELP_FOR_QUICK_SCROLL = "user_saw_help_for_quick_scroll_notations";
	public static final String USER_SAW_COURSE_COMPLETED_POPUP = "user_saw_course_completed_popup";
	public static final String FIRST_INIT_FINISHED = "first_init_finished";
	public static final String DEVICE_ID = "device_id"; // sometimes getDeviceId gives empty string

	/* Board and Game Preferences */
	public static final String PREF_SOUNDS = "enable sounds_"; // change field name to avoid class cast exceptions
	public static final String PREF_SHOW_SUBMIT_MOVE_LIVE = "show submit move live";
	public static final String PREF_AUTO_QUEEN_FOR_LIVE = "use auto queen promotion for live";
	public static final String PREF_SHOW_SUBMIT_MOVE_DAILY = "show submit button daily";
	public static final String PREF_SHOW_TIMER_IN_TACTICS = "show_timer_in_tactics";
	public static final String PREF_DAILY_NOTIFICATIONS = "use daily notifications";
	public static final String PREF_DAILY_MINI_BOARDS = "use_mini_boards";
	public static final String PREF_ON_VACATION = "user on vacation";
	public static final String PREF_SHOW_LEGAL_MOVES = "show possible move highlights";
	public static final String PREF_BOARD_COORDINATES = "show coordinates";
	public static final String PREF_BOARD_HIGHLIGHT_LAST_MOVE = "highlight last move";
	public static final String PREF_BOARD_SHOW_ANSWER_BOTTOM = "show answer bottom";

	public static final String PREF_COMPUTER_LEVEL = "computer strength lvl";
	public static final String PREF_BACKGROUND_SET = "background set";
	public static final String PREF_SOUNDS_SET = "sounds set";
	public static final String PREF_COLORS_SET = "colors set";
	public static final String PREF_COORDINATES_SET = "colors set";
	public static final String PREF_LANGUAGE = "application language";

	public static final String PREF_USER_SKILL_LEVEL = "user_skill_level";
	public static final String PREF_USER_SKILL_LEVEL_SET = "user_skill_level_was_set";
	public static final String PREF_USER_COUNTRY = "user_country";
	public static final String PREF_USER_COUNTRY_ID = "user_country_id";
	public static final String PREF_USER_ID = "user_id";
	public static final String PREF_USER_AVATAR_URL = "user_avatar_url";
	public static final String PREF_USER_TACTICS_RATING = "user_tactics_rating";
	public static final String PREF_USER_LESSONS_RATING = "user_lessons_rating";
	public static final String PREF_USER_DAILY_RATING = "user_daily_rating";
	public static final String PREF_USER_LESSONS_COMPLETE = "user_lessons_complete";
	public static final String PREF_USER_COURSE_COMPLETE = "user_course_complete";

    public static final String PREF_THEME_BACK_ID = "theme_background_id";
    public static final String PREF_THEME_BACKGROUND_PATH_PORT = "theme_background_path_port";
    public static final String PREF_THEME_BACKGROUND_PATH_LAND = "theme_background_path_land";
    public static final String PREF_THEME_FONT_COLOR = "theme_font_color";
    public static final String PREF_THEME_BOARD_PATH = "theme_board_path";
    public static final String PREF_THEME_BOARD_COORDINATE_LIGHT = "theme_board_coordinate_light";
    public static final String PREF_THEME_BOARD_COORDINATE_DARK = "theme_board_coordinate_dark";
    public static final String PREF_THEME_BOARD_HIGHLIGHT = "theme_board_highlight";
	public static final String PREF_THEME_PIECES_PATH = "theme_pieces_path";
	public static final String PREF_THEME_PIECES_NAME = "theme_pieces_name";
	public static final String PREF_THEME_BOARD_NAME = "theme_board_name";
	public static final String PREF_THEME_BOARD_ID = "theme_board_id";
	public static final String PREF_THEME_PIECES_PREVIEW = "theme_pieces_preview_url";
	public static final String PREF_THEME_BOARD_PREVIEW = "theme_board_preview_url";
	public static final String PREF_THEME_BACKGROUND_PREVIEW = "theme_background_preview_url";
	public static final String PREF_THEME_PIECES_ID = "theme_pieces_id";
	public static final String PREF_DEFAULT_PIECES_ID = "default_pieces_id";
	public static final String PREF_THEME_BOARD_USE_THEME = "is_use_theme_board";
	public static final String PREF_THEME_PIECES_USE_THEME = "is_use_theme_pieces";
	public static final String PREF_THEME_SOUNDS_PATH = "theme_sounds_path";
	public static final String PREF_THEME_SOUNDS_ID = "theme_sounds_id";
	public static final String PREF_THEME_IS_PIECES_3D_PATH = "theme_is_pieces_3d";
	public static final String PREF_THEME_BACKGROUNDS_LOADED = "theme_backgrounds_loaded";
	public static final String PREF_THEME_BOARDS_LOADED = "theme_boards_loaded";
	public static final String PREF_THEME_PIECES_LOADED = "theme_pieces_loaded";
	public static final String PREF_THEME_SOUNDS_LOADED = "theme_sounds_loaded";
    public static final String PREF_THEME_NAME = "theme_name";
    public static final String PREF_THEME_BACKGROUND_NAME = "theme_background_name";
	public static final String CUSTOM_THEME_NAME = "Custom";
	public static final String DEFAULT_THEME_NAME = "Game Room";
	public static final String DEFAULT_THEME_PIECES_NAME = "game_room";
	public static final String DEFAULT_THEME_BOARD_NAME = "Dark Wood";

	public static final String PREF_DEMO_TACTICS_LOADED = "demo tactics loaded";
	public static final String PREF_TEMP_TOKEN_GCM = "temporary token for gcm";
    public static final String PREF_USER_CHOOSE_VIDEO_LIBRARY = "user choose video library mode";
    public static final String PREF_USER_CHOOSE_LESSONS_LIBRARY = "user choose lesson library mode";
    public static final String USER_ASKED_FOR_FEEDBACK = "user was asked for feedback";
    public static final String LAST_TIME_ASKED_FOR_FEEDBACK = "last time user was asked for feedback";

	public static final String PULL_TO_REFRESH_HEADER_TOP_INSET = "pull_to_refresh_header_top_inset";

	/* New Daily and Live games*/
	public static final String PREF_LAST_USED_DAILY_MODE = "last_used_daily_mode";
	public static final String PREF_DEFAULT_DAILY_MODE = "defaultDailyMode";
	public static final String PREF_DEFAULT_LIVE_MODE = "defaultLiveMode";
	public static final String AD_BANNER = "Banner";
	public static final String AD_RECTANGLE = "Rectangle";
	public static final String AD_FULLSCREEN = "Fullscreen";

	public static final String FULLSCREEN_AD_ALREADY_SHOWED = "fullscreen_ad_showed";
	public static final String FIRST_TIME_START = "first_time_start";
	public static final String START_DAY = "start_day";
//	public static final String LAST_ACTIVITY_PAUSED_TIME = "last_activity_aause_time";
	public static final String ADS_SHOW_COUNTER = "ads_show_counter";
	public static final String MATOMY_AD = "matomy";
	public static final String RESPONSE = "response";
	public static final int UPGRADE_SHOW_COUNTER = 10;
	public static final String WELCOME_GAME_WON = "welcome_game_result_won";
	public static final String LESSONS_LIMIT_HAS_REACHED = "lessons_limit_has_been_reached";

	public static final String ID = "id";
	public static final String EXTRA_WEB_URL = "extras weblink url";
	public static final String EXTRA_TITLE = "screen title";
	public static final String VIDEO_LINK = "video_link";

	/* Live Chess params */
	public static final String LIVE_GAME_CONFIG_BUILDER = "live_game_config_builder";
	public static final String DAILY_GAME_CONFIG_BUILDER = "daily_game_config_builder";
	public static final String FAIR_POLICY_LINK = "http://support.chess.com/Knowledgebase/Article/View/171/6/fair-play-policy";

	public final static int GAME_MODE_COMPUTER_VS_PLAYER_WHITE = 0;
	public final static int GAME_MODE_COMPUTER_VS_PLAYER_BLACK = 1;
	public final static int GAME_MODE_2_PLAYERS = 2;
	public final static int GAME_MODE_COMPUTER_VS_COMPUTER = 3;
	public final static int GAME_MODE_VIEW_FINISHED_ECHESS = 5;

	public static final String GAME_LISTENER_IGNORE_OLD_GAME_ID = "GAME LISTENER: ignore old game id=";

	public final static String RANDOM = "Random";

	/* Messages */
	public static final String WARNING = ", warning: ";
	public static final String CHALLENGE = ", challenge: ";
	public static final String LISTENER = ": listener=";

	/* Stuff */
	public static final String FEEDBACK_SUBJECT = "Chess App Feedback - Android";
	public static final String FEEDBACK_EMAIL = "feedback@chess.com";
	public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
	public static final String MIME_TYPE_MESSAGE_RFC822 = "message/rfc822";

    public static final String CURRENT_LOCALE = "current locale of screen";

	public static final String BUGSENSE_DEBUG_APP_API_REQUEST = "APP_API_REQUEST";
	public static final String BUGSENSE_DEBUG_APP_API_RESPONSE = "APP_API_RESPONSE";
	public static final String BUGSENSE_DEBUG_GAME_ONLINE_ITEM = "GAME_ONLINE_ITEM";

	public static final String EXCEPTION = "exception";

	/*Email Feedback*/
	public static final String OS_VERSION = "OS Version: ";
	public static final String DEVICE = "Device: ";
	public static final String APP_VERSION = "App Version: ";
	public static final String USERNAME_ = "Username: ";
	public static final String VERSION_CODE = "versionCode ";
	public static final String VERSION_NAME = "versionName ";
	public static final String SDK_API = "Android API ";

	/* GCM */
	public static final String GCM_RETRY_TIME = "GCM retry time";
	public static final String GCM_REGISTERED_ON_SERVER = "registered on chess GCM server";
	public static final String GCM_SAVED_TOKEN = "saved token on server";

	public static final String NOTIFICATION = "notification";

    public static final String NEED_TO_RESTART = "needToRestart";

	/**
	 * 4 hrs the same as login token, but it might be changed if there was a login from another device
	 */
	public static final long LIVE_SESSION_EXPIRE_TIME = 4 * 60 * 60 * 1000;
	/**
	 * 4 hrs on server
	 */
	public static final long USER_TOKEN_EXPIRE_TIME = 4 * 60 * 60 * 1000;
	public static final long TIME_FOR_APP_REVIEW = 7 * 86400 * 1000; // 7 days
}
