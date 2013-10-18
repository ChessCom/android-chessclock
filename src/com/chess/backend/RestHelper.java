package com.chess.backend;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.chess.R;
import com.chess.backend.entity.api.BaseResponseItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.utilities.AppUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.protocol.HTTP;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

	private static RestHelper ourInstance = new RestHelper();

	public static RestHelper getInstance(){
		if (ourInstance == null) {
			ourInstance = new RestHelper();
		}
		return ourInstance;
	}

	private RestHelper(){}

	public static void resetInstance() {
		ourInstance = null;
	}

	/* Methods*/
	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final String PUT = "PUT";
	public static final String DELETE = "DELETE";

	/* Results */
	public static final String R_STATUS_SUCCESS = "success";

	//	http://api.chess-7.com
	public static final String HOST_PRODUCTION = "api.chess.com";
	public static final String HOST_TEST = "api.chess-7.com";
	public static String HOST = HOST_TEST; // switch production/test server

	public String BASE_URL = "http://" + (HOST == null? HOST_TEST : HOST);
	public String BASES_S_URL = "https://" + (HOST == null? HOST_TEST : HOST);
	private static final String V1 = "/v1";

	/* Methods calls*/
	public String PLAY_ANDROID_HTML = BASE_URL + "/play/android.html";
//	public static final String GOOGLE_PLAY_URI = "market://details?id=com.chess";
//	public static final String GET_ANDROID_VERSION = BASE_URL() + API + "/get_android_version";

	/* Methods */
		/*Users*/
	public String CMD_USERS = BASE_URL + V1 + "/users";
	public String CMD_LOGIN = CMD_USERS + "/login";
	public String CMD_GCM = CMD_USERS + "/gcm";
	public String CMD_USER_STATS = CMD_USERS + "/stats";
	public String CMD_USER_PROFILE = CMD_USERS + "/profile";
	public String CMD_PASSWORD = CMD_USERS + "/password";

	/*Games*/
	public String CMD_GAMES = BASE_URL + V1 + "/games/";
	public String CMD_GAMES_ALL = CMD_GAMES + "all";
	public String CMD_GAMES_CHALLENGES = CMD_GAMES + "challenges";
	public String CMD_VACATIONS = CMD_GAMES + "vacations";
	public String CMD_SEEKS = CMD_GAMES + "seeks";
	public String CMD_GAME_STATS = CMD_GAMES + "stats";

	public String CMD_GAME_BY_ID(long id) {
		return CMD_GAMES + id;
	}

	public String CMD_ANSWER_GAME_SEEK(long gameSeekId) {
		return CMD_GAMES + gameSeekId + "/seeks";
	}

	public String CMD_PUT_GAME_ACTION(long gameId) {
		return CMD_GAMES + gameId + "/actions";
	}

	/* Live Games Archive */
	public String CMD_GAMES_LIVE_ARCHIVE = CMD_GAMES + "live/archive";

	/*Articles*/
	public String CMD_ARTICLES = BASE_URL + V1 + "/articles/";
	public String CMD_ARTICLES_LIST = CMD_ARTICLES + "list";
	public String CMD_ARTICLES_CATEGORIES = CMD_ARTICLES + "categories";
	public static final int DEFAULT_ITEMS_PER_PAGE = 20;

	public String CMD_ARTICLE_BY_ID(long id) {
		return CMD_ARTICLES + id;
	}

	public String CMD_ARTICLE_COMMENTS(long id) {
		return CMD_ARTICLES + id + "/comments";
	}

	public String CMD_ARTICLE_EDIT_COMMENT(long articleId, long commentId) {
		return CMD_ARTICLES + articleId + "/comments/" + commentId;
	}

	/*Explorers*/
	public final String CMD_EXPLORERS = BASE_URL + V1 + "/explorers";
	public final String CMD_EXPLORERS_MOVES = CMD_EXPLORERS + "/moves";

	/*Forums*/
	public String CMD_FORUMS = BASE_URL + V1 + "/forums/";
	public String CMD_FORUMS_CATEGORIES = CMD_FORUMS + "categories";
	public String CMD_FORUMS_COMMENTS = CMD_FORUMS + "comments";
	public String CMD_FORUMS_TOPICS = CMD_FORUMS + "topics";

	/* Friends */
	public String CMD_FRIENDS = BASE_URL + V1 + "/friends";
	public String CMD_FRIENDS_REQUEST = CMD_FRIENDS + "/requests";

	public String CMD_FRIENDS_REQUEST_BY_ID(long id) {
		return CMD_FRIENDS + "/" + id + "/requests";
	}

	/* Videos */
	public String CMD_VIDEOS = BASE_URL + V1 + "/videos/";
	public String CMD_VIDEO_CATEGORIES = CMD_VIDEOS + "categories";

	public String CMD_VIDEO_BY_ID(long id) {
		return CMD_VIDEOS + id;
	}

	public String CMD_VIDEOS_COMMENTS(long id) {
		return CMD_VIDEOS + id + "/comments";
	}

	public String CMD_VIDEOS_EDIT_COMMENT(long videoId, long commentId) {
		return CMD_VIDEOS + videoId + "/comments/" + commentId;
	}

	/* Tactics */
	public String CMD_TACTICS = BASE_URL + V1 + "/tactics/";
	public String CMD_TACTICS_STATS = CMD_TACTICS + "stats";
	public String CMD_TACTIC_TRAINER = CMD_TACTICS + "trainer";

	/* Lessons */
	public String CMD_LESSONS = BASE_URL + V1 + "/lessons/";
	public String CMD_LESSONS_CATEGORIES = CMD_LESSONS + "categories";
	public String CMD_LESSONS_COURSES = CMD_LESSONS + "courses";
	public String CMD_LESSONS_RATING = CMD_LESSONS + "rating";
	public String CMD_LESSONS_STATS = CMD_LESSONS + "stats";

	public String CMD_LESSON_BY_ID(long id) {
		return CMD_LESSONS + id;
	}

	/**
	 * Themes
 	 */
	public String CMD_THEMES = BASE_URL + V1 + "/themes";
	public String CMD_THEMES_DEFAULT = CMD_THEMES + "/default";
	public String CMD_THEMES_USER = CMD_THEMES + "/user";

	public String CMD_THEME_DEFAULT_BY_ID(long id) {
		return CMD_THEMES_DEFAULT + id;
	}

	public String CMD_THEME_USER_BY_ID(long id) {
		return CMD_THEMES_USER + id;
	}

	/**
	 * Theme Backgrounds
	 */
	public String CMD_BACKGROUND = BASE_URL + V1 + "/background";

	public String CMD_BACKGROUND_BY_ID(long id) {
		return CMD_BACKGROUND + "/" + id;
	}

	/**
	 * Theme Boards
 	 */
	public String CMD_BOARD = BASE_URL + V1 + "/board";

	public String CMD_BOARD_BY_ID(long id) {
		return CMD_BOARD + "/" + id;
	}

	/**
	 * Theme Pieces
 	 */
	public String CMD_PIECES = BASE_URL + V1 + "/pieces";

	public String CMD_PIECES_BY_ID(long id) {
		return CMD_PIECES + "/" + id;
	}

	/**
	 * Theme Sounds
 	 */
	public String CMD_SOUND = BASE_URL + V1 + "/sound";

	public String CMD_SOUND_BY_ID(long id) {
		return CMD_SOUND + "/" + id;
	}


	/* Messages */
	public String CMD_MESSAGES = BASE_URL + V1 + "/messages/";
	public String CMD_MESSAGES_INBOX = CMD_MESSAGES + "inbox";
	public String CMD_MESSAGES_ARCHIVE = CMD_MESSAGES + "archive";

	public String CMD_MESSAGE_CONVERSATION_BY_ID(long id) {
		return CMD_MESSAGES + id;
	}

	public String CMD_MEMBERSHIP = BASE_URL + V1 + "/membership/android";
	public String CMD_MEMBERSHIP_PAYLOAD = CMD_MEMBERSHIP + "/payload";
	public String CMD_MEMBERSHIP_KEY = CMD_MEMBERSHIP + "/public-key";

	public static String GET_FEN_IMAGE(String fen) {
		// fen should be like "rnbqkbnr%2Fpppppppp%2F8%2F8%2F3P4%2F8%2FPPP1PPPP%2FRNBQKBNR"
		return "http://www.chess.com/diagram?fen=" + fen+ "&amp;size=0";
	}

	/* Parameters */
	public static final String P_USER_NAME_OR_MAIL = "usernameOrEmail";
	public static final String P_FIELDS = "fields[]";
	public static final String P_LOGIN_TOKEN = "loginToken";
	public static final String P_PAGE_ = "page";
	public static final String P_ITEMS_PER_PAGE = "itemsPerPage";
	public static final String P_LIMIT = "limit";
	public static final String GCM_P_REGISTER_ID = "registrationId";

	public static final String P_USERNAME = "username";
	public static final String P_VIEW_USERNAME = "viewUsername";
	public static final String P_TACTICS_RATING = "tacticsrating";
	public static final String P_PASSWORD = "password";
	public static final String P_EMAIL = "email";
	public static final String P_DEVICE_ID = "deviceId";
	public static final String P_FIRST_NAME = "firstName";
	public static final String P_LAST_NAME = "lastName";
	public static final String P_SKILL_LEVEL = "skillLevel";
	public static final String P_AVATAR = "avatar";
	public static final String P_COUNTRY_ID = "countryId";
	public static final String P_COUNTRY_CODE = "countryCode";
	public static final String P_CURRENT_POINTS = "currentPoints";
	public static final String P_CURRENT_PERCENT = "currentPercent";
	public static final String P_LAST_POS_NUMBER = "lastPositionNumber";
	public static final String P_OLD_PASS = "oldPassword";
	public static final String P_NEW_PASS = "newPassword";

	public static final String P_BACKGROUND_ID = "backgroundId";
	public static final String P_SCREEN = "screen";
	public static final String P_WIDTH = "width";
	public static final String P_HEIGHT = "height";
	public static final String P_BOARD_ID = "boardId";
	public static final String P_SOUND_ID = "soundId";
	public static final String P_PIECE_ID = "pieceId";

	public static final String P_MEMBERSHIP_TYPE = "membershipType";
	//	public static final String P_APP_TYPE = "app_type";
	public static final String P_APP_TYPE = "appType";
	//	public static final String P_FACEBOOK_ACCESS_TOKEN = "facebook_access_token";
	public static final String P_FACEBOOK_ACCESS_TOKEN = "facebookAccessToken";
	public static final String P_APN_DEVICE_TOKEN = "apn_device_token";
	public static final String P_OPPONENT = "opponent";
	//	public static final String P_CONVERSATION_ID = "conversationId";
	public static final String P_CONTENT = "content";

	public static final String V_0 = "MmE5NzUxNmMzNTRiNjg4NDhjZGJkOGY1NGEyMjZhMGE1NWIyMWVkMTM4ZTIwN2FzZGZhc2RmYXN3ZWUyM2FlYQ==";
	public static final String V_2 = "MmE5NzUxNmMzNTRiNjg4NDhjZGJkOGY1NGFhc2RmYXNkZmFkZmVkMTM4ZTIwN2FkNmM1Y2JiOWMwMGFhNWFlYQ==";
	public static final String V_3 = "MmE5NzUxNmMzNTRiNjg4NDhjZGJkOGY1NGEyMjZhMGE1NWIyMWVkMTM4ZTIwN2FkNmM1Y2JiOWMwMGFhNWFlYQ==";
	public static final String V_2_2 = "ZTEyMzI4NTdjM2U3NGE3YmI4M2NhNjhlYWNiNzg3ZGFzZHUwMGFzZGtsa2ZhOXNiNjE4M2U2OGM4OTUxNTVlNQ==";
	public static final String V_3_0 = "ZTEyMzI4NTdjM2U3NGE3YmI4M2NhNjhlYWNiNzg3NWRlZTQ1NTA4NWY0NWIyNmZiNjE4M2U2OGM4OTUxNTVlNQ==";
	public static final String V_2_1 = "ZTEyMzI4NTdjM2U3NGE3YmI4M2NhNjhlYWNiNzg3NWRlZTQ1NTA4aWFhc2Q4ZmFzODk4M2U2OGM4OTUxNTVlNQ=";

	public static final String P_DAYS_PER_MOVE = "daysPerMove";
	public static final String P_USER_SIDE = "userPosition";
	public static final String P_MIN_RATING = "minRating";
	public static final String P_MAX_RATING = "maxRating";
	public static final String P_IS_RATED = "isRated";
	public static final String P_GAME_TYPE = "gameTypeCode";
	public static final String P_TYPE = "type";
	public static final String P_PRODUCT_SKU = "productSku";
	public static final String P_RELOAD = "reload";
	public static final String P_PURCHASE_DATA = "purchaseData";
	public static final String P_DATA_SIGNATURE = "dataSignature";

	public static final String P_LOCATION = "location";

	public static final int P_RANDOM = 0;
	public static final int P_WHITE = 1;
	public static final int P_BLACK = 2;

//	origin|small|large|tiny|micro
//	Description	Image size. Default is `small`.


	public static final String P_TACTICS_ID = "tacticsId";
	public static final String P_PASSED = "passed";
	public static final String P_CORRECT_MOVES = "correctMoves";
	public static final String P_SECONDS = "seconds";

	public String P_SYMBOL = !BASE_URL.equals("http://" + HOST_PRODUCTION) ? V_3 : V_3_0;

	public static final String P_FEN = "fen";

	//	SUBMIT_ECHESS_ACTION
	public static final String P_COMMAND = "command";
	public static final String P_NEW_MOVE = "newMove";
	public static final String P_TIMESTAMP = "timestamp";
	public static final String P_MESSAGE = "message";

	public static final String P_IS_INSTALL = "isInstall";

	public static final String P_KEYWORD = "keyword";
	public static final String P_DIFFICULTY = "difficulty";
	public static final String P_CATEGORY_ID = "categoryId";
	public static final String P_CATEGORY_CODE = "categoryCode";
	public static final String P_PREVIEW = "preview";
	public static final String P_OPENING = "opening";
	public static final String P_AUTHOR = "author";
	public static final String P_THEME = "theme";
	public static final String SIGNED = "signed=";
	public static final String P_FORUM_CATEGORY_ID = "forumCategoryId";
	public static final String P_ARTICLE_ID = "articleId";
	public static final String P_FORUM_TOPIC_ID = "forumTopicId";
	public static final String P_COMMENT_ID = "commentId";
	public static final String P_COURSE_ID = "courseId";
	public static final String P_TOPICS_PER_PAGE = "topicsPerPage";
	public static final String P_SUBJECT = "subject";
	public static final String P_BODY = "body";
	public static final String P_COMMENT_BODY = "commentBody";
	public static final String P_COMMENTS_PER_PAGE = "commentsPerPage";
	public static final String P_AVATAR_SIZE = "avatarSize";

	/* Returned Values */
	public static final String R_ERROR_MESSAGE = "error_message";
	public static final String R_USER_TOKEN = "user_token";
	public static final String R_FB_USER_HAS_NO_ACCOUNT = "Facebook user has no Chess.com account";
	public static final String SYMBOL_PARAMS_SPLIT = ":";
	public static final String SYMBOL_PARAMS_SPLIT_SLASH = "[|]";
	public static final String SYMBOL_ITEM_SPLIT = "[|]";

	public boolean IS_TEST_SERVER_MODE = !BASE_URL.equals("http://" + HOST_PRODUCTION);

	/* Values */
	public static final int V_BASIC_MEMBER = 0;
	public static final int V_GOLD_MEMBER = 1;
	public static final int V_PLATINUM_MEMBER = 2;
	public static final int V_DIAMOND_MEMBER = 3;

	/* Avatars sizes */
	public static final String V_AV_SIZE_ORIG = "origin";
	public static final String V_AV_SIZE_SMALL = "small"; // default on server // 200
	public static final String V_AV_SIZE_LARGE = "large"; // 450
	public static final String V_AV_SIZE_TINY = "tiny"; // 88
	public static final String V_AV_SIZE_MICRO = "micro"; // 20

	/* Theme background values */
	public static final String V_HANDSET = "handset";
	public static final String V_TABLET = "tablet";

	// old
	public static final String V_RESIGN = "RESIGN";
	public static final String V_SUBMIT = "SUBMIT";
	public static final String V_OFFERDRAW = "OFFERDRAW";
	public static final String V_ACCEPTDRAW = "ACCEPTDRAW";
	public static final String V_DECLINEDRAW = "DECLINEDRAW";
	public static final String V_CHAT = "CHAT";
	public static final String V_BASIC = "basic";

	public static final String V_ID = "id";

	public static final int V_GAME_CHESS = 1;
	public static final int V_GAME_CHESS_960 = 2;

	public static final int V_GAME_LIVE_STANDARD = 1;
	public static final int V_GAME_LIVE_BLITZ = 2;
	public static final int V_GAME_LIVE_BULLET = 3;

	public static final String V_ENCODED_MOVES = "encodedMoves";
	public static final String V_GAME_ID = "game_id";
	public static final String V_TACTICS_ID = "tactics_id";
	public static final String V_CORRECT_MOVES = "correct_moves";
	public static final String V_SECONDS = "seconds";
	public static final String V_TACTICS_RATING = "tacticsrating";
	public static final String V_USERNAME = "username";

	//	all         (0 = show only games where its users turn to move , 1 = show all users games)
	public static final String V_ONLY_USER_TURN = "0";
	public static final String V_VIDEO_LIST_CNT = "20";
	public static final String V_VIDEO_ITEM_ONE = "1";
	public static final String V_ANDROID = "android";
	public static final String V_TRUE = "1";
	public static final String V_FALSE = "0";

	//private static final String TAG = "Encode";
	public static final int MAX_ITEMS_CNT = 2000;
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String AUTHORIZATION_HEADER_VALUE = "Basic Ym9iYnk6ZmlzY2hlcg==";

	private static final String Q_ = "?";
	private static final String AND = "&";
	private static final String EQUALS = "=";


	private static String formUrl(List<NameValuePair> nameValuePairs) {
		List<NameValuePair> safeList = new ArrayList<NameValuePair>();
		safeList.addAll(nameValuePairs);
		StringBuilder builder = new StringBuilder();
		builder.append(Q_);
		String separator = Symbol.EMPTY;
		for (NameValuePair pair : safeList) {
			builder.append(separator);
			separator = AND;
			builder.append(pair.getName()).append(EQUALS).append(pair.getValue());
		}
		return builder.toString();
	}

	public static final String OBJ_START = "{";
	public static final String OBJ_END = "}";

	public static String formPostData(LoadItem loadItem) {
		List<NameValuePair> nameValuePairs = loadItem.getRequestParams();

		StringBuilder encodedParams = new StringBuilder();
		String separator = Symbol.EMPTY;
		for (NameValuePair pair : nameValuePairs) {
			encodedParams.append(separator);
			separator = AND;

			String name;
			String value;
			name = pair.getName();
			try {
				value = URLEncoder.encode(pair.getValue(), HTTP.UTF_8);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "failed to encode url");
				e.printStackTrace();
				value = pair.getValue();
			}
			encodedParams.append(name).append(EQUALS).append(value);
		}

		return encodedParams.toString();
	}

	public static int encodeServerCode(int code) {
		return StaticData.INTERNAL_ERROR | code << 8;
	}

	public static int decodeServerCode(int code) {
		return code >> 8;
	}

	public static boolean containsServerCode(int code) {
		return code > 0 && code >> 8 != 0;
	}


	private static final String TAG = "RequestJsonTask";

	public <CustomType> CustomType requestData(LoadItem loadItem, Class<CustomType> customTypeClass, Context context) throws InternalErrorException {
		CustomType item = null;
		String appId = AppUtils.getAppId(context);
		String requestMethod = loadItem.getRequestMethod();
		String url = createSignature(loadItem, appId);

		Log.d(TAG, "requesting by url = " + url);

		HttpURLConnection connection = null;

		try {
			URL urlObj = new URL(url);
			if (needSecureConnection(loadItem)) {
				KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				String algorithm = TrustManagerFactory.getDefaultAlgorithm();
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
				tmf.init(keyStore);

				SSLContext sslContext = createSslContext(context, true);
				connection = (HttpURLConnection) urlObj.openConnection();
				((HttpsURLConnection)connection).setSSLSocketFactory(sslContext.getSocketFactory());

			} else {
				connection = (HttpURLConnection) urlObj.openConnection();
				connection.setRequestMethod(requestMethod);
			}

			if (IS_TEST_SERVER_MODE) {
				connection.setRequestProperty("Authorization", getBasicAuth());
			}

			if (!TextUtils.isEmpty(loadItem.getFilePath())) { // if multiPart
				submitRawData(connection, loadItem);
			} else if (requestMethod.equals(POST) || requestMethod.equals(PUT)) {
				submitPostData(connection, loadItem);
			} else {
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + HTTP.UTF_8);
			}

			int statusCode;
			statusCode = connection.getResponseCode();

			Gson gson = new Gson();
			if (statusCode != HttpStatus.SC_OK) {
				Log.e(TAG, "Error " + statusCode + " while retrieving data from " + url);
				InputStream inputStream = connection.getErrorStream();
				String resultString = convertStreamToString(inputStream);
				Log.d(TAG, "SERVER RESPONSE: " + resultString);

				BaseResponseItem baseResponse = gson.fromJson(resultString, BaseResponseItem.class);
				Log.d(TAG, "Code: " + baseResponse.getCode() + " Message: " + baseResponse.getMessage());
				throw new InternalErrorException(encodeServerCode(baseResponse.getCode()));
			}

			InputStream inputStream = null;
			String resultString;
			try {
				inputStream = connection.getInputStream();

				resultString = convertStreamToString(inputStream);
				if (resultString.contains(OBJ_START)) {
					int firstIndex = resultString.indexOf(OBJ_START);

					int lastIndex = resultString.lastIndexOf(OBJ_END);

					resultString = resultString.substring(firstIndex, lastIndex + 1);

					Log.d(TAG, "SERVER RESPONSE: " + resultString);
					if (resultString.contains("\"challenges\":[[]")) {  // TODO remove before release
						resultString = resultString.replace("[],", "").replace("[]]", "]");
						Log.d(TAG, "After edit SERVER RESPONSE: " + resultString);
					}
				} else {
					Log.d(TAG, "ERROR -> WebRequest SERVER RESPONSE: " + resultString);
					throw new InternalErrorException(StaticData.INTERNAL_ERROR);
				}
				BaseResponseItem baseResponse = gson.fromJson(resultString, BaseResponseItem.class);
				if (baseResponse.getStatus().equals(R_STATUS_SUCCESS)) {
					item = gson.fromJson(resultString, customTypeClass);
					if (item == null) {
						throw new InternalErrorException(StaticData.EMPTY_DATA);
					}
				}
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new InternalErrorException(e, StaticData.INTERNAL_ERROR);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			Log.e(TAG, "JsonSyntaxException Error while retrieving data from " + url, e);
			throw new InternalErrorException(e, StaticData.INTERNAL_ERROR);
		} catch (IOException e) {
			if (e instanceof InternalErrorException) {
				throw new InternalErrorException(e, ((InternalErrorException) e).getCode());
			} else {
				Log.e(TAG, "I/O error while retrieving data from " + url, e);
				throw new InternalErrorException(e, StaticData.NO_NETWORK);
			}
		} catch (IllegalStateException e) {
			Log.e(TAG, "Incorrect URL: " + url, e);
			throw new InternalErrorException(e, StaticData.UNKNOWN_ERROR);
		} catch (Exception e) {
			Log.e(TAG, "Error while retrieving data from " + requestMethod + " " + url, e);
			throw new InternalErrorException(e, StaticData.UNKNOWN_ERROR);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return item;
	}

	private boolean needSecureConnection(LoadItem loadItem) {
		return (loadItem.getLoadPath().equals(CMD_LOGIN) || loadItem.getLoadPath().equals(CMD_PASSWORD)) && !IS_TEST_SERVER_MODE;
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private static String getBasicAuth() {
		return "Ym9iYnk6ZmlzY2hlcg==";
	}

	private static void submitRawData(HttpURLConnection connection, LoadItem loadItem) throws IOException {
		String charset = HTTP.UTF_8;
		File binaryFile = new File(loadItem.getFilePath());
		String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
		String CRLF = "\r\n"; // Line separator required by multipart/form-data.
		String twoHypes = "--";

		connection.setDoOutput(true);
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Cache-Control", "no-cache");
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		PrintWriter writer = null;
		try {
			OutputStream output = connection.getOutputStream();
			writer = new PrintWriter(new OutputStreamWriter(output, charset), true); // true = autoFlush, important!

			// Send Normal params
			List<NameValuePair> nameValuePairs = loadItem.getRequestParams();

			for (NameValuePair pair : nameValuePairs) {
				writer.append(twoHypes + boundary).append(CRLF);
				writer.append("Content-Disposition: form-data; name=\"" + pair.getName() + "\"").append(CRLF);
				writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
				writer.append(CRLF);
				Log.d(TAG, "POST data: name = " + pair.getName() + " value = " + pair.getValue());
				writer.append(pair.getValue()).append(CRLF).flush();
			}

			// Send binary file.
			writer.append(twoHypes + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"" + loadItem.getFileMark() + "\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
			writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
			writer.append("Content-Transfer-Encoding: binary").append(CRLF);
			writer.append(CRLF).flush();
			InputStream input = null;
			try {
				input = new FileInputStream(binaryFile);
				byte[] buffer = new byte[1024];
				for (int length = 0; (length = input.read(buffer)) > 0; ) {
					output.write(buffer, 0, length);
				}
				output.flush(); // Important! Output cannot be closed. Close of writer will close output as well.
			} finally {
				if (input != null) try {
					input.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			writer.append(CRLF).flush(); // CRLF is important! It indicates end of binary boundary.

			// End of multipart/form-data.
			writer.append(twoHypes + boundary + twoHypes).append(CRLF);
		} finally {
			if (writer != null) writer.close();
		}
	}

	private static void submitPostData(URLConnection connection, LoadItem loadItem) throws IOException {
		String query = formPostData(loadItem);
		Log.d(TAG, loadItem.getRequestMethod() + ": " + query);
		String charset = HTTP.UTF_8;
		connection.setDoOutput(true); // Triggers POST.
		OutputStream output = null;
		try {
			output = connection.getOutputStream();
			output.write(query.getBytes(charset));
		} finally {
			if (TextUtils.isEmpty(loadItem.getFilePath()) || !AppUtils.JELLYBEAN_PLUS_API) {  // don't close if we will continue to write. But close if we are on pre JB
				if (output != null) {
					try {
						output.close();
					} catch (IOException ex) {
						Log.e(TAG, "Error while submiting POST data " + ex.toString());
					}
				}
			}
		}
	}

	private String createSignature(LoadItem loadItem, String appId) {
		String requestMethod = loadItem.getRequestMethod();
		String appPart = getAppPartData(loadItem);
		String requestPath = loadItem.getLoadPath().substring(BASE_URL.length());

		String data = Q_ + formPostData(loadItem);
		if (requestMethod.equals(POST)) {
			data = formPostData(loadItem);
		}
		if (!TextUtils.isEmpty(loadItem.getFilePath())) {
			data = Symbol.EMPTY;
		}
		String signedPart = "154c4dc2f899fad29383c0cfa9905ce8143fc200";
		try {
			signedPart = SHA1(requestMethod + requestPath + data + appPart);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		String addStr = AND;
		if (requestMethod.equals(POST)) {
			data = Symbol.EMPTY;
			addStr = Q_;
		}

		if (needSecureConnection(loadItem)) {
			return BASES_S_URL + requestPath + data + addStr + SIGNED + appId + "-" + signedPart;
		} else {
			return BASE_URL + requestPath + data + addStr + SIGNED + appId + "-" + signedPart;
		}
	}

	private String getAppPartData(LoadItem loadItem) {
		String data = "2341kj23n23413nk23kj3n14";
		try {
			data = new String(Base64.decode(P_SYMBOL, Base64.DEFAULT), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return loadItem.getCode() + data;
	}

	private static String convertToHex(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (byte b : data) {
			int halfByte = (b >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				buf.append((0 <= halfByte) && (halfByte <= 9) ? (char) ('0' + halfByte) : (char) ('a' + (halfByte - 10)));
				halfByte = b & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	private static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		byte[] sha1hash = md.digest();
		return convertToHex(sha1hash);
	}

	private SSLContext createSslContext(Context context, boolean clientAuth) throws GeneralSecurityException {
		KeyStore trustStore = loadTrustStore(context);
		KeyStore keyStore = loadKeyStore(context);

		MyTrustManager myTrustManager = new MyTrustManager(trustStore);
		TrustManager[] tms = new TrustManager[] { myTrustManager };

		KeyManager[] kms = null;
		if (clientAuth) {
			KeyManagerFactory kmf = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, KEYSTORE_PASSWORD.toCharArray());
			kms = kmf.getKeyManagers();
		}

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(kms, tms, null);

		return sslContext;
	}

	private KeyStore loadTrustStore(Context context) {
		try {
			KeyStore localTrustStore = KeyStore.getInstance("BKS");
			InputStream in = context.getResources().openRawResource(R.raw.mykeystore);
			try {
				localTrustStore.load(in, TRUSTSTORE_PASSWORD.toCharArray());
			} finally {
				in.close();
			}

			return localTrustStore;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	private KeyStore keyStore;

	private static final String TRUSTSTORE_PASSWORD = "asd234p";
	private static final String KEYSTORE_PASSWORD = "asd234p";

	// use http://transoceanic.blogspot.ru/2011/11/android-import-ssl-certificate-and-use.html

	private KeyStore loadKeyStore(Context context) {
		if (keyStore != null) {
			return keyStore;
		}

		try {
			keyStore = KeyStore.getInstance("BKS");
			InputStream in = context.getResources().openRawResource(R.raw.mykeystore);
			try {
				keyStore.load(in, KEYSTORE_PASSWORD.toCharArray());
			} finally {
				in.close();
			}

			return keyStore;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <ItemType> String parseJsonToString(ItemType jRequest) {
		Gson gson = new Gson();
		return gson.toJson(jRequest);
	}

	public static String convertStreamToString(InputStream is) {
		Scanner scanner = new Scanner(is).useDelimiter("\\A");
		return scanner.hasNext() ? scanner.next() : "";
	}

	public String getOnlineGameLink(long gameId) {
		return BASE_URL + "/echess/game?id=" + gameId;
	}

	public String getLiveGameLink(long gameId) {
		return BASE_URL + "/livechess/game?id=" + gameId;
	}

}
