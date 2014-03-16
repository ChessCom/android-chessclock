package com.chess.backend.interfaces;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.LoginItem;
import com.chess.model.DataHolder;
import com.chess.model.TacticsDataHolder;
import com.chess.statics.AppConstants;
import com.chess.statics.AppData;
import com.chess.statics.FlurryData;
import com.chess.statics.Symbol;
import com.chess.ui.activities.BaseFragmentPopupsActivity;
import com.chess.ui.interfaces.PopupShowFace;
import com.flurry.android.FlurryAgent;

/**
 * Created by vm on 03.02.14.
 */

public class LoginUpdateListener extends AbstractUpdateListener<LoginItem> {
	private String facebookToken;
	private final PopupShowFace popupShowFace;

	private AppData appData;

	public LoginUpdateListener(Context context, String facebookToken, PopupShowFace popupShowFace) {
		super(context, LoginItem.class);

		this.facebookToken = facebookToken;
		this.popupShowFace = popupShowFace;

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
			appData.setUsername(username);
		}
		preferencesEditor.putInt(username + AppConstants.USER_PREMIUM_STATUS, loginData.getPremiumStatus());
		preferencesEditor.putLong(username + AppConstants.PREF_USER_ID, loginData.getUserId());
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
					if (popupShowFace != null) {
						popupShowFace.showSinglePopupDialog(R.string.login, R.string.invalid_username_or_password);
						appData.setPassword(Symbol.EMPTY);
					}
					return;
				case ServerErrorCodes.FACEBOOK_USER_NO_ACCOUNT:
					if (popupShowFace != null) {
						popupShowFace.setPositiveBtnId(R.string.sign_up);
						popupShowFace.showPopupDialog(R.string.no_chess_account_signup_please, BaseFragmentPopupsActivity.CHESS_NO_ACCOUNT_TAG);
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
