package com.chess.backend;

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
}
