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
		loadItem.addRequestParams(RestHelper.P_FIELDS, filter);
		return loadItem;
	}

	public static LoadItem getAllGames(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_GAMES_ALL);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem getFriends(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_FRIENDS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem getMembershipDetails(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_MEMBERSHIP);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem postMembershipUpdate(String userToken, String originalJson, String signature) {
		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.setLoadPath(RestHelper.CMD_MEMBERSHIP);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(RestHelper.P_PURCHASE_DATA, originalJson);
		loadItem.addRequestParams(RestHelper.P_DATA_SIGNATURE, signature);
		return loadItem;
	}

	public static LoadItem getGameById(String userToken, long gameId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_GAMES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(RestHelper.P_GAME_ID, gameId);
		return loadItem;
	}

	public static LoadItem putGameAction(String userToken, long gameId, String command, long timestamp) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_PUT_GAME_ACTION(gameId));
		loadItem.setRequestMethod(RestHelper.PUT);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(RestHelper.P_COMMAND, command);
		loadItem.addRequestParams(RestHelper.P_TIMESTAMP, timestamp);
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
		loadItem.setLoadPath(RestHelper.CMD_ANSWER_GAME_SEEK(gameId));
		loadItem.setRequestMethod(command);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem deleteVacation(String userToken) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_VACATIONS);
		loadItem.setRequestMethod(RestHelper.DELETE);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
		return loadItem;
	}

	public static LoadItem postGameSeek(String userToken, int days, int color, int isRated, int gameType, String opponentName) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_SEEKS);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(RestHelper.P_DAYS_PER_MOVE, days);
		loadItem.addRequestParams(RestHelper.P_USER_SIDE, color);
		loadItem.addRequestParams(RestHelper.P_IS_RATED, isRated);
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, gameType);
		if (!TextUtils.isEmpty(opponentName)) {
			loadItem.addRequestParams(RestHelper.P_OPPONENT, opponentName);
		}
		return loadItem;
	}

	public static LoadItem postFriend(String userToken, String username, String message) {
		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.setLoadPath(RestHelper.CMD_FRIENDS_REQUEST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(RestHelper.P_USERNAME, username);
		loadItem.addRequestParams(RestHelper.P_MESSAGE, message);
		return loadItem;
	}

	public static LoadItem postUserProfile(String userToken, String firstName, String lastName, int userCountryId, int userSkill) {
		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.setLoadPath(RestHelper.CMD_USER_PROFILE);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
		loadItem.addRequestParams(RestHelper.P_FIRST_NAME, firstName);
		loadItem.addRequestParams(RestHelper.P_LAST_NAME, lastName);
		loadItem.addRequestParams(RestHelper.P_COUNTRY_ID, userCountryId);
		loadItem.addRequestParams(RestHelper.P_SKILL_LEVEL, userSkill);
		return loadItem;
	}

	public static LoadItem postUsers(String userName, String password, String email, String deviceId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_USERS);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_USERNAME, userName);
		loadItem.addRequestParams(RestHelper.P_PASSWORD, password);
		loadItem.addRequestParams(RestHelper.P_EMAIL, email);
		loadItem.addRequestParams(RestHelper.P_APP_TYPE, RestHelper.V_ANDROID);
		loadItem.addRequestParams(RestHelper.P_DEVICE_ID, deviceId);
		return loadItem;
	}
}
