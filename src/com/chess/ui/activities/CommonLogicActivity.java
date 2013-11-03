package com.chess.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.GcmItem;
import com.chess.backend.entity.api.LoginItem;
import com.chess.backend.entity.api.RegisterItem;
import com.chess.backend.gcm.GcmHelper;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.model.DataHolder;
import com.chess.model.TacticsDataHolder;
import com.chess.statics.*;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.flurry.android.FlurryAgent;
import com.google.android.gcm.GCMRegistrar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.chess.statics.AppConstants.LIVE_SESSION_ID;

//import com.facebook.android.Facebook;

/**
 * CommonLogicActivity class
 *
 * @author alien_roger
 * @created at: 23.09.12 8:10
 */
public abstract class CommonLogicActivity extends BaseFragmentPopupsActivity {

	private static final int SIGNIN_FACEBOOK_CALLBACK_CODE = 128;
	private static final int SIGNIN_CALLBACK_CODE = 16;
	protected static final long FACEBOOK_DELAY = 200;
	private static final int MIN_USERNAME_LENGTH = 3;
	private static final int MAX_USERNAME_LENGTH = 20;

	protected static final int REQUEST_REGISTER = 11;
	private static final int REQUEST_UNREGISTER = 22;

	private LoginUpdateListener loginUpdateListener;
	private int loginReturnCode;

	private String currentLocale;

	protected Handler handler;
	private EditText loginUsernameEdt;
	private EditText passwordEdt;
	protected boolean isRestarted;
	private AppData appData;
	protected SharedPreferences preferences;
	protected SharedPreferences.Editor preferencesEditor;
	private UiLifecycleHelper facebookUiHelper;
	private boolean facebookActive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loginUpdateListener = new LoginUpdateListener();

		appData = new AppData(this);
		preferences = appData.getPreferences();
		preferencesEditor = appData.getEditor();

		currentLocale = preferences.getString(AppConstants.CURRENT_LOCALE, StaticData.LOCALE_EN);

		handler = new Handler();
		setLocale();

		facebookUiHelper = new UiLifecycleHelper(this, callback);
		facebookUiHelper.onCreate(savedInstanceState);
	}

	protected AppData getAppData() {
		return appData == null? new AppData(this) : appData;
	}

	protected String getCurrentUsername() {
		return getAppData().getUsername();
	}

	protected String getCurrentUserToken() {
		return getAppData().getUserToken();
	}

	protected void facebookInit(LoginButton fbLoginBtn) {
		if (fbLoginBtn != null) {
//			facebookUiHelper = new UiLifecycleHelper(this, callback);
//			facebookUiHelper.onCreate(savedInstanceState);
			facebookActive = true;

			// logout from facebook
			Session facebookSession = Session.getActiveSession();
			if (facebookSession != null) {
				facebookSession.closeAndClearTokenInformation();
				Session.setActiveSession(null);
			}

//			fbLoginBtn.setFragment(this);
			fbLoginBtn.setReadPermissions(Arrays.asList("user_status", "email"));
			fbLoginBtn.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
				@Override
				public void onUserInfoFetched(GraphUser user) {
				}
			});

//			loginUpdateListener = new LoginUpdateListenerNew();
		}
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

		if (preferences.getLong(AppConstants.FIRST_TIME_START, 0) == 0) {
			preferencesEditor.putLong(AppConstants.FIRST_TIME_START, System.currentTimeMillis());
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, 0);
			preferencesEditor.commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (facebookActive) {
			facebookUiHelper.onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (facebookActive) {
			facebookUiHelper.onPause();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);

		isRestarted = false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (facebookActive) {
			facebookUiHelper.onSaveInstanceState(outState);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (facebookActive) {
			facebookUiHelper.onActivityResult(requestCode, resultCode, data);
		}
	}

	protected void setLocale() {
		String prevLang = getResources().getConfiguration().locale.getLanguage();
		String[] languageCodes = getResources().getStringArray(R.array.languages_codes);

		String setLocale = languageCodes[appData.getLanguageCode()];
		if(!prevLang.equals(setLocale)) {
			Locale locale = new Locale(setLocale);
			Locale.setDefault(locale);
			Configuration config = new Configuration();
			config.locale = locale;
			getResources().updateConfiguration(config, getResources().getDisplayMetrics());

			preferencesEditor.putString(AppConstants.CURRENT_LOCALE, setLocale);
			preferencesEditor.commit();

			currentLocale = setLocale;
		}
	}

	protected void restartActivity(){
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

	protected List<String> getItemsFromEntries(int entries){
		String[] array = getResources().getStringArray(entries);
		return getItemsFromArray(array);
	}

	protected List<String> getItemsFromArray(String[] array){
		List<String> items = new ArrayList<String>();
		items.addAll(Arrays.asList(array));
		return items;
	}

	protected void registerGcmService(){
		if (!appData.isNotificationsEnabled()) { // no need to register if user turned off notifications
			return;
		}
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
			// Device is already registered on GCM, check server.
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

				new RequestJsonTask<GcmItem>(new GcmRegisterUpdateListener(REQUEST_REGISTER)).execute(loadItem);
			}
		}
	}

	protected void unRegisterGcmService(){
		// save token to unregister from server
		preferencesEditor.putString(AppConstants.PREF_TEMP_TOKEN_GCM, appData.getUserToken());
		preferencesEditor.commit();
		GCMRegistrar.unregister(this);
	}

	protected class GcmRegisterUpdateListener extends AbstractUpdateListener<GcmItem> {
		private int requestCode;

		public GcmRegisterUpdateListener(int requestCode) {
			super(CommonLogicActivity.this, GcmItem.class);
			this.requestCode = requestCode;
		}

		@Override
		public void updateData(GcmItem returnedObj) {   // TODO invent exponential leap

            if (returnedObj.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
                switch (requestCode) {
                    case GcmHelper.REQUEST_REGISTER:
                        GCMRegistrar.setRegisteredOnServer(getContext(), true);
                        appData.registerOnChessGCM(appData.getUserToken());
                        break;
                    case GcmHelper.REQUEST_UNREGISTER:
                        GCMRegistrar.setRegisteredOnServer(getContext(), false);
                        appData.unRegisterOnChessGCM();
                        // remove saved token
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(AppConstants.PREF_TEMP_TOKEN_GCM, Symbol.EMPTY);
                        editor.commit();
                        break;
                }
            } else {
				if (returnedObj.getCode() == ServerErrorCodes.YOUR_GCM_ID_ALREADY_REGISTERED) {
					GCMRegistrar.setRegisteredOnServer(getContext(), true);
					appData.registerOnChessGCM(appData.getUserToken());
				}

//              if (requestCode == GcmHelper.REQUEST_REGISTER && getContext() != null) {
//                  Toast.makeText(getContext(), R.string.gcm_not_registered, Toast.LENGTH_SHORT).show();
//              }
            }
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (resultCode == ServerErrorCodes.YOUR_GCM_ID_ALREADY_REGISTERED) {
				Log.d("GCMIntentService", "YOUR_GCM_ID_ALREADY_REGISTERED registering to GCM anyway");
				GCMRegistrar.setRegisteredOnServer(getContext(), true);
				appData.registerOnChessGCM(appData.getUserToken());
			} else {
				super.errorHandle(resultCode);
			}
		}
	}

	protected void setLoginFields(EditText passedUsernameEdt, EditText passedPasswordEdt) {
		this.loginUsernameEdt = passedUsernameEdt;
		this.passwordEdt = passedPasswordEdt;
	}

	protected void signInUser(){
		String username = getTextFromField(loginUsernameEdt);
		if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
			loginUsernameEdt.setError(getString(R.string.validateUsername));
			loginUsernameEdt.requestFocus();
			return;
		}

		String pass = getTextFromField(passwordEdt);
		if (pass.length() == 0) {
			passwordEdt.setError(getString(R.string.password_cant_be_empty));
			passwordEdt.requestFocus();
			return;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_LOGIN);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_DEVICE_ID, getDeviceId());
		loadItem.addRequestParams(RestHelper.P_USER_NAME_OR_MAIL, username);
		loadItem.addRequestParams(RestHelper.P_PASSWORD, getTextFromField(passwordEdt));
		loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.P_USERNAME);
		loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.P_TACTICS_RATING);

		new RequestJsonTask<LoginItem>(loginUpdateListener).executeTask(loadItem);

		loginReturnCode = SIGNIN_CALLBACK_CODE;
	}

	protected String getDeviceId() {
		String string = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
		while ((string != null ? string.length() : 0) < 32) { // 32 length is requirement for deviceId parameter
			string += string;
		}
		return string.substring(0, 32);
	}

	private class LoginUpdateListener extends AbstractUpdateListener<LoginItem> {
		public LoginUpdateListener() {
			super(getContext(), LoginItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			if (show){
				showPopupHardProgressDialog(R.string.signing_in_);
			} else {
				if(isPaused)
					return;

				dismissProgressDialog();
			}
		}

		@Override
		public void updateData(LoginItem returnedObj) {
			if (loginReturnCode == SIGNIN_FACEBOOK_CALLBACK_CODE) {
				FlurryAgent.logEvent(FlurryData.FB_LOGIN);
			}
			if (!TextUtils.isEmpty(returnedObj.getData().getUsername())) {
				preferencesEditor.putString(AppConstants.USERNAME, returnedObj.getData().getUsername().trim().toLowerCase());
			}
			preferencesEditor.putInt(AppConstants.USER_PREMIUM_STATUS, returnedObj.getData().getPremiumStatus());
			preferencesEditor.putString(LIVE_SESSION_ID, returnedObj.getData().getSessionId());
			processLogin(returnedObj.getData());
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				// get server code
				int serverCode = RestHelper.decodeServerCode(resultCode);
				switch (serverCode){
					case ServerErrorCodes.INVALID_USERNAME_PASSWORD:
						passwordEdt.setError(getResources().getString(R.string.invalid_username_or_password));
						passwordEdt.requestFocus();
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
			Log.d("TEST"," session callback session = " + session + " state = " + state);
			onSessionStateChange(session, state, exception);
		}
	};

	protected void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state != null && state.isOpened()) {
			loginWithFacebook(session);
		}
	}

	private void loginWithFacebook(Session session) {
		// save facebook access token to appData for future re-login
		getAppData().setFacebookToken(session.getAccessToken());

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_LOGIN);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_FACEBOOK_ACCESS_TOKEN, session.getAccessToken());
		loadItem.addRequestParams(RestHelper.P_DEVICE_ID, getDeviceId());
		loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.V_USERNAME);
		loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.V_TACTICS_RATING);

		new RequestJsonTask<LoginItem>(loginUpdateListener).executeTask(loadItem);
		loginReturnCode = SIGNIN_FACEBOOK_CALLBACK_CODE;
	}

	protected void processLogin(RegisterItem.Data returnedObj) {
		if (TextUtils.isEmpty(getTextFromField(passwordEdt))) {
			preferencesEditor.putString(AppConstants.PASSWORD, getTextFromField(passwordEdt));
		}

		preferencesEditor.putString(AppConstants.USER_TOKEN, returnedObj.getLoginToken());
		preferencesEditor.commit();

		getAppData().setLiveChessMode(false);
		DataHolder.reset();
		TacticsDataHolder.reset();

		afterLogin();
	}

	protected void afterLogin(){ }

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(NETWORK_CHECK_TAG)){
			startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), NETWORK_REQUEST);
		} else if (tag.equals(CHESS_NO_ACCOUNT_TAG)){
//			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.getInstance().REGISTER_HTML)));
		}
		super.onPositiveBtnClick(fragment);
	}

}
