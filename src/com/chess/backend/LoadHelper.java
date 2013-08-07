package com.chess.backend;

import android.text.TextUtils;
import com.chess.backend.entity.LoadItem;

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
		loadItem.setLoadPath(CMD_USERS);
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
		loadItem.setLoadPath(CMD_TACTICS_STATS);
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
		loadItem.setLoadPath(CMD_GAMES_ALL);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem getFriends(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(CMD_FRIENDS);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem getMembershipDetails(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(CMD_MEMBERSHIP);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem postMembershipUpdate(String userToken, String originalJson, String signature) {
		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(POST);
		loadItem.setLoadPath(CMD_MEMBERSHIP);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_PURCHASE_DATA, originalJson);
		loadItem.addRequestParams(P_DATA_SIGNATURE, signature);
		return loadItem;
	}

	public static LoadItem getGameById(String userToken, long gameId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(CMD_GAMES);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_GAME_ID, gameId);
		return loadItem;
	}

	public static LoadItem putGameAction(String userToken, long gameId, String command, long timestamp) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(CMD_PUT_GAME_ACTION(gameId));
		loadItem.setRequestMethod(PUT);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_COMMAND, command);
		loadItem.addRequestParams(P_TIMESTAMP, timestamp);
		return loadItem;
	}

	public static LoadItem acceptChallenge(String userToken, long gameId) {
		return answerGameSeek(userToken, gameId, PUT);
	}

	public static LoadItem declineChallenge(String userToken, long gameId) {
		return answerGameSeek(userToken, gameId, DELETE);
	}

	public static LoadItem answerGameSeek(String userToken, long gameId, String command) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(CMD_ANSWER_GAME_SEEK(gameId));
		loadItem.setRequestMethod(command);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem deleteVacation(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(CMD_VACATIONS);
		loadItem.setRequestMethod(DELETE);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem postGameSeek(String userToken, int days, int color, int isRated, int gameType, String opponentName) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(CMD_SEEKS);
		loadItem.setRequestMethod(POST);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_DAYS_PER_MOVE, days);
		loadItem.addRequestParams(P_USER_SIDE, color);
		loadItem.addRequestParams(P_IS_RATED, isRated);
		loadItem.addRequestParams(P_GAME_TYPE, gameType);
		if (!TextUtils.isEmpty(opponentName)) {
			loadItem.addRequestParams(P_OPPONENT, opponentName);
		}
		return loadItem;
	}

	public static LoadItem postFriend(String userToken, String username, String message) {
		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(POST);
		loadItem.setLoadPath(CMD_FRIENDS_REQUEST);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_USERNAME, username);
		loadItem.addRequestParams(P_MESSAGE, message);
		return loadItem;
	}

	public static LoadItem postFriendByEmail(String userToken, String email, String message) {
		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(POST);
		loadItem.setLoadPath(CMD_FRIENDS_REQUEST);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_EMAIL, email);
		loadItem.addRequestParams(P_MESSAGE, message);
		return loadItem;
	}

	public static LoadItem postUserProfile(String userToken, String firstName, String lastName, int userCountryId, int userSkill) {
		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(POST);
		loadItem.setLoadPath(CMD_USER_PROFILE);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_FIRST_NAME, firstName);
		loadItem.addRequestParams(P_LAST_NAME, lastName);
		loadItem.addRequestParams(P_COUNTRY_ID, userCountryId);
		loadItem.addRequestParams(P_SKILL_LEVEL, userSkill);
		return loadItem;
	}

	public static LoadItem postUsers(String userName, String password, String email, String deviceId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(CMD_USERS);
		loadItem.setRequestMethod(POST);
		loadItem.addRequestParams(P_USERNAME, userName);
		loadItem.addRequestParams(P_PASSWORD, password);
		loadItem.addRequestParams(P_EMAIL, email);
		loadItem.addRequestParams(P_APP_TYPE, V_ANDROID);
		loadItem.addRequestParams(P_DEVICE_ID, deviceId);
		return loadItem;
	}

	public static LoadItem getForumTopicsForCategory(String userToken, int categoryId, int page) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(CMD_FORUMS_TOPICS);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_FORUM_CATEGORY_ID, categoryId);
		loadItem.addRequestParams(P_PAGE_, page);
		loadItem.addRequestParams(P_TOPICS_PER_PAGE, DEFAULT_ITEMS_PER_PAGE);
		return loadItem;
	}

	public static LoadItem getForumPostsForTopic(String userToken, int topicId, int page) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(CMD_FORUMS_COMMENTS);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_FORUM_TOPIC_ID, topicId);
		loadItem.addRequestParams(P_PAGE_, page);
		loadItem.addRequestParams(P_COMMENTS_PER_PAGE, DEFAULT_ITEMS_PER_PAGE);
		return loadItem;
	}

	public static LoadItem getLessonsByCourseId(String userToken, int courseId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(CMD_LESSONS);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(P_COURSE_ID, courseId);
		return loadItem;
	}

	public static LoadItem getLessonsRating(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(CMD_LESSONS_RATING);
		loadItem.addRequestParams(P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

}
