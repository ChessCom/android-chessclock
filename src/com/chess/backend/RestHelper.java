package com.chess.backend;

import android.util.Log;
import com.bugsense.trace.BugSenseHandler;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.BaseResponseItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * RestHelper class
 *
 * @author alien_roger
 * @created at: 14.03.12 5:47
 */
public class RestHelper {

	/* Methods*/  // new
	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final String PUT = "PUT";
	public static final String DELETE = "DELETE";

	/* Results */
	public static final String R_STATUS_SUCCESS = "success";  // new
	public static final String R_STATUS_ERROR = "error"; // new

	public static final String R_SUCCESS = "Success";
	public static final String R_SUCCESS_P = "Success+";
	public static final String R_ERROR = "Error+";
	public static final String R_YOUR_MOVE = "Success+1";
	public static final String R_OPPONENT_MOVE = "Success+0";

//	https://github.com/ChessCom/chess/blob/develop/docs/api_user_manual.txt
	public static final String BASE_URL = "http://www.chess-7.com/index_api_qa.php";
//	public static final String BASE_URL = "http://www.chess.com";
	private static final String API = "/api";
	private static final String V1 = "/v1";
	private static final String API_V2 = API + "/v2";
	private static final String API_V3 = API + "/v3";
	private static final String API_V4 = API + "/v4";
	private static final String API_V5 = API + "/v5";
	private static final String USERS = "/users";
	private static final String GAMES = "/games";

	/*Google Cloud Messaging API part*/
	public static final String GCM_BASE_URL = BASE_URL + "/api/gcm";
	public static final String GCM_REGISTER = GCM_BASE_URL + "/register";
	public static final String GCM_UNREGISTER = GCM_BASE_URL + "/unregister";

	/* Params */
//	public static final String GCM_P_ID = "id";
	public static final String GCM_P_REGISTER_ID = "registrationId";
//	public static final String GCM_P_DEVICE_ID = "device_id";


	/* Methods calls*/
	public static final String LOGIN_HTML_ALS = BASE_URL + "/login.html?als=";
	public static final String REGISTER_HTML = BASE_URL + "/register.html";
	public static final String PLAY_ANDROID_HTML = BASE_URL + "/play/android.html";
	public static final String ECHESS_MOBILE_STATS = BASE_URL + "/echess/mobile-stats/";
	public static final String TOURNAMENTS = BASE_URL + "/tournaments";
	public static final String GOOGLE_PLAY_URI = "market://details?id=com.chess";

//	public static final String VACATION_LEAVE = BASE_URL + API + "/vacation_leave";
//	public static final String VACATION_RETURN = BASE_URL + API + "/vacation_return";
//	public static final String REGISTER = BASE_URL + API + "/register";
//	public static final String TACTICS_TRAINER = BASE_URL + API + "/tactics_trainer";
//	public static final String VALIDATE_TOKEN = BASE_URL + API + "/validate_auth_token";
//	public static final String GET_GAME = BASE_URL + API + "/get_game";
//	public static final String ADD_FRIEND = BASE_URL + API + "/add_friend";
//	public static final String SUBMIT_CHEATER_SUSPECT = BASE_URL + API + "/submit_cheater_suspect";
//	public static final String GET_USER_INFO = BASE_URL + API + "/get_user_info";
//	public static final String GET_MOVE_STATUS = BASE_URL + API + "/get_move_status";
//	public static final String FRIEND_REQUEST = BASE_URL + API + "/friend_request";
//	public static final String GET_FRIEND_REQUEST = BASE_URL + API + "/get_friend_requests";
//	public static final String GET_VACATION_STATUS = BASE_URL + API + "/get_vacation_status";
	public static final String GET_ANDROID_VERSION = BASE_URL + API + "/get_android_version";
//	public static final String GET_TACTICS_PROBLEM_BATCH = BASE_URL + API + "/get_tactics_problem_batch";

	// Echess methods
//    public static final String ECHESS_SUBMIT_ACTION = BASE_URL + API + "/submit_echess_action";
//	public static final String ECHESS_OPEN_INVITES = BASE_URL + API + "/echess_open_invites";
//	public static final String ECHESS_OPEN_INVITES_V2 = BASE_URL + API_V2 + "/echess_open_invites";
//	public static final String ECHESS_CHALLENGES = BASE_URL + API + "/echess_challenges";
//	public static final String ECHESS_CHALLENGES_V2 = BASE_URL + API_V2 + "/echess_challenges";
//	public static final String ECHESS_NEW_GAME = BASE_URL + API + "/echess_new_game";
//	public static final String ECHESS_CURRENT_GAMES = BASE_URL + API_V3 + "/get_echess_current_games";
//	public static final String ECHESS_FINISHED_GAMES = BASE_URL + API_V3 + "/get_echess_finished_games";
//	public static final String ECHESS_INFO = BASE_URL + API_V2 + "/get_echess_info";

//	public static final String LOGIN = BASE_URL + API_V2 + "/login";
//	public static final String GET_FRIENDS_ONLINE = BASE_URL + API_V2 + "/get_friends";
//	public static final String GET_FRIENDS = BASE_URL + API + "/get_friends";
//	public static final String GET_VIDEOS  = BASE_URL + API_V2 + "/get_videos";

//	public static final String GET_USER_INFO_V2 = BASE_URL + API_V2 + "/get_user_info";
//	public static final String GET_USER_INFO_V3 = BASE_URL + API_V3 + "/get_user_info";

	public static final String GET_GAME_V5 = BASE_URL + API_V5 + "/get_game";

	/* Methods */
		/*Users*/
	public static final String CMD_USER = BASE_URL + V1 + USERS;
	public static final String CMD_LOGIN = CMD_USER +"/login";
	public static final String CMD_REGISTER = CMD_USER +"/register";
	public static final String CMD_GCM = CMD_USER + "/gcm";
	public static final String CMD_USER_STATS = CMD_USER + "/stats";
		/*Games*/
	public static final String CMD_GAMES = BASE_URL + V1 + GAMES;
	public static final String CMD_GAMES_ALL = CMD_GAMES +"/all";
	public static final String CMD_GAMES_CHALLENGES = CMD_GAMES +"/challenges";
	public static final String CMD_VACATIONS = CMD_GAMES + "/vacations";
	public static final String CMD_SEEKS = CMD_GAMES + "/seeks";
	public static final String CMD_MOVES = CMD_GAMES + "/moves";
	public static final String CMD_GAME_STATS = CMD_GAMES + "/stats";
		/*Articles*/
	public static final String CMD_ARTICLES = BASE_URL + V1 + "/articles";
	public static final String CMD_ARTICLES_LIST = CMD_ARTICLES + "/list";
	public static final String CMD_ARTICLES_CATEGORIES = CMD_ARTICLES + "/categories";
	public static String CMD_ARTICLE_BY_ID(long id) {return CMD_ARTICLES + "/" + id;}
	public static String CMD_ARTICLE_COMMENTS(long id) {return CMD_ARTICLES + "/" + id + "/comments";} // POST

		/*Other stuff*/
	public static final String CMD_FRIENDS = BASE_URL + V1 + "/friends";
	public static final String CMD_VIDEOS = BASE_URL + V1 + "/videos";
	public static final String CMD_VIDEO_CATEGORIES = CMD_VIDEOS + "/categories";

	public static final String CMD_TACTICS = BASE_URL + V1 + "/tactics";
	public static final String CMD_TACTIC_TRAINER = CMD_TACTICS + "/trainer";
	public static String CMD_ANSWER_GAME_SEEK(long gameSeekId) {return CMD_GAMES + "/" + gameSeekId + "/seeks";}
	public static String CMD_PUT_GAME_ACTION(long gameId) {return CMD_GAMES + "/" + gameId + "/actions";}
	public static String CMD_GAME_BY_ID(long gameId) {return CMD_GAMES + "/" + gameId;}

	/* Parameters */
	// new
	public static final String P_USER_NAME_OR_MAIL = "usernameOrEmail";
//	public static final String P_FIELDS = "fields";
	public static final String P_FIELDS = "fields[]";
	public static final String P_LOGIN_TOKEN = "loginToken";
	public static final String P_PAGE = "page";
	public static final String P_ITEMS_PER_PAGE = "itemsPerPage";

	public static final String P_USER_NAME = "username";
	public static final String P_PASSWORD = "password";
	public static final String P_EMAIL = "email";
	public static final String P_AUTH_TOKEN = "auth_token";
//	public static final String P_COUNTRY_ID = "country_id";
	public static final String P_COUNTRY_ID = "countryId";
	public static final String P_COUNTRY_CODE = "countryCode";
//	public static final String P_APP_TYPE = "app_type";
	public static final String P_APP_TYPE = "appType";
//	public static final String P_FACEBOOK_ACCESS_TOKEN = "facebook_access_token";
	public static final String P_FACEBOOK_ACCESS_TOKEN = "facebookAccessToken";
	public static final String P_APN_DEVICE_TOKEN = "apn_device_token";
	public static final String P_OPPONENT = "opponent";

/*
opponent		false	See explanation above for possible values. Default is `null`.
daysPerMove	\d+	true	Days per move.
userPosition	0|1|2	true	User will play as - 0 = random, 1 = white, 2 = black. Default is `0`.
minRating	\d+	false	Minimum rating.
maxRating	\d+	false	Maximum rating.
isRated	0|1	true	Is game seek rated or not. Default is `1`.
gameTypeCode	chess(960)?	true	Game type code. Default is `chess`.
gameSeekName	\w+	false	Name of new game/challenge. Default is `Let's Play!`.
		 */

	public static final String P_DAYS_PER_MOVE = "daysPerMove";
	public static final String P_USER_SIDE = "userPosition";
	public static final String P_MIN_RATING = "minRating";
	public static final String P_MAX_RATING = "maxRating";
	public static final String P_IS_RATED = "isRated";
	public static final String P_GAME_TYPE = "gameTypeCode";

	public static final String P_UID = "uid";
	public static final String P_LOCATION = "location";
	public static final String P_AMOUNT = "amount";

	public static final int P_WHITE = 1;
	public static final int P_BLACK = 2;
	public static final String P_GID = "gid"; // game id
	public static final String P_ALL = "all";
	public static final String P_RETURN = "return";
	public static final String P_GET_GAME = "get_game";

	public static final String P_TACTICS_ID = "tacticsId";
	public static final String P_TACTICS_TRAINER = "tactics_trainer";
	public static final String P_PASSED = "passed";
	public static final String P_CORRECT_MOVES = "correctMoves";
	public static final String P_ENCODED_MOVES = "encodedMoves";
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
//	public static final String P_CHESSID = "chessid";
	public static final String P_COMMAND = "command";
	public static final String P_NEWMOVE = "newMove";
	public static final String P_NEWMOVEENCODED = "newMoveEncoded";
	public static final String P_TIMESTAMP = "timestamp";
	public static final String P_MESSAGE = "message";

	public static final String P_IS_INSTALL = "isInstall";

	public static final String P_IPHONE = "iphone";
	public static final String P_KEYWORD = "keyword";
	public static final String P_CATEGORY = "category";
	public static final String P_SKILL_LEVEL = "skill_level";
	public static final String P_OPENING = "opening";
	public static final String P_AUTHOR = "author";
	public static final String P_THEME = "theme";
	public static final String P_PAGE_SIZE = "page-size";

	private static final String GOTO = "&goto=";

	/* Returned Values */
	public static final String R_ERROR_MESSAGE = "error_message";
	public static final String R_USER_TOKEN = "user_token";
	public static final String R_FB_USER_HAS_NO_ACCOUNT = "Facebook user has no Chess.com account";
	public static final String SYMBOL_PARAMS_SPLIT = ":";
	public static final String SYMBOL_ITEM_SPLIT = "[|]";
	public static final String R_PLEASE_LOGIN_AGAIN = "Please login again.";
	public static final String R_INVALID_PASS = "Invalid password.";
	public static final String R_YOU_ARE_ON_VACATION = "You are on vacation.";
	public static final String R_TACTICS_LIMIT_REACHED = "Tactics daily limit reached.";

	public static final String R_DRAW_OFFER_PENDING = "is_draw_offer_pending";

	public static final boolean IS_TEST_SERVER_MODE = !BASE_URL.equals("http://www.chess.com");
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String AUTHORIZATION_HEADER_VALUE = "Basic Ym9iYnk6ZmlzY2hlcg==";


	/* Values */
	// new
	public static final int V_BASIC_MEMBER = 0;
	public static final int V_GOLD_MEMBER = 1;
	public static final int V_PLATINUM_MEMBER = 2;
	public static final int V_DIAMOND_MEMBER = 3;

/*
loginToken			\w+	true	Login token used to authenticate user.
command			SUBMIT|RESIGN|OFFERDRAW|ACCEPTDRAW|DECLINEDRAW|CHAT	true	Command to execute.
timestamp			\d+	true	Timestamp of the game.
newMove				false	Only used for `SUBMIT` command - format = b7b8=Q or e2e4
newMoveEncoded		false	Only used for `SUBMIT` command - strange encoded move format.
message				false	Only used for `CHAT` command.
	 */

	// old
	public static final String V_RESIGN = "RESIGN";
	public static final String V_SUBMIT = "SUBMIT";
	public static final String V_OFFERDRAW = "OFFERDRAW";
	public static final String V_ACCEPTDRAW = "ACCEPTDRAW";
	public static final String V_DECLINEDRAW = "DECLINEDRAW";
	public static final String V_CHAT = "CHAT";
/*
1 = Standard Chess | chess
2 = Chess 960	   | chess(960)
	 */
	public static final int V_GAME_CHESS = 1;
	public static final int V_GAME_CHESS_960 = 2;

	public static final String V_ENCODED_MOVES = "encodedMoves";
	public static final String V_GAME_ID = "game_id";
	public static final String V_TACTICS_ID = "tactics_id";
//	public static final String V_PASSED = "passed";
	public static final String V_CORRECT_MOVES = "correct_moves";
	public static final String V_SECONDS = "seconds";
	public static final String V_TACTICS_RATING = "tacticsrating";
	public static final String V_USERNAME = "username";
	public static final String V_TEST_NAME = "bobby";
	public static final String V_TEST_NAME2 = "fischer";

	//	all         (0 = show only games where its users turn to move , 1 = show all users games)
	public static final String V_ONLY_USER_TURN = "0";
	public static final String V_VIDEO_LIST_CNT = "20";
    public static final String V_VIDEO_ITEM_ONE = "1";
    public static final String V_ANDROID = "android";
	public static final String V_TRUE = "1";
	public static final String V_FALSE = "0";

	//private static final String TAG = "Encode";
	public static final int MAX_ITEMS_CNT = 2000;

	private static final String Q_ = "?";
	private static final String EQUALS = "=";
	private static final String AND = "&";


	public static String formCustomRequest(LoadItem loadItem) {
		return loadItem.getLoadPath() + formUrl(loadItem.getRequestParams());
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

	public static final String OBJ_START = "{";
	public static final String SYMBOL_QUOTE = "\"";
	public static final String OBJ_DIVIDER = ":";
	public static final String OBJ_END = "}";


	public static String formJsonData(List<NameValuePair> requestParams){
		StringBuilder data = new StringBuilder();
		String separator = StaticData.SYMBOL_EMPTY;
		data.append(OBJ_START);
		for (NameValuePair requestParam : requestParams) {

			data.append(separator);
			separator = StaticData.SYMBOL_COMMA;
			data.append(SYMBOL_QUOTE)
					.append(requestParam.getName()).append(SYMBOL_QUOTE)
					.append(OBJ_DIVIDER)
					.append(SYMBOL_QUOTE)
					.append(requestParam.getValue())
					.append(SYMBOL_QUOTE);
		}
		data.append(OBJ_END);
		return data.toString();
	}

	public static String formPostData(LoadItem loadItem) {
		List<NameValuePair> nameValuePairs = loadItem.getRequestParams();

//		String url = Q_;
//		for (NameValuePair pair: nameValuePairs) {
//			url += pair.getName() + EQUALS + pair.getValue();
//			url += AND;
//		}

		StringBuilder encodedParams = new StringBuilder();
//		encodedParams.append(Q_);
		String separator = StaticData.SYMBOL_EMPTY;
		for (NameValuePair pair: nameValuePairs) {
			encodedParams.append(separator);
			separator = AND;

			String name;
			String value;
			try {
				name = URLEncoder.encode(pair.getName(), HTTP.UTF_8);
				value = URLEncoder.encode(pair.getValue(), HTTP.UTF_8);
			} catch (UnsupportedEncodingException e) {
				Log.e("TEST", "failed to encode url");
				e.printStackTrace();
				name = pair.getName();
				value = pair.getValue();
			}
			encodedParams.append(name).append(EQUALS).append(value);
		}

		return encodedParams.toString();
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
//				+ LccHolder.HOST + "%2Fmembership.html" + param;
	}

/*	public static String formCustomPaginationRequest(LoadItem loadItem, int page) {
		loadItem.replaceRequestParams(P_PAGE, String.valueOf(page));
		return loadItem.getLoadPath() + formUrl(loadItem.getRequestParams());
	}*/

	public static int encodeServerCode(int code) {
		return StaticData.INTERNAL_ERROR | code << 8;
	}

	public static int decodeServerCode(int code) {
		return code >> 8;
	}

	public static boolean containsServerCode(int code) {
		return code > 0 && code >> 8 != 0;
	}


	public static  <CustomType> CustomType requestData(LoadItem loadItem, Class<CustomType> customTypeClass) throws InternalErrorException{
		CustomType item = null;
		String TAG = "RequestJsonTask";
		String url = formCustomRequest(loadItem);
		String requestMethod = loadItem.getRequestMethod();
		if (requestMethod.equals(POST) || requestMethod.equals(PUT)){
			url = formPostRequest(loadItem);
		}

		Log.d(TAG, "retrieving from url = " + url);

		long tag = System.currentTimeMillis();
		BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_REQUEST, "tag=" + tag + " " + url);

		HttpURLConnection connection = null;
		try {
			URL urlObj = new URL(url);
			connection = (HttpURLConnection) urlObj.openConnection();
			connection.setRequestMethod(requestMethod);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + HTTP.UTF_8);

			if (IS_TEST_SERVER_MODE) {
				Authenticator.setDefault(new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(V_TEST_NAME, V_TEST_NAME2.toCharArray());
					}
				});
			}

			if (requestMethod.equals(POST) || requestMethod.equals(PUT) ){
				submitPostData(connection, loadItem);
			}

			final int statusCode = connection.getResponseCode();
			Gson gson = new Gson();
			if (statusCode != HttpStatus.SC_OK) {
				Log.e(TAG, "Error " + statusCode + " while retrieving data from " + url);

				InputStream inputStream = connection.getErrorStream();
				String resultString = convertStreamToString(inputStream);

				BaseResponseItem baseResponse = gson.fromJson(resultString, BaseResponseItem.class);
				Log.d(TAG, "Code: " + baseResponse.getCode() + " Message: " + baseResponse.getMessage());
				throw new InternalErrorException(encodeServerCode(baseResponse.getCode()));
			}

			InputStream inputStream = null;
			String resultString = null;
			try {
				inputStream = connection.getInputStream();

				resultString = convertStreamToString(inputStream);
				if (resultString.contains(OBJ_START)){
					int firstIndex = resultString.indexOf(OBJ_START);

					int lastIndex = resultString.lastIndexOf(OBJ_END);

					resultString = resultString.substring(firstIndex, lastIndex + 1);

				} else /*(!resultString.startsWith(OBJ_START))*/{
//					result = StaticData.INTERNAL_ERROR;
					Log.d(TAG, "ERROR -> WebRequest SERVER RESPONSE: " + resultString);
					throw new InternalErrorException(StaticData.INTERNAL_ERROR);
				}
				BaseResponseItem baseResponse = gson.fromJson(resultString, BaseResponseItem.class);
				if (baseResponse.getStatus().equals(R_STATUS_SUCCESS)) {
					item = gson.fromJson(resultString, customTypeClass);
					if(item == null) {
//						result = StaticData.RESULT_OK;
						throw new InternalErrorException(StaticData.EMPTY_DATA);
					}
				}
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}

			Log.d(TAG, "WebRequest SERVER RESPONSE: " + resultString);
			BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_RESPONSE, "tag=" + tag + " " + resultString);

		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new InternalErrorException(e, StaticData.INTERNAL_ERROR);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			throw new InternalErrorException(e, StaticData.INTERNAL_ERROR);
		} catch (IOException e) {
			if (e instanceof InternalErrorException) {
				throw new InternalErrorException(e, ((InternalErrorException)e).getCode());
			} else {
				Log.e(TAG, "I/O error while retrieving data from " + url, e);
				throw new InternalErrorException(e, StaticData.NO_NETWORK);
			}
		} catch (IllegalStateException e) {
			Log.e(TAG, "Incorrect URL: " + url, e);
			throw new InternalErrorException(e, StaticData.UNKNOWN_ERROR);
		} catch (Exception e) {
			Log.e(TAG, "Error while retrieving data from " + url, e);
			throw new InternalErrorException(e, StaticData.UNKNOWN_ERROR);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return item;
	}

	private static void submitPostData(URLConnection connection, LoadItem loadItem) throws IOException {
		String query = formPostData(loadItem);
		String charset = HTTP.UTF_8;
		connection.setDoOutput(true); // Triggers POST.
		OutputStream output = null;
		try {
			output = connection.getOutputStream();
			output.write(query.getBytes(charset));
		} finally {
			if (output != null) try {
				output.close();
			} catch (IOException ex) {
				Log.e("RequestJsonTask", "Error while submiting POST data " + ex.toString());
			}
		}

	}


	public static <ItemType> String parseJsonToString(ItemType jRequest) {
		Gson gson = new Gson();
		return gson.toJson(jRequest);
	}

	public static String convertStreamToString(java.io.InputStream is) {
		Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
		return scanner.hasNext() ? scanner.next() : "";
	}
}
