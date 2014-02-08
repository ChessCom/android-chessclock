package com.chess.lcc.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.LoginItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.lcc.android.interfaces.LiveUiUpdateListener;
import com.chess.model.DataHolder;
import com.chess.statics.AppConstants;
import com.chess.statics.AppData;
import com.chess.ui.interfaces.LoginErrorUpdateListener;
import com.chess.utilities.AppUtils;

/**
 * Created by vm on 04.02.14.
 */
public class LiveConnector { // todo: move code to LCH

	private static final String TAG = "LCCLOG-LiveConnector";
	private static final int NEXT_CONNECTION_DELAY = 5000;

	//	private Handler handler;
	private Context context;
	private AppData appData;
	private LiveUiUpdateListener liveUiUpdateListener;
	private LoginErrorUpdateListener loginErrorUpdateListener;
	//private boolean needReLoginToLive; // lets try to avoid this state

	public LiveConnector(Context context, LiveUiUpdateListener liveUiUpdateListener, LoginErrorUpdateListener loginErrorUpdateListener) {
		this.context = context;
		this.liveUiUpdateListener = liveUiUpdateListener;
		this.loginErrorUpdateListener = loginErrorUpdateListener;

		appData = new AppData(context);
//		handler = new Handler();
	}

	/*private Runnable sessionIdCheckRunnable = new Runnable() {
		@Override
		public void run() {
			sessionIdCheck();
		}
	};

	private void sessionIdCheck() {
		LoadItem loadItem = LoadHelper.getUserInfo(appData.getUserToken());
		loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.V_SESSION_ID);

		new RequestJsonTask<UserItem>(new SessionIdUpdateListener()).executeTask(loadItem);
	}

	private class SessionIdUpdateListener extends AbstractUpdateListener<UserItem> {

		public SessionIdUpdateListener() {
			super(context, UserItem.class);
		}

		@Override
		public void updateData(UserItem returnedObj) {
			super.updateData(returnedObj);

			if (TextUtils.isEmpty(returnedObj.getData().getSessionId())) { // if API was not updated to get a single sessionId field
				// we perform re-login
				performReloginForLive();
				//needReLoginToLive = true;
				return;
			} else {
				appData.setLiveSessionId(returnedObj.getData().getSessionId());
			}

			if (liveUiUpdateListener != null) {
				liveUiUpdateListener.performServiceConnection();
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			//LogMe.dl(TAG, "SessionIdUpdateListener errorHandle resultCode=" + resultCode);
		}
	}*/

	public void performReloginForLive() {
		Log.d(TAG, "performReloginForLive");

		/*
		logout();
		unBindAndStopLiveService();
		*/

		String password = appData.getPassword();
		if (!TextUtils.isEmpty(password)) {

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_LOGIN);
			loadItem.setRequestMethod(RestHelper.POST);
			loadItem.addRequestParams(RestHelper.P_DEVICE_ID, AppUtils.getDeviceId(context));
			loadItem.addRequestParams(RestHelper.P_USER_NAME_OR_MAIL, appData.getUsername());
			loadItem.addRequestParams(RestHelper.P_PASSWORD, password);
			loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.P_USERNAME);
			loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.P_TACTICS_RATING);

			new RequestJsonTask<LoginItem>(new LoginUpdateListener()).executeTask(loadItem);

		} else if (!TextUtils.isEmpty(appData.getFacebookToken())) {

			String accessToken = appData.getFacebookToken();

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_LOGIN);
			loadItem.setRequestMethod(RestHelper.POST);
			loadItem.addRequestParams(RestHelper.P_FACEBOOK_ACCESS_TOKEN, accessToken);
			loadItem.addRequestParams(RestHelper.P_DEVICE_ID, AppUtils.getDeviceId(context));
			loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.V_USERNAME);
			loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.V_TACTICS_RATING);

			FacebookLoginUpdateListener facebookLoginUpdateListener =
					new FacebookLoginUpdateListener(context, accessToken, loginErrorUpdateListener);

			new RequestJsonTask<LoginItem>(facebookLoginUpdateListener).executeTask(loadItem);
		}

		//needReLoginToLive = true;
	}

	/*private void performReloginForLiveDelayed() {
//		handler.postDelayed(performReloginForLiveRunnable, NEXT_CONNECTION_DELAY);
	}

	private Runnable performReloginForLiveRunnable = new Runnable() {
		@Override
		public void run() {
			performReloginForLive();
		}
	};*/

	private class LoginUpdateListener extends AbstractUpdateListener<LoginItem> {
		public LoginUpdateListener() {
			super(context, LoginItem.class);
		}

		@Override
		public void showProgress(boolean show) { // DO not show progress as we already showing it while making first attempt to connect
		}

		@Override
		public void updateData(LoginItem returnedObj) {

			LoginItem.Data loginData = returnedObj.getData();

			SharedPreferences.Editor preferencesEditor = appData.getEditor();

			String username = loginData.getUsername();
			if (!TextUtils.isEmpty(username)) {
				preferencesEditor.putString(AppConstants.USERNAME, username);
			}
			preferencesEditor.putInt(AppConstants.USER_PREMIUM_STATUS, loginData.getPremiumStatus());
			preferencesEditor.putString(AppConstants.LIVE_SESSION_ID, loginData.getSessionId());
			preferencesEditor.putLong(AppConstants.LIVE_SESSION_ID_SAVE_TIME, System.currentTimeMillis());
			preferencesEditor.putString(AppConstants.USER_TOKEN, loginData.getLoginToken());
			preferencesEditor.putLong(AppConstants.USER_TOKEN_SAVE_TIME, System.currentTimeMillis());
			preferencesEditor.commit();

			liveUiUpdateListener.registerGcm();
			DataHolder.getInstance().setLiveChessMode(true);
			Log.d(TAG, "LBA LoginUpdateListener -> updateData");

			liveUiUpdateListener.performServiceConnection();
		}

		@Override
		public void errorHandle(Integer resultCode) {

			//LogMe.dl(TAG, "LoginUpdateListener resultCode=" + resultCode);
//			performReloginForLiveDelayed(); // todo: to vm: this is incorrect logic you already tried to re-login, but failed, and you even don't know why, but you still try to do this. this will end up with blocked account.

			// show message only for re-login and app update     // todo: to vm: why this code is commented and there is no handle for provided error codes???
			/*if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.ACCESS_DENIED_CODE) { // handled in CommonLogicFragment
					String message = getString(R.string.version_is_obsolete_update);
					safeShowSinglePopupDialog(R.string.error, message);
					return;
				} else if (serverCode != ServerErrorCodes.INVALID_LOGIN_TOKEN_SUPPLIED) { // handled in CommonLogicFragment
					String serverMessage = ServerErrorCodes.getUserFriendlyMessage(LiveBaseActivity.this, serverCode); // TODO restore

					safeShowSinglePopupDialog(R.string.error, serverMessage);
					return;
				}
			}*/
			super.errorHandle(resultCode);
		}
	}

	private class FacebookLoginUpdateListener extends com.chess.backend.interfaces.LoginUpdateListener {

		public FacebookLoginUpdateListener(Context context, String facebookToken, LoginErrorUpdateListener loginErrorUpdateListener) {
			super(context, facebookToken, loginErrorUpdateListener);
		}

		@Override
		public void updateData(LoginItem returnedObj) {
			super.updateData(returnedObj);

			//if (needReLoginToLive) {
			DataHolder.getInstance().setLiveChessMode(true);
			liveUiUpdateListener.performServiceConnection();
			//}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
		}
	}

//	public void removeCallbacks() {
//		handler.removeCallbacks(sessionIdCheckRunnable);
//		handler.removeCallbacks(performReloginForLiveRunnable);
//	}
}
