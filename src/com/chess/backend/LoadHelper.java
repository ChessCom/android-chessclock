package com.chess.backend;

import android.text.TextUtils;
import com.chess.ui.engine.configs.DailyGameConfig;

import static com.chess.backend.RestHelper.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.06.13
 * Time: 16:49
 */
public class LoadHelper {

	public static LoadItem getUserInfo(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_USERS);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem getUserInfo(String userToken, String username) {
		LoadItem loadItem = getUserInfo(userToken);
		loadItem.addRequestParams(P_USERNAME, username);
		return loadItem;
	}

	public static LoadItem getTacticsBasicStats(String userToken) {
		LoadItem loadItem = getTacticsStats(userToken);
		loadItem.addRequestParams(P_TYPE, V_BASIC);
		return loadItem;
	}

	public static LoadItem getTacticsStats(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_TACTICS_STATS);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem getAllGamesFiltered(String userToken, String filter) {
		LoadItem loadItem = getAllGames(userToken);
		loadItem.addRequestParams(P_FIELDS, filter);
		return loadItem;
	}

	public static LoadItem getAllGames(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_GAMES_ALL);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_AVATAR_SIZE, RestHelper.V_AV_SIZE_TINY);
		return loadItem;
	}

	public static LoadItem getCurrentMyTurnGames(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_GAMES_CURRENT);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_GAME_TYPE_ID, RestHelper.V_GAME_CHESS);
		loadItem.addRequestParams(P_MY_TURN_ONLY, RestHelper.V_TRUE);
		loadItem.addRequestParams(P_FIELDS, RestHelper.V_ID);
		return loadItem;
	}

	public static LoadItem getFriends(String userToken, String username) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_FRIENDS);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_AVATAR_SIZE, RestHelper.V_AV_SIZE_SMALL);
		loadItem.addRequestParams(P_USERNAME, username);
		return loadItem;
	}

	public static LoadItem getMembershipDetails(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_MEMBERSHIP);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem postMembershipUpdate(String userToken, String originalJson, String signature) {
		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(POST);
		loadItem.setLoadPath(getInstance().CMD_MEMBERSHIP);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_PURCHASE_DATA, originalJson);
		loadItem.addRequestParams(P_DATA_SIGNATURE, signature);
		return loadItem;
	}

	public static LoadItem getGameById(String userToken, long gameId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_GAME_BY_ID(gameId));
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem putGameAction(String userToken, long gameId, String command, long timestamp) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_PUT_GAME_ACTION(gameId));
		loadItem.setRequestMethod(PUT);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_COMMAND, command);
		loadItem.addRequestParams(P_TIMESTAMP, timestamp);
		return loadItem;
	}

	public static LoadItem acceptChallenge(String loginToken, long gameId) {
		return answerGameSeek(loginToken, gameId, PUT);
	}

	public static LoadItem declineChallenge(String loginToken, long gameId) {
		return answerGameSeek(loginToken, gameId, DELETE);
	}

	public static LoadItem answerGameSeek(String loginToken, long gameId, String command) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_ANSWER_GAME_SEEK(gameId));
		loadItem.setRequestMethod(command);
		loadItem.addRequestParams(P_LOGIN_TOKEN, loginToken);
		return loadItem;
	}

	public static LoadItem acceptFriendRequest(String loginToken, long id) {
		return answerFriendRequest(loginToken, id, PUT);
	}

	public static LoadItem declineFriendRequest(String loginToken, long id) {
		return answerFriendRequest(loginToken, id, DELETE);
	}

	public static LoadItem answerFriendRequest(String userToken, long id, String command) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_FRIENDS_REQUEST_BY_ID(id));
		loadItem.setRequestMethod(command);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem deleteOnVacation(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_VACATIONS);
		loadItem.setRequestMethod(DELETE);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem getOnVacation(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_VACATIONS);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem postOnVacation(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_VACATIONS);
		loadItem.setRequestMethod(POST);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem postGameSeek(String userToken, DailyGameConfig dailyGameConfig) {
		int days = dailyGameConfig.getDaysPerMove();
		int gameType = dailyGameConfig.getGameType();
		int isRated = dailyGameConfig.isRated() ? 1 : 0;
		String opponentName = dailyGameConfig.getOpponentName();
		int minRating = dailyGameConfig.getMinRating();
		int maxRating = dailyGameConfig.getMaxRating();
		return postGameSeek(userToken, days, isRated, gameType, opponentName, minRating, maxRating);
	}

	public static LoadItem postGameSeek(String userToken, int days, int isRated, int gameType, String opponentName,
										int minRating, int maxRating) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_SEEKS);
		loadItem.setRequestMethod(POST);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_DAYS_PER_MOVE, days);
		loadItem.addRequestParams(P_USER_SIDE, RestHelper.P_RANDOM); // always random!
		loadItem.addRequestParams(P_IS_RATED, isRated);
		loadItem.addRequestParams(P_GAME_TYPE_CODE_ID, gameType);
		if (!TextUtils.isEmpty(opponentName)) {
			loadItem.addRequestParams(P_OPPONENT, opponentName);
		} else {
			loadItem.addRequestParams(P_MIN_RATING, minRating);
			loadItem.addRequestParams(P_MAX_RATING, maxRating);
		}
		return loadItem;
	}

	public static LoadItem postFriend(String userToken, String username, String message) {
		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(POST);
		loadItem.setLoadPath(getInstance().CMD_FRIENDS_REQUEST);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_USERNAME, username);
		loadItem.addRequestParams(P_MESSAGE, message);
		return loadItem;
	}

	public static LoadItem postFriendByEmail(String userToken, String email, String message) {
		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(POST);
		loadItem.setLoadPath(getInstance().CMD_FRIENDS_REQUEST);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_EMAIL, email);
		loadItem.addRequestParams(P_MESSAGE, message);
		return loadItem;
	}

	public static LoadItem postUserProfile(String userToken, String firstName, String lastName, int userCountryId, int userSkill) {
		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(POST);
		loadItem.setLoadPath(getInstance().CMD_USER_PROFILE);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_FIRST_NAME, firstName);
		loadItem.addRequestParams(P_LAST_NAME, lastName);
		loadItem.addRequestParams(P_COUNTRY_ID, userCountryId);
		loadItem.addRequestParams(P_SKILL_LEVEL, userSkill);
		return loadItem;
	}

	public static LoadItem postUsers(String username, String password, String email, String deviceId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_USERS);
		loadItem.setRequestMethod(POST);
		loadItem.addRequestParams(P_USERNAME, username);
		loadItem.addRequestParams(P_PASSWORD, password);
		loadItem.addRequestParams(P_EMAIL, email);
		loadItem.addRequestParams(P_APP_TYPE, V_ANDROID);
		loadItem.addRequestParams(P_DEVICE_ID, deviceId);
		return loadItem;
	}

	public static LoadItem getForumTopicsForCategory(String userToken, int categoryId, int page) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_FORUMS_TOPICS);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_FORUM_CATEGORY_ID, categoryId);
		loadItem.addRequestParams(P_PAGE, page);
		loadItem.addRequestParams(P_TOPICS_PER_PAGE, DEFAULT_ITEMS_PER_PAGE);
		return loadItem;
	}

	public static LoadItem getForumPostsForTopic(String userToken, int topicId, int page) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_FORUMS_COMMENTS);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_FORUM_TOPIC_ID, topicId);
		loadItem.addRequestParams(P_PAGE, page);
		loadItem.addRequestParams(P_COMMENTS_PER_PAGE, DEFAULT_ITEMS_PER_PAGE);
		return loadItem;
	}

	/* Lesssons */
	public static LoadItem getLessonsByCourseId(String userToken, int courseId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_LESSONS);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_COURSE_ID, courseId);
		return loadItem;
	}

	public static LoadItem getLessonsRating(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_LESSONS_RATING);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	/* Explorer */
	public static LoadItem getExplorerMoves(String userToken, String fen) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_EXPLORERS_MOVES);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_FEN, fen);
		return loadItem;
	}

	/* Themes */
	public static LoadItem getBackgroundById(String userToken, int id, int width, int height, String screen) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_BACKGROUND_BY_ID(id));
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_WIDTH, width);
		loadItem.addRequestParams(P_HEIGHT, height);
		loadItem.addRequestParams(P_SCREEN, screen);
		return loadItem;
	}

	public static LoadItem getBoardById(String userToken, int id) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_BOARD_BY_ID(id));
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem getPiecesById(String userToken, int id) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_PIECES_BY_ID(id));
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem getSoundsById(String userToken, int id) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_SOUND_BY_ID(id));
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem getServerStats() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(getInstance().CMD_STATS);
		return loadItem;
	}
}
