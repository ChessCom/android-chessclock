package com.chess.backend;

import com.chess.backend.entity.LoadItem;
import com.chess.backend.statics.StaticData;
import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * RestHelper class
 *
 * @author alien_roger
 * @created at: 14.03.12 5:47
 */
public class RestHelper {

	/* Results */
	public static final String R_SUCCESS = "Success";
	public static final String R_SUCCESS_P = "Success+";
	public static final String R_ERROR = "Error+";
	public static final String R_YOUR_MOVE = "Success+1";
	public static final String R_OPPONENT_MOVE = "Success+0";

//	https://github.com/ChessCom/chess/blob/develop/docs/api_user_manual.txt
//	public static final String BASE_URL = "http://www.chess-5.com";

	public static final String HOST_PRODUCTION = "chess.com";
	public static final String HOST_TEST = "chess-2.com";
	public static final String HOST = HOST_TEST; // switch production/test server

	public static final String BASE_URL = "http://www." + HOST;
	public static final String API_V5 = "/api/v5";
	public static final String API_V4 = "/api/v4";
	public static final String API_V3 = "/api/v3";
	public static final String API_V2 = "/api/v2";
	public static final String API = "/api";

	/*Google Cloud Messaging API part*/
	public static final String GCM_BASE_URL = BASE_URL + "/api/gcm";
	public static final String GCM_REGISTER = GCM_BASE_URL + "/register";
	public static final String GCM_UNREGISTER = GCM_BASE_URL + "/unregister";
	/* Params */
	public static final String GCM_P_ID = "id";
	public static final String GCM_P_REGISTER_ID = "registration_id";
//	public static final String GCM_P_DEVICE_ID = "device_id";


	//	API: Allow auto login to chess.com:
//	url: http://www.chess.com/login.html?als=<auto_login_string>&goto=<goto_url>
//
//	parameters
//	id          (user token)
//	goto_url    (redirect url after login)
//
//	Example: http://www.chess.com/login.html?als=kFCMJsY4%2BiWfnCKH7%2F%2FG45myelPMhTDTN0UTho3j99KlppWfbj6Dz7oBtjLRM5elPYxo1Q0m2qWvY3EeQ44x6A%3D%3D&goto=%2Fhome%2Finvite_friends.html
	/* Methods calls*/
	public static final String LOGIN_HTML_ALS = BASE_URL + "/login.html?als=";
	public static final String REGISTER_HTML = BASE_URL + "/register.html";
	public static final String PLAY_ANDROID_HTML = BASE_URL + "/play/android.html";
	public static final String ECHESS_MOBILE_STATS = BASE_URL + "/echess/mobile-stats/";
	public static final String TOURNAMENTS = BASE_URL + "/tournaments";
	public static final String GOOGLE_PLAY_URI = "market://details?id=com.chess";

	public static final String VACATION_LEAVE = BASE_URL + API + "/vacation_leave";
	public static final String VACATION_RETURN = BASE_URL + API + "/vacation_return";
	public static final String REGISTER = BASE_URL + API + "/register";
	public static final String TACTICS_TRAINER = BASE_URL + API + "/tactics_trainer";
	public static final String VALIDATE_TOKEN = BASE_URL + API + "/validate_auth_token";
	public static final String GET_GAME = BASE_URL + API + "/get_game";
	public static final String ADD_FRIEND = BASE_URL + API + "/add_friend";
	public static final String SUBMIT_CHEATER_SUSPECT = BASE_URL + API + "/submit_cheater_suspect";
	public static final String GET_USER_INFO = BASE_URL + API + "/get_user_info";
	public static final String GET_MOVE_STATUS = BASE_URL + API + "/get_move_status";
	public static final String FRIEND_REQUEST = BASE_URL + API + "/friend_request";
	public static final String GET_FRIEND_REQUEST = BASE_URL + API + "/get_friend_requests";
	public static final String GET_VACATION_STATUS = BASE_URL + API + "/get_vacation_status";
	public static final String GET_ANDROID_VERSION = BASE_URL + API + "/get_android_version";
	public static final String GET_TACTICS_PROBLEM_BATCH = BASE_URL + API + "/get_tactics_problem_batch";

	// Echess methods
    public static final String ECHESS_SUBMIT_ACTION = BASE_URL + API + "/submit_echess_action";
	public static final String ECHESS_OPEN_INVITES = BASE_URL + API + "/echess_open_invites";
	public static final String ECHESS_OPEN_INVITES_V2 = BASE_URL + API_V2 + "/echess_open_invites";
	public static final String ECHESS_CHALLENGES = BASE_URL + API + "/echess_challenges";
	public static final String ECHESS_CHALLENGES_V2 = BASE_URL + API_V2 + "/echess_challenges";
	public static final String ECHESS_NEW_GAME = BASE_URL + API + "/echess_new_game";
	public static final String ECHESS_CURRENT_GAMES = BASE_URL + API_V3 + "/get_echess_current_games";
	public static final String ECHESS_FINISHED_GAMES = BASE_URL + API_V3 + "/get_echess_finished_games";
	public static final String ECHESS_INFO = BASE_URL + API_V2 + "/get_echess_info";

	public static final String LOGIN = BASE_URL + API_V2 + "/login";
	public static final String GET_FRIENDS_ONLINE = BASE_URL + API_V2 + "/get_friends";
	public static final String GET_FRIENDS = BASE_URL + API + "/get_friends";
	public static final String GET_VIDEOS  = BASE_URL + API_V2 + "/get_videos";

	public static final String GET_USER_INFO_V2 = BASE_URL + API_V2 + "/get_user_info";
	public static final String GET_USER_INFO_V3 = BASE_URL + API_V3 + "/get_user_info";

	public static final String GET_GAME_V5 = BASE_URL + API_V5 + "/get_game";

	/* Parameters */
	public static final String P_USER_NAME = "username";
	public static final String P_PASSWORD = "password";
	public static final String P_EMAIL = "email";
	public static final String P_AUTH_TOKEN = "auth_token";
	public static final String P_COUNTRY_ID = "country_id";
	public static final String P_COUNTRY_CODE = "country_code";
	public static final String P_APP_TYPE = "app_type";
	public static final String P_FACEBOOK_ACCESS_TOKEN = "facebook_access_token";
	public static final String P_APN_DEVICE_TOKEN = "apn_device_token";
	public static final String P_OPPONENT = "opponent";

	public static final String P_TIMEPERMOVE = "timepermove";
	public static final String P_IPLAYAS = "iplayas";
	public static final String P_MINRATING = "minrating";
	public static final String P_MAXRATING = "maxrating";
	public static final String P_ISRATED = "israted";
	public static final String P_GAME_TYPE = "game_type";

	public static final String P_UID = "uid";
	public static final String P_LOCATION = "location";
	public static final String P_AMOUNT = "amount";

	public static final String P_ID = "id"; // user token
	public static final String P_GID = "gid"; // game id
	public static final String P_ALL = "all";
	public static final String P_RETURN = "return";
	public static final String P_GET_GAME = "get_game";
	public static final String P_TACTICS_ID = "tactics_id";
	public static final String P_TACTICS_TRAINER = "tactics_trainer";
	public static final String P_PASSED = "passed";
	public static final String P_CORRECT_MOVES = "correct_moves";
	public static final String P_SECONDS = "seconds";
	public static final String P_GET_ANDROID_VERSION = "get_android_version";
	public static final String P_ECHESS_OPEN_INVITES = "echess_open_invites";
	public static final String P_DECLINEINVITEID = "declineinviteid";
	public static final String P_ACCEPTINVITEID = "acceptinviteid";
	public static final String P_GET_ECHESS_CURRENT_GAMES = "get_echess_current_games";
	public static final String P_ECHESS_CHALLENGES = "echess_challenges";
	public static final String P_GET_ECHESS_FINISHED_GAMES = "get_echess_finished_games";

	public static final String P_STARTING_FEN_POSITION = "starting_fen_position";
	public static final String P_USER_TO_MOVE = "user_to_move";
	public static final String P_HAS_NEW_MESSAGE = "has_new_message";
	public static final String P_GAME_RESULT = "game_result";
	public static final String P_DRAW_OFFERED = "draw_offered";

	//	SUBMIT_ECHESS_ACTION
	public static final String P_CHESSID = "chessid";
	public static final String P_COMMAND = "command";
	public static final String P_NEWMOVE = "newmove";
	public static final String P_NEWMOVEENCODED = "newmoveencoded";
	public static final String P_TIMESTAMP = "timestamp";
	public static final String P_MESSAGE = "message";

	public static final String P_IS_INSTALL = "is_install";

	public static final String P_IPHONE = "iphone";
	public static final String P_KEYWORD = "keyword";
	public static final String P_CATEGORY = "category";
	public static final String P_SKILL_LEVEL = "skill_level";
	public static final String P_OPENING = "opening";
	public static final String P_AUTHOR = "author";
	public static final String P_THEME = "theme";
	public static final String P_PAGE = "page";
	public static final String P_PAGE_SIZE = "page-size";

	private static final String GOTO = "&goto=";

	/* Returned Values */
	public static final String R_ERROR_MESSAGE = "error_message";
	public static final String R_USER_TOKEN = "user_token";
	public static final String R_FB_USER_HAS_NO_ACCOUNT = "Facebook user has no Chess.com account";
	public static final String SYMBOL_PARAMS_SPLIT = ":";
	public static final String SYMBOL_PARAMS_SEPARATOR = "[+]";
	public static final String R_PLEASE_LOGIN_AGAIN = "Please login again.";
	public static final String R_INVALID_PASS = "Invalid password.";
	public static final String R_YOU_ARE_ON_VACATION = "You are on vacation.";
	public static final String R_TACTICS_LIMIT_REACHED = "Tactics daily limit reached.";

	public static final String R_DRAW_OFFER_PENDING = "is_draw_offer_pending";

	public static final boolean IS_TEST_SERVER_MODE = !HOST.equals(HOST_PRODUCTION);
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String AUTHORIZATION_HEADER_VALUE = "Basic Ym9iYnk6ZmlzY2hlcg==";


//	The echess challenges response looks like the following:
//	The echess challenges response looks like the following:
//	<
//	<game_seek_id>: The game id
//	<game_name>: The seek name - can be null
//	<opponent_username>: The opponent username
//	<opponent_rating>: The opponent rating
//	<opponent_win_count>: The opponent win count
//	<opponent_loss_count>: The opponent loss count
//	<opponent_draw_count>: The opponent Draw count
//	<player_color>:  The users color he/she will play as, 1 = white, 2 = black, 0 = random
//	<days_per_move>: The days per move for the seek
//	<game_type>: The chess game type.  1 = chess, 2 = chess960
//	<is_rated>: Is the seek rated or unrated?, 1 for rated, 2 for not rated
//	<initial_setup_fen>: The initial starting position.  This field can be null
//	>
//
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
//	The echess finished games response looks like the following:
//	<
//	<game_id>: The game id
//	<player_color>: The users color he/she played as, w = white, b = black
//	<game_type>: The chess game type.  1 = chess, 2 = chess960
//	<game_name>: The games name, can be null
//	<white_username>: The username for white
//	<black_username>: The username for black
//	<white_rating>: Whites rating
//	<black_rating>: Blacks rating
//	<time_stamp>: The timestamp for the game
//	<initial_fen>: the initial starting position, can be null
//	<last_move_from_square>: the last move, from square
//	<last_move_to_square>: the last move, to square
//	<has_new_message>: new messages for the game. 1 = yes, 0 = no
//	<move_list>: the moves from the start of the game (all moves after <intial_fen>)
//	<game_result>: The result of the game.  1 = win, 0 = loss, 2 = draw
//	>



//	starting_fen_position   (used for thematic games only)
//	user_to_move    (1 for white and 0 for black)
//	has_new_message    (1 if id is passed and there is a new message)
//	game_type: 1 = standard chess , 2 = chess 960
//	game_result: result of the game for a given user
//	draw_offered: 0 = no draw offered, 1 = white offered draw, 2 = black offered draw



	/* Values */
	public static final String V_RESIGN = "RESIGN";
	public static final String V_SUBMIT = "SUBMIT";
	public static final String V_OFFERDRAW = "OFFERDRAW";
	public static final String V_ACCEPTDRAW = "ACCEPTDRAW";
	public static final String V_DECLINEDRAW = "DECLINEDRAW";
	public static final String V_CHAT = "CHAT";

	public static final String V_ENCODED_MOVES = "encoded_moves";
	public static final String V_TACTICS_ID = "tactics_id";
//	public static final String V_PASSED = "passed";
	public static final String V_CORRECT_MOVES = "correct_moves";
	public static final String V_SECONDS = "seconds";
	public static final String V_USERNAME = "username";

	//	all         (0 = show only games where its users turn to move , 1 = show all users games)
    public static final String V_ALL_USERS_GAMES = "1";
	public static final String V_ONLY_USER_TURN = "0";
	public static final String V_VIDEO_LIST_CNT = "20";
    public static final String V_VIDEO_ITEM_ONE = "1";
    public static final String V_ANDROID = "android";
	public static final String V_PASSED = "1";
	public static final String V_FAILED = "0";
	public static final String V_ZERO = "0";
	public static final String V_ONE = "1";

	//private static final String TAG = "Encode";
	public static final int MAX_ITEMS_CNT = 2000;

	private static final String Q_ = "?";
	private static final String EQUALS = "=";
	private static final String AND = "&";


	public static String formCustomRequest(LoadItem loadItem) {

		String fullUrl = formUrl(loadItem.getRequestParams());
		return loadItem.getLoadPath() + fullUrl;
	}

	public static String formPostRequest(LoadItem loadItem) {
		return loadItem.getLoadPath() ;
	}

	private static String formUrl(List<NameValuePair> nameValuePairs) {
		List<NameValuePair> safeList = new ArrayList<NameValuePair>();
		safeList.addAll(nameValuePairs);
		StringBuilder builder = new StringBuilder();
		builder.append(Q_);
		String separator = StaticData.SYMBOL_EMPTY;
		for (NameValuePair pair: safeList) {
			builder.append(separator);
			separator = AND;
			builder.append(pair.getName()).append(EQUALS).append(pair.getValue());
		}
		return builder.toString();
	}

	public static String formJsonData(List<NameValuePair> requestParams){
		StringBuilder data = new StringBuilder();
		String separator = StaticData.SYMBOL_EMPTY;
		data.append("{");
		for (NameValuePair requestParam : requestParams) {

			data.append(separator);
			separator = StaticData.SYMBOL_COMMA;
			data.append("\"")
					.append(requestParam.getName()).append("\"")
					.append(":")
					.append("\"")
					.append(requestParam.getValue())
					.append("\"");
		}
		data.append("}");
		return data.toString();
	}

	public static String formTournamentsLink(String userToken) {
		return LOGIN_HTML_ALS + userToken + GOTO + TOURNAMENTS;
	}

	public static String formStatsLink(String userToken, String username) {
		return LOGIN_HTML_ALS + userToken + GOTO + ECHESS_MOBILE_STATS + username;
	}

	public static String getMembershipLink(String userToken, String param) {
		return LOGIN_HTML_ALS + userToken + GOTO + "%2Fmembership.html" + param;
//				+ sharedData.getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY)
//				+ "&goto=http%3A%2F%2Fwww."
//				+ LccHelper.HOST + "%2Fmembership.html" + param;
	}

	public static String formCustomPaginationRequest(LoadItem loadItem, int page) {
		loadItem.replaceRequestParams(RestHelper.P_PAGE, String.valueOf(page));
		String fullUrl = formUrl(loadItem.getRequestParams());
		return loadItem.getLoadPath() + fullUrl;
	}
}
