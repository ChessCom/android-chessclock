package com.chess.backend;

import com.chess.backend.entity.LoadItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.06.13
 * Time: 16:49
 */
public class LoadHelper {

	public static LoadItem getUserInfo(String userToken) {
		return getUserInfo(userToken, null);
	}

	public static LoadItem getUserInfo(String userToken, String username) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_USERS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
		if (username != null)
			loadItem.addRequestParams(RestHelper.P_USERNAME, username);

		return loadItem;
	}
}
