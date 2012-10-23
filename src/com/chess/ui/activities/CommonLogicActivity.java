package com.chess.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import com.chess.R;
import com.chess.backend.GcmHelper;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.GSMServerResponseItem;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.*;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.backend.tasks.PostDataTask;
import com.chess.backend.tasks.PostJsonDataTask;
import com.chess.model.GameListCurrentItem;
import com.chess.ui.views.BackgroundChessDrawable;
import com.chess.utilities.AppUtils;
import com.chess.utilities.ChessComApiParser;
import com.facebook.android.Facebook;
import com.facebook.android.LoginButton;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.flurry.android.FlurryAgent;
import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * CommonLogicActivity class
 *
 * @author alien_roger
 * @created at: 23.09.12 8:10
 */
public abstract class CommonLogicActivity extends BaseFragmentActivity {

	private static final int SIGNIN_FACEBOOK_CALLBACK_CODE = 128;
	private static final int SIGNIN_CALLBACK_CODE = 16;
	protected static final long FACEBOOK_DELAY = 200;
	private static final int MIN_USERNAME_LENGTH = 3;
	private static final int MAX_USERNAME_LENGTH = 20;

	protected static final int REQUEST_REGISTER = 11;
	private static final int REQUEST_UNREGISTER = 22;

	private LoginUpdateListener loginUpdateListener;
	private int loginReturnCode;

	protected BackgroundChessDrawable backgroundChessDrawable;
	private String currentLocale;
	protected Facebook facebook;
	protected Handler handler;
	private EditText loginUsernameEdt;
	private EditText passwordEdt;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		backgroundChessDrawable = new BackgroundChessDrawable(this);

		currentLocale = preferences.getString(AppConstants.CURRENT_LOCALE, StaticData.LOCALE_EN);

		setLocale();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		facebookInit((LoginButton) findViewById(R.id.fb_connect));
	}

	protected void facebookInit(LoginButton fbLoginBtn) {
		if (fbLoginBtn != null) {
			facebook = new Facebook(AppConstants.FACEBOOK_APP_ID);
			SessionStore.restore(facebook, this);

			SessionEvents.dropAuthListeners();
			SessionEvents.addAuthListener(new SampleAuthListener());
			SessionEvents.dropLogoutListeners();
			SessionEvents.addLogoutListener(new SampleLogoutListener());
			fbLoginBtn.init(this, facebook);

			handler = new Handler();

			loginUpdateListener = new LoginUpdateListener();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (preferences.getLong(AppConstants.FIRST_TIME_START, 0) == 0) {
			preferencesEditor.putLong(AppConstants.FIRST_TIME_START, System.currentTimeMillis());
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, 0);
			preferencesEditor.commit();
		}

		if(!currentLocale.equals(getResources().getConfiguration().locale.getLanguage())){
			restartActivity();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, FlurryData.API_KEY);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	protected void setLocale(){
		String prevLang = getResources().getConfiguration().locale.getLanguage();
		String[] languageCodes = getResources().getStringArray(R.array.languages_codes);

		String setLocale = languageCodes[AppData.getLanguageCode(this)];
		if(!prevLang.equals(setLocale)) {
			Locale locale = new Locale(setLocale);
			Locale.setDefault(locale);
			Configuration config = new Configuration();
			config.locale = locale;
			getResources().updateConfiguration(config, getResources().getDisplayMetrics());

			preferencesEditor.putString(AppConstants.CURRENT_LOCALE, setLocale);
			preferencesEditor.commit();

			currentLocale = setLocale;

			restartActivity();
		}
	}

	protected void restartActivity(){
		Intent intent = getIntent();
		finish();
		startActivity(intent);
		Log.d("TEST", "___restartActivity___");
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
//		if (!AppData.isNotificationsEnabled(this) || DataHolder.getInstance().isGuest()) // no need to register if user turned off notifications
		if (!AppData.isNotificationsEnabled(this) || AppData.isGuest(this)) // no need to register if user turned off notifications
			return;


		// Make sure the device has the proper dependencies.
//		try {
//			GCMRegistrar.checkDevice(this); // Check device to support GCM, if not supported then turn on timed notifications
//		} catch (UnsupportedOperationException ex) {
//			Log.d("TEST_GCM", "Device doesn't support GCM, so use timed notifications " + ex.toString());
//			AppUtils.startNotificationsUpdate(this);
//			return;
//		}

		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
//		GCMRegistrar.checkManifest(this);   // don't need to check manifest because it's developers task to write it correct :)

		/* When an application is updated, it should invalidate its existing registration ID.
		The best way to achieve this validation is by storing the current
		 application version when a registration ID is stored.
		 Then when the application is started, compare the stored value
		 with the current application version.
		 If they do not match, invalidate the stored data and start the registration process again.
		 */

		final String registrationId = GCMRegistrar.getRegistrationId(this);
		if (registrationId.equals("")) {
			// Automatically registers application on startup.
			GCMRegistrar.register(this, GcmHelper.SENDER_ID);
			Log.d("TEST", " no regId - > GCMRegistrar.register");
		} else {
			// Device is already registered on GCM, check server.
			if (GCMRegistrar.isRegisteredOnServer(this) && AppData.isRegisterOnChessGCM(this)) {
				// Skips registration.
				Log.d("TEST", "already registered");
			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.

//				String deviceId = new AppUtils.DeviceInfo().getDeviceInfo(this).android_id;

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.GCM_REGISTER);
				loadItem.addRequestParams(RestHelper.GCM_P_ID, AppData.getUserToken(this));
				loadItem.addRequestParams(RestHelper.GCM_P_REGISTER_ID, registrationId);
//				loadItem.addRequestParams(RestHelper.GCM_P_DEVICE_ID, deviceId);

				new PostJsonDataTask(new PostUpdateListener(REQUEST_REGISTER)).execute(loadItem);
				Log.d("GCMIntentService", "Registering to server, registrationId = " + registrationId
						+ " \ntoken = " + AppData.getUserToken(this)
						/*+ " \ndeviceId = " + deviceId */);
			}
		}
	}

	protected void unregisterGcmService(){
//		try{
//			GCMRegistrar.checkDevice(this);
//		} catch (UnsupportedOperationException ex){
//			Log.d("TEST_GCM", "Device doesn't support GCM, so use timed notifications ");
//			AppUtils.stopNotificationsUpdate(this);
//		}

		// save token to unregister from server
		preferencesEditor.putString(AppConstants.PREF_TEMP_TOKEN_GCM, AppData.getUserToken(this));
		preferencesEditor.commit();
		GCMRegistrar.unregister(this);
	}

	protected class PostUpdateListener extends AbstractUpdateListener<String> {
		private int requestCode;

		public PostUpdateListener(int requestCode) {
			super(CommonLogicActivity.this);
			this.requestCode = requestCode;
		}

		@Override
		public void updateData(String returnedObj) {
			super.updateData(returnedObj);
			Log.d("TEST", "PostUpdateListener -> updateDate = " + returnedObj);

			GSMServerResponseItem responseItem = parseJson(returnedObj);

			if(responseItem.getCode() < 400){
				switch (requestCode){
					case REQUEST_REGISTER:
						GCMRegistrar.setRegisteredOnServer(getContext(), true);
						AppData.registerOnChessGCM(getContext(), AppData.getUserToken(getContext()));
						break;
					case REQUEST_UNREGISTER:
						GCMRegistrar.setRegisteredOnServer(getContext(), false);
						AppData.unRegisterOnChessGCM(getContext());
						break;
				}
			}
		}

		GSMServerResponseItem parseJson(String jRespString) {
			Gson gson = new Gson();
			return gson.fromJson(jRespString, GSMServerResponseItem.class);
		}
	}

	protected void setLoginFields(EditText passedUsernameEdt, EditText passedPasswordEdt) {
		this.loginUsernameEdt = passedUsernameEdt;
		this.passwordEdt = passedPasswordEdt;
	}

	protected void signInUser(){
		String userName = getTextFromField(loginUsernameEdt);
		if (userName.length() < MIN_USERNAME_LENGTH || userName.length() > MAX_USERNAME_LENGTH) {
			loginUsernameEdt.setError(getString(R.string.validateUsername));
			loginUsernameEdt.requestFocus();
			return;
		}

		String pass = getTextFromField(passwordEdt);
		if (pass.length() == 0) {
			passwordEdt.setError(getString(R.string.invalid_password));
			passwordEdt.requestFocus();
			return;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.LOGIN);
		loadItem.addRequestParams(RestHelper.P_USER_NAME, userName);
		loadItem.addRequestParams(RestHelper.P_PASSWORD, getTextFromField(passwordEdt));

		new PostDataTask(loginUpdateListener).executeTask(loadItem);

		loginReturnCode = SIGNIN_CALLBACK_CODE;
	}

	private class LoginUpdateListener extends AbstractUpdateListener<String> {
		public LoginUpdateListener() {
			super(getContext());
		}

		@Override
		public void showProgress(boolean show) {
			if (show){
				showPopupHardProgressDialog(R.string.signingin);
			} else {
				Log.d("TEST", "LoginScreen LoginUpdateListener ->  dismissProgressDialog();, isPaused = " + isPaused);
				if(isPaused)
					return;

				dismissProgressDialog();
			}
		}

		@Override
		public void updateData(String returnedObj) {
//			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
			if (returnedObj.length() > 0) {
				final String[] responseArray = returnedObj.split(":");
				if (responseArray.length >= 4) {
					if (loginReturnCode == SIGNIN_CALLBACK_CODE) {
						preferencesEditor.putString(AppConstants.USERNAME, loginUsernameEdt.getText().toString().trim().toLowerCase());
						processLogin(responseArray);
					} else if (loginReturnCode == SIGNIN_FACEBOOK_CALLBACK_CODE && responseArray.length >= 5) {
						FlurryAgent.logEvent(FlurryData.FB_LOGIN, null);
						preferencesEditor.putString(AppConstants.USERNAME, responseArray[4].trim().toLowerCase());
						processLogin(responseArray);
					}
				}
			}
//			}
		}

		@Override
		public void errorHandle(String resultMessage) {
			if (resultMessage.contains(RestHelper.R_FB_USER_HAS_NO_ACCOUNT)) {
				popupItem.setPositiveBtnId(R.string.sing_up);
				showPopupDialog(R.string.no_chess_account_signup_please, CHESS_NO_ACCOUNT_TAG);
			} else /*if(returnedObj.contains(RestHelper.R_ERROR))*/{
//				String message = returnedObj.substring(RestHelper.R_ERROR.length());
				if(resultMessage.equals(RestHelper.R_INVALID_PASS)){
					passwordEdt.setError(getResources().getString(R.string.invalid_password));
					passwordEdt.requestFocus();
				}else{

					showToast(resultMessage);
//					showSinglePopupDialog(R.string.error, message);
				}
			}
		}
	}

	public class SampleAuthListener implements SessionEvents.AuthListener {
		@Override
		public void onAuthSucceed() {
			Log.d("TEST", " Activity is = " + this);
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.LOGIN);
			loadItem.addRequestParams(RestHelper.P_FACEBOOK_ACCESS_TOKEN, facebook.getAccessToken());
			loadItem.addRequestParams(RestHelper.P_RETURN, RestHelper.V_USERNAME);

			new GetStringObjTask(loginUpdateListener).executeTask(loadItem);

			loginReturnCode = SIGNIN_FACEBOOK_CALLBACK_CODE;
		}

		@Override
		public void onAuthFail(String error) {
			showToast(getString(R.string.login_failed)+ StaticData.SYMBOL_SPACE + error);
		}
	}
	public class SampleLogoutListener implements SessionEvents.LogoutListener {
		@Override
		public void onLogoutBegin() {
			showToast(R.string.loggin_out);
		}

		@Override
		public void onLogoutFinish() {
			showToast(R.string.you_logged_out);
		}
	}

	private void processLogin(String[] response) {
		// from actionbar
		preferencesEditor.putString(AppConstants.PASSWORD, passwordEdt.getText().toString().trim());
		preferencesEditor.putString(AppConstants.USER_PREMIUM_STATUS, response[0].split("[+]")[1]);
		preferencesEditor.putString(AppConstants.API_VERSION, response[1]);
		try {
			preferencesEditor.putString(AppConstants.USER_TOKEN, URLEncoder.encode(response[2], HTTP.UTF_8));
		} catch (UnsupportedEncodingException ignored) {
			preferencesEditor.putString(AppConstants.USER_TOKEN, response[2]);
		}
		preferencesEditor.putString(AppConstants.USER_SESSION_ID, response[3]);
		preferencesEditor.commit();

		afterLogin();
	}

	protected void afterLogin(){ }

	/**
	 * Prevent earlier launch of task, as it finish right after onPause callback
	 */
	protected class DelayedCallback implements Runnable {

		private Intent data;
		private int resultCode;
		private int requestCode;

		public DelayedCallback(Intent data, int requestCode, int resultCode) {
			this.data = data;
			this.requestCode = requestCode;
			this.resultCode = resultCode;
		}

		@Override
		public void run() {
			handler.removeCallbacks(this);
			facebook.authorizeCallback(requestCode, resultCode, data);
		}
	}


	protected void checkMove(){
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.ECHESS_CURRENT_GAMES);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));
		loadItem.addRequestParams(RestHelper.P_ALL, RestHelper.V_ONLY_USER_TURN);
		new GetStringObjTask(new CheckMoveUpdateListener()).executeTask(loadItem);
	}

	private class CheckMoveUpdateListener extends AbstractUpdateListener<String> {
		public CheckMoveUpdateListener() {
			super(getContext());
		}

		@Override
		public void updateData(String returnedObj) {
			int haveMoves = 0;
			List<GameListCurrentItem> itemList = ChessComApiParser.getCurrentOnlineGames(returnedObj);

			if(itemList.size() == 1) { // only one icon
				GameListCurrentItem gameListItem = itemList.get(0);

				AppUtils.showNewMoveStatusNotification(getMeContext(),
						getMeContext().getString(R.string.your_move),
						getMeContext().getString(R.string.your_turn_in_game_with,
								gameListItem.getOpponentUsername(),
								gameListItem.getLastMoveFromSquare() + gameListItem.getLastMoveToSquare()),
						StaticData.MOVE_REQUEST_CODE,
						gameListItem);
				haveMoves++;
			} else if(itemList.size() > 0) {
				for (GameListCurrentItem currentItem : itemList) {
					AppUtils.cancelNotification(getMeContext(), (int) currentItem.getGameId());
				}

				AppUtils.showMoveStatusNotification(getMeContext(),
						getMeContext().getString(R.string.your_turn),
						getMeContext().getString(R.string.your_move) + StaticData.SYMBOL_SPACE
								+ StaticData.SYMBOL_LEFT_PAR + itemList.size() + StaticData.SYMBOL_RIGHT_PAR,
						0, OnlineScreenActivity.class);
				getMeContext().sendBroadcast(new Intent(IntentConstants.USER_MOVE_UPDATE));
			}

			if(haveMoves == 1){ // play for one
				SharedPreferences preferences = AppData.getPreferences(getMeContext());
				boolean playSounds = preferences.getBoolean(AppData.getUserName(getMeContext()) + AppConstants.PREF_SOUNDS, false);
				if(playSounds){
					final MediaPlayer player = MediaPlayer.create(getMeContext(), R.raw.move_opponent);
					if(player != null){
						player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mediaPlayer) {
								player.release();
							}
						});
						player.start();
					}
				}


				getMeContext().sendBroadcast(new Intent(IntentConstants.USER_MOVE_UPDATE));
			}
		}
	}
}
