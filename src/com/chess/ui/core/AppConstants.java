package com.chess.ui.core;

public class AppConstants {
	public static final String FACEBOOK_APP_ID = "2427617054";
	/*Sreen Features*/
	public static final String SMALL_SCREEN = "small_screen";
	// TODO split to GameType and Game Mode Constants

	public static final String ENTER_FROM_NOTIFICATION = "enter_from_notification";


	public static final String GAME_MODE = "game_mode";
	public static final String MESSAGE = "message";
	public static final String FINISHABLE = "finishable";
	public static final String LIVE_CHESS = "live_chess";
	public static final String OBJECT = "object";
	public static final String TITLE = "title";
	public static final String TAB_INDEX = "tab_index";
	public static final String VIDEO_SKILL_LEVEL = "video_skill_level";
	public static final String VIDEO_CATEGORY = "video_category";
	public static final String ENABLE_LIVE_CONNECTING_INDICATOR = "enable_live_connecting_indicator";
	//	public static final String REPEATABLE = "repeatable"; // TODO use REPEATABLE_TASK instead!
	public static final String REQUEST_RESULT = "request_result";
	public static final String CALLBACK_CODE = "callback_code";
	public static final String REPEATABLE_TASK = "repeatable_task";
	public static final String USER_TOKEN = "user_token";
	public static final String CHALLENGE_INITIAL_TIME = "initial_time";
	public static final String CHALLENGE_BONUS_TIME = "bonus_time";
	public static final String CHALLENGE_MIN_RATING = "min_rating";
	public static final String CHALLENGE_MAX_RATING = "max_rating";
	public static final String SAVED_COMPUTER_GAME = "saving";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String API_VERSION = "api_version";
	public static final String PREF_COMPUTER_STRENGTH = "strength";
	public static final String PREF_ACTION_AFTER_MY_MOVE = "aim";
	public static final String USER_PREMIUM_STATUS = "premium_status";
	public static final String PREF_SOUNDS = "enableSounds";
	public static final String PREF_SHOW_SUBMIT_MOVE_LIVE = "ssblive";
	public static final String PREF_SHOW_SUBMIT_MOVE = "ssb";
	public static final String PREF_NOTIFICATION = "notifE";
	public static final String PREF_BOARD_COORDINATES = "coords";
	public static final String PREF_BOARD_SQUARE_HIGHLIGHT = "highlights";
	public static final String PREF_BOARD_TYPE = "boardBitmap";
	public static final String PREF_PIECES_SET = "piecesBitmap";

	public static final String FULLSCREEN_AD_ALREADY_SHOWED = "fullscreen_ad_showed";
	public static final String ONLINE_GAME_LIST_TYPE = "gamestype";
	public static final String USER_SESSION_ID = "user_session_id";
	public static final String FIRST_TIME_START = "first_time_start";
	public static final String START_DAY = "start_day";
	public static final String LAST_ACTIVITY_PAUSED_TIME = "last_activity_aause_time";
	public static final String ADS_SHOW_COUNTER = "ads_show_counter";
	public static final String WHITE_USERNAME = "white_username";
	public static final String BLACK_USERNAME = "black_username";


	public static final String DESCRIPTION = "description";
	public static final String SKILL_LEVEL = "skill_level";
	public static final String OPENING = "opening";
	public static final String AUTHOR_USERNAME = "author_username";
	public static final String AUTHOR_CHESS_TITLE = "author_chess_title";
	public static final String AUTHOR_FIRST_GAME = "author_first_name";
	public static final String AUTHOR_LAST_NAME = "author_last_name";
	public static final String MINUTES = "minutes";
	public static final String PUBLISH_TIMESTAMP = "publish_timestamp";
	public static final String VIEW_URL = "view";

	public static final String SCORE = "score";
	public static final String USER_RATING_CHANGE = "user_rating_change";
	public static final String USER_RATING = "user_rating";
	public static final String PROBLEM_RATING_CHANGE = "problem_rating_change";
	public static final String PROBLEM_RATING = "problem_rating";

	public static final String ID = "id";
	public static final String FEN = "fen";
	public static final String MOVE_LIST = "move_list";
	public static final String ATTEMPT_CNT = "attempt_count";
	public static final String PASSED_CNT = "passed_count";
	public static final String RATING = "rating";
	public static final String AVG_SECONDS = "average_seconds";
	public static final String STOP = "stop";

	public final static int GAME_MODE_COMPUTER_VS_HUMAN_WHITE = 0;
	public final static int GAME_MODE_COMPUTER_VS_HUMAN_BLACK = 1;
	public final static int GAME_MODE_HUMAN_VS_HUMAN = 2;
	public final static int GAME_MODE_COMPUTER_VS_COMPUTER = 3;
	public final static int GAME_MODE_LIVE_OR_ECHESS = 4;  // TODO refactor game modes to inheritance
	public final static int GAME_MODE_VIEW_FINISHED_ECHESS = 5;
	public final static int GAME_MODE_TACTICS = 6;


	public static final String API_V2_LOGIN = "/api/v2/login";
	public static final String DEFAULT_GAMEBOARD_CASTLE = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
	public static final String REQUEST_DRAW = "Request draw: ";
	public static final String OFFERDRAW = "OFFERDRAW";
	public static final String ACCEPTDRAW = "ACCEPTDRAW";
	public static final String SUCCESS = "Success";
	public static final String API_SUBMIT_ECHESS_ACTION_ID = "/api/submit_echess_action?id=";
	public static final String ERROR_PLUS = "Error+";
	public static final String ERROR = "Error";
	public static final String LCCLOG_RESIGN_GAME_BY_FAIR_PLAY_RESTRICTION = "LCCLOG: resign game by fair play restriction: ";
	public static final String RESIGN_GAME = "Resign game: ";
	public static final String LCCLOG_RESIGN_GAME = "LCCLOG: resign game: ";
	public static final String LCCLOG_ABORT_GAME = "LCCLOG: abort game: ";
	public static final String CHESSID_PARAMETER = "&chessid=";
	public static final String COMMAND_RESIGN__AND_TIMESTAMP_PARAMETER = "&command=RESIGN&timestamp=";
	public static final String API_V3_GET_GAME_ID = "/api/v3/get_game?id=";
	public static final String COMMAND_SUBMIT_AND_NEWMOVE_PARAMETER = "&command=SUBMIT&newmove=";
	public static final String TIMESTAMP_PARAMETER = "&timestamp=";
	public static final String LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION = "LCCLOG ANDROID: receive broadcast intent, action=";
	public static final String COMMAND_PARAMETER = "&command=";
	public static final String LCCLOG_ANDROID_SEND_BROADCAST_OBJECT_INTENT_ACTION = "LCCLOG ANDROID: sendBroadcastObjectIntent action=";
	public static final String LCC_HOLDER_IS_NULL = "LccHolder is null";
	public static final String WARNING = ", warning: ";
	public static final String CHALLENGE = ", challenge: ";
	public static final String GAME_LISTENER_IGNORE_OLD_GAME_ID = "GAME LISTENER: ignore old game id=";
	public static final String LISTENER = ": listener=";
	public static final String TACTICS_ID_PARAMETER = "&tactics_id=";
	public static final String API_TACTICS_TRAINER_ID_PARAMETER = "/api/tactics_trainer?id=";
	public static final String PASSED_PARAMETER = "&passed=";
	public static final String CORRECT_MOVES_PARAMETER = "&correct_moves=";
	public static final String SECONDS_PARAMETER = "&seconds=";
	public static final String SYMBOL_SPACE = " ";
	public static final String OPPONENT = "opponent";
	public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
	public static final String MOBCLIX_EXCEPTION_IN_SHOW_GAME_END_POPUP = "MOBCLIX: EXCEPTION IN showGameEndPopup";
	public static final String URL_GET_ANDROID_VERSION = "http://www.chess.com/api/get_android_version";
	public static final String EMAIL_MOBILE_CHESS_COM = "mobile@chess.com";
	public static final String PACKAGE_NAME = "com.chess";
	public static final String API_ECHESS_OPEN_INVITES_ID = "/api/echess_open_invites?id=";
	public static final String DECLINE_INVITEID_PARAMETER = "&declineinviteid=";
	public static final String ACCEPT_INVITEID_PARAMETER = "&acceptinviteid=";

	public static final String API_V2_GET_ECHESS_CURRENT_GAMES_ID = "/api/v2/get_echess_current_games?id=";
	public static final String API_ECHESS_CHALLENGES_ID = "/api/echess_challenges?id=";
	public static final String API_V2_GET_ECHESS_FINISHED_GAMES_ID = "/api/v2/get_echess_finished_games?id=";
	public static final String JUST_KEEP_MY_CHALLENGE = "Just keep my challenge: ";
	public static final String CANCEL_MY_CHALLENGE = "Cancel my challenge: ";
	public static final String LOGIN_HTML_ALS = "/login.html?als=";
	public static final String ECHESS_MOBILE_STATS = "/echess/mobile-stats/";
	public static final String TOURNAMENTS = "/tournaments";
}
