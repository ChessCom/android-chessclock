package com.chess.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.GcmItem;
import com.chess.backend.entity.api.LoginItem;
import com.chess.backend.entity.api.RegisterItem;
import com.chess.backend.gcm.GcmHelper;
import com.chess.backend.image_load.bitmapfun.ImageCache;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.model.DataHolder;
import com.chess.model.TacticsDataHolder;
import com.chess.statics.*;
import com.chess.utilities.AppUtils;
import com.facebook.Session;
import com.facebook.SessionState;
import com.flurry.android.FlurryAgent;
import com.google.android.gcm.GCMRegistrar;

import java.util.Locale;


//import com.facebook.android.Facebook;

/**
 * CommonLogicActivity class
 *
 * @author alien_roger
 * @created at: 23.09.12 8:10
 */
public abstract class CommonLogicActivity extends BaseFragmentPopupsActivity {

	private String currentLocale;

	protected Handler handler;
	protected boolean isRestarted;
	private AppData appData;
	protected SharedPreferences preferences;
	protected SharedPreferences.Editor preferencesEditor;
	protected boolean isTablet;
	private CommonLogicActivity.GcmRegisterUpdateListener gcmRegisterUpdateListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		isTablet = AppUtils.isTablet(this);

		appData = new AppData(this);
		preferences = appData.getPreferences();
		preferencesEditor = appData.getEditor();

//		currentLocale = preferences.getString(AppConstants.CURRENT_LOCALE, StaticData.LOCALE_EN);
		currentLocale = StaticData.LOCALE_EN;

		handler = new Handler();
		setLocale();

		gcmRegisterUpdateListener = new GcmRegisterUpdateListener();
	}

	protected AppData getAppData() {
		return appData == null ? new AppData(this) : appData;
	}

	protected String getCurrentUsername() {
		return getAppData().getUsername();
	}

	protected String getCurrentUserToken() {
		return getAppData().getUserToken();
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		isRestarted = true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, FlurryData.API_KEY);

		if (getAppData().getFirstTimeStart() == 0) {
			getAppData().setFirstTimeStart(System.currentTimeMillis());
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, 0);
			preferencesEditor.commit();
		}
	}


	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);

		isRestarted = false;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && (requestCode & 0xFFFF) == 0xFACE) { // if it was request to authorize facebook user
			for (Fragment fragment : getSupportFragmentManager().getFragments()) { // transmit to all fragments? is it safe..? // TODO check logic
				if (fragment != null) {
					fragment.onActivityResult(requestCode & 0xffff, resultCode, data);
				}
			}
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setLocale();
	}

	protected void setLocale() {
		String prevLang = getResources().getConfiguration().locale.getLanguage();
		String[] languageCodes = getResources().getStringArray(R.array.languages_codes);

		String setLocale = languageCodes[appData.getLanguageCode()];

//		if(!prevLang.equals(setLocale)) {
		Locale locale = new Locale(setLocale);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getResources().updateConfiguration(config, getResources().getDisplayMetrics());

//			preferencesEditor.putString(AppConstants.CURRENT_LOCALE, setLocale);
//			preferencesEditor.commit();

		currentLocale = setLocale;
//		}
	}

	protected void restartActivity() {
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

	protected void registerGcmService() {
		/* When an application is updated, it should invalidate its existing registration ID.
		The best way to achieve this validation is by storing the current
		 application version when a registration ID is stored.
		 Then when the application is started, compare the stored value
		 with the current application version.
		 If they do not match, invalidate the stored data and start the registration process again.
		 */

		final String registrationId = GCMRegistrar.getRegistrationId(this);
		if (TextUtils.isEmpty(registrationId)) {
			// Automatically registers application on startup.
			GCMRegistrar.register(this, GcmHelper.SENDER_ID);
		} else {
			// Device is already registered on GCM, check our server.
			if (GCMRegistrar.isRegisteredOnServer(this) && appData.isRegisterOnChessGCM()) {
				// Skips registration.
			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.getInstance().CMD_GCM);
				loadItem.setRequestMethod(RestHelper.POST);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, appData.getUserToken());
				loadItem.addRequestParams(RestHelper.GCM_P_REGISTER_ID, registrationId);

				new RequestJsonTask<GcmItem>(gcmRegisterUpdateListener).execute(loadItem);
			}
		}
	}

	protected class GcmRegisterUpdateListener extends AbstractUpdateListener<GcmItem> {

		public GcmRegisterUpdateListener() {
			super(CommonLogicActivity.this, GcmItem.class);
		}

		@Override
		public void updateData(GcmItem returnedObj) {
			GCMRegistrar.setRegisteredOnServer(getContext(), true);
			appData.registerOnChessGCM(appData.getUserToken());
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.YOUR_GCM_ID_ALREADY_REGISTERED) {
					GCMRegistrar.setRegisteredOnServer(getContext(), true);
					appData.registerOnChessGCM(appData.getUserToken());
					Log.d("GCMIntentService", "Already registered on server -> Re-registering GCM");
				} else {
					super.errorHandle(resultCode);
				}
			} else {
				super.errorHandle(resultCode);
			}
		}
	}

	protected String getDeviceId() {
		String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
		if (TextUtils.isEmpty(deviceId)) {
			deviceId = getAppData().getDeviceId();
			if (TextUtils.isEmpty(deviceId)) { // generate a new one
				deviceId = "Hello" +  (Math.random() * 100) + "There" + System.currentTimeMillis();
				getAppData().setDeviceId(deviceId);
			}
		}

		deviceId = ImageCache.hashKeyForDisk(deviceId);
		return deviceId.substring(0, 32);
	}

	protected class LoginUpdateListener extends AbstractUpdateListener<LoginItem> {
		private String facebookToken;

		public LoginUpdateListener(String facebookToken) {
			super(getContext(), LoginItem.class);
			this.facebookToken = facebookToken;
		}

		public LoginUpdateListener() {
			super(getContext(), LoginItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			if (show) {
				showPopupHardProgressDialog(R.string.signing_in_);
			} else {
				if (isPaused)
					return;

				dismissProgressDialog();
			}
		}

		@Override
		public void updateData(LoginItem returnedObj) {
			LoginItem.Data loginData = returnedObj.getData();
			String username = loginData.getUsername();
			if (!TextUtils.isEmpty(username)) {
				preferencesEditor.putString(AppConstants.USERNAME, username);
			}
			preferencesEditor.putInt(AppConstants.USER_PREMIUM_STATUS, loginData.getPremiumStatus());
			preferencesEditor.putString(AppConstants.LIVE_SESSION_ID, loginData.getSessionId());
			preferencesEditor.putLong(AppConstants.LIVE_SESSION_ID_SAVE_TIME, System.currentTimeMillis());
			preferencesEditor.commit();

			if (!TextUtils.isEmpty(facebookToken)) {
				FlurryAgent.logEvent(FlurryData.FB_LOGIN);
				// save facebook access token to appData for future re-login
				getAppData().setFacebookToken(facebookToken);
			}

			processLogin(loginData);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				// get server code
				int serverCode = RestHelper.decodeServerCode(resultCode);
				switch (serverCode) {
					case ServerErrorCodes.INVALID_USERNAME_PASSWORD:
//						if (passwordEdt != null) {
//							passwordEdt.setError(getResources().getString(R.string.invalid_username_or_password));
//							passwordEdt.requestFocus();
//						} else {
							showSinglePopupDialog(R.string.login, R.string.invalid_username_or_password);
//						}
						getAppData().setPassword(Symbol.EMPTY);
						break;
					case ServerErrorCodes.FACEBOOK_USER_NO_ACCOUNT:
						popupItem.setPositiveBtnId(R.string.sign_up);
						showPopupDialog(R.string.no_chess_account_signup_please, CHESS_NO_ACCOUNT_TAG);
						break;
					default:

						break;
				}
			}
		}
	}

	protected Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	protected void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state != null && state.isOpened()) {
			loginWithFacebook(session.getAccessToken());
		}
	}

	protected void loginWithFacebook(String accessToken) {
		Log.d("lcc", "loginWithFacebook");
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_LOGIN);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_FACEBOOK_ACCESS_TOKEN, accessToken);
		loadItem.addRequestParams(RestHelper.P_DEVICE_ID, getDeviceId());
		loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.V_USERNAME);
		loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.V_TACTICS_RATING);

		new RequestJsonTask<LoginItem>(new LoginUpdateListener(accessToken)).executeTask(loadItem);
	}

	protected void processLogin(RegisterItem.Data returnedObj) {
		preferencesEditor.putString(AppConstants.USER_TOKEN, returnedObj.getLoginToken());
		preferencesEditor.putLong(AppConstants.USER_TOKEN_SAVE_TIME, System.currentTimeMillis());
		preferencesEditor.commit();

		getAppData().setLiveChessMode(false);
		DataHolder.reset();
		TacticsDataHolder.reset();

		afterLogin();
	}

	protected void afterLogin() {
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(NETWORK_CHECK_TAG)) {
			startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), NETWORK_REQUEST);
		} else if (tag.equals(CHESS_NO_ACCOUNT_TAG)) { // TODO add logic to lead to register screen
//			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.getInstance().REGISTER_HTML)));
		}
		super.onPositiveBtnClick(fragment);
	}

	protected boolean inLandscape() {
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	protected boolean inPortrait() {
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}

}
