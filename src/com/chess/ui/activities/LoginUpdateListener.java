package com.chess.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.LoginItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.model.DataHolder;
import com.chess.model.TacticsDataHolder;
import com.chess.statics.AppConstants;
import com.chess.statics.AppData;
import com.chess.statics.FlurryData;
import com.chess.ui.interfaces.LoginErrorUpdateListener;
import com.flurry.android.FlurryAgent;

/**
 * Created by vm on 03.02.14.
 */

// todo: move to appropriate package

public class LoginUpdateListener extends AbstractUpdateListener<LoginItem> {
	private String facebookToken;
	private final LoginErrorUpdateListener loginErrorUpdateListener;

	private AppData appData;

	public LoginUpdateListener(Context context, String facebookToken, LoginErrorUpdateListener loginErrorUpdateListener) {
		super(context, LoginItem.class);

		this.facebookToken = facebookToken;
		this.loginErrorUpdateListener = loginErrorUpdateListener;

		appData = new AppData(context);
	}

	@Override
	public void showProgress(boolean show) {
		// don't show overlay
	}

	@Override
	public void updateData(LoginItem returnedObj) {
		LoginItem.Data loginData = returnedObj.getData();
		String username = loginData.getUsername();
		SharedPreferences.Editor preferencesEditor = appData.getPreferences().edit();

		if (!TextUtils.isEmpty(username)) {
			preferencesEditor.putString(AppConstants.USERNAME, username);
		}
		preferencesEditor.putInt(username + AppConstants.USER_PREMIUM_STATUS, loginData.getPremiumStatus());
		preferencesEditor.putString(AppConstants.LIVE_SESSION_ID, loginData.getSessionId());
		preferencesEditor.putLong(AppConstants.LIVE_SESSION_ID_SAVE_TIME, System.currentTimeMillis());
		preferencesEditor.commit();

		if (!TextUtils.isEmpty(facebookToken)) {
			FlurryAgent.logEvent(FlurryData.FB_LOGIN);
			// save facebook access token to appData for future re-login
			appData.setFacebookToken(facebookToken);
		}

		preferencesEditor.putString(AppConstants.USER_TOKEN, loginData.getLoginToken());
		preferencesEditor.putLong(AppConstants.USER_TOKEN_SAVE_TIME, System.currentTimeMillis());
		preferencesEditor.commit();

		DataHolder.getInstance().setLiveChessMode(false);
		DataHolder.reset();
		TacticsDataHolder.reset();
	}

	@Override
	public void errorHandle(Integer resultCode) {
		if (RestHelper.containsServerCode(resultCode)) {
			// get server code
			int serverCode = RestHelper.decodeServerCode(resultCode);
			switch (serverCode) {
				case ServerErrorCodes.INVALID_USERNAME_PASSWORD:
					if (loginErrorUpdateListener != null) {
						loginErrorUpdateListener.onInvalidLoginCredentials();
					}
					return;
				case ServerErrorCodes.FACEBOOK_USER_NO_ACCOUNT:
					if (loginErrorUpdateListener != null) {
						loginErrorUpdateListener.onFacebookUserNoAccount();
					}
					return;
				default:
					super.errorHandle(resultCode);
					return;
			}
		}
		super.errorHandle(resultCode);
	}
}
