package com.chess.backend;

import com.chess.backend.entity.LoadItem;
import org.apache.http.NameValuePair;

import java.net.URLEncoder;
import java.util.List;

/**
 * RestHelper class
 *
 * @author alien_roger
 * @created at: 14.03.12 5:47
 */
public class RestHelper {

	private static final String BASE_URL = "http://www.chess.com/";
	public static final String API_V3 = "/api/v3";
	public static final String API_V2 = "/api/v2";
	public static final String API = "/api";

	/* Methods calls*/
	public static final String LOGIN_HTML_ALS = BASE_URL + "/login.html?als=";
	public static final String ECHESS_MOBILE_STATS = BASE_URL + "/echess/mobile-stats/";
	public static final String TOURNAMENTS = BASE_URL + "/tournaments";

	public static final String GET_ANDROID_VERSION = BASE_URL + API + "/get_android_version";
	public static final String SUBMIT_ECHESS_ACTION = BASE_URL + API + "/submit_echess_action";
	public static final String TACTICS_TRAINER = BASE_URL + API + "/tactics_trainer";
	public static final String GET_MOVE_STATUS = BASE_URL + API + "/get_move_status";
	public static final String ECHESS_OPEN_INVITES = BASE_URL + API + "/echess_open_invites";
	public static final String ECHESS_CHALLENGES = BASE_URL + API + "/echess_challenges";
	public static final String REGISTER = BASE_URL + API + "/register";
	public static final String GET_VACATION_STATUS = BASE_URL + API + "/get_vacation_status";
	public static final String VACATION_LEAVE = BASE_URL + API + "/vacation_leave";
	public static final String VACATION_RETURN = BASE_URL + API + "/vacation_return";

	public static final String LOGIN = BASE_URL + API_V2 + "/login";
	public static final String GET_FRIENDS = BASE_URL + API_V2 + "/get_friends";
	public static final String GET_ECHESS_CURRENT_GAMES = BASE_URL + API_V2 + "/get_echess_current_games";
	public static final String GET_ECHESS_FINISHED_GAMES = BASE_URL + API_V2 + "/get_echess_finished_games";

	public static final String GET_GAME = BASE_URL + API_V3 + "/get_game";

	/* Parameters */
	public static final String P_USER_NAME = "username";
	public static final String P_PASSWORD = "password";
	public static final String EMAIL = "email";
	public static final String COUNTRY_ID = "country_id";
	public static final String COUNTRY_CODE = "country_code";
	public static final String APP_TYPE = "app_type";
	public static final String FACEBOOK_ACCESS_TOKEN = "facebook_access_token";
	public static final String APN_DEVICE_TOKEN = "apn_device_token";
	public static final String PASSWORD = "country_code";




	public static final String P_ID = "id";
	public static final String P_CHESSID = "chessid";
	public static final String P_COMMAND = "command";
	public static final String P_TIMESTAMP = "timestamp";
	public static final String P_GET_GAME = "get_game";
	public static final String P_NEWMOVE = "newmove";
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

	/* Retirned Values */
	public static final String ERROR_MESSAGE = "error_message";
	public static final String USER_TOKEN = "user_token";


	/* Values */
	public static final String V_RESIGN = "RESIGN";
	public static final String V_SUBMIT = "SUBMIT";


	public static String formCustomRequest(LoadItem loadItem) {

		String fullUrl = formUrl(loadItem.getRequestParams());
		return loadItem.getLoadPath() + fullUrl;
	}

	private static String formUrl(List<NameValuePair> nameValuePairs ) {
		String url = "?";
		for (NameValuePair pair: nameValuePairs) {
			url += pair.getName() + "=" + URLEncoder.encode(pair.getValue());
			url += "&";
		}
		return url.substring(0, url.length()-1);
	}

}
