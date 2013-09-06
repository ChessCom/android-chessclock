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
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.GcmItem;
import com.chess.backend.entity.api.LoginItem;
import com.chess.backend.entity.api.MovesStatusItem;
import com.chess.backend.entity.api.RegisterItem;
import com.chess.backend.gcm.GcmHelper;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.model.DataHolder;
import com.chess.model.TacticsDataHolder;
import com.chess.ui.views.drawables.BackgroundChessDrawable;
import com.facebook.widget.LoginButton;
import com.flurry.android.FlurryAgent;
import com.google.android.gcm.GCMRegistrar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.chess.backend.statics.AppConstants.LIVE_SESSION_ID;

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

//	private LoginUpdateListener loginUpdateListener;
	private LoginUpdateListenerNew loginUpdateListener;
	private int loginReturnCode;

	protected BackgroundChessDrawable backgroundChessDrawable;
	private String currentLocale;

	protected Handler handler;
	private EditText loginUsernameEdt;
	private EditText passwordEdt;
	protected boolean isRestarted;
	private AppData appData;
	protected SharedPreferences preferences;
	protected SharedPreferences.Editor preferencesEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		backgroundChessDrawable = new BackgroundChessDrawable(this);
		loginUpdateListener = new LoginUpdateListenerNew();

		appData = new AppData(this);
		preferences = appData.getPreferences();
		preferencesEditor = appData.getEditor();

		currentLocale = preferences.getString(AppConstants.CURRENT_LOCALE, StaticData.LOCALE_EN);

		handler = new Handler();
		setLocale();
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

//	@Override
//	protected void onPostCreate(Bundle savedInstanceState) {
//		super.onPostCreate(savedInstanceState);
////		if (mainView != null) {
////			facebookInit((LoginButton) mainView.findViewById(R.id.fb_connect));
////		}
//	}

	protected void facebookInit(LoginButton fbLoginBtn) {
//		if (fbLoginBtn != null) {
//			facebook = new Facebook(AppConstants.FACEBOOK_APP_ID);
//			SessionStore.restore(facebook, this);
//
//			SessionEvents.dropAuthListeners();
//			SessionEvents.addAuthListener(new SampleAuthListener());
//			SessionEvents.dropLogoutListeners();
//			SessionEvents.addLogoutListener(new SampleLogoutListener());
//			fbLoginBtn.init(this, facebook);
//
//
////			loginUpdateListener = new LoginUpdateListener();
//			loginUpdateListener = new LoginUpdateListenerNew();
//		}
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
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);

		isRestarted = false;
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
		if (registrationId.equals("")) {
			// Automatically registers application on startup.
			GCMRegistrar.register(this, GcmHelper.SENDER_ID);
			Log.d("TEST", " no regId - > GCMRegistrar.register");
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

				new RequestJsonTask<GcmItem>(new PostUpdateListener(REQUEST_REGISTER)).execute(loadItem);
			}
		}
	}

	protected void unRegisterGcmService(){
		// save token to unregister from server
		preferencesEditor.putString(AppConstants.PREF_TEMP_TOKEN_GCM, appData.getUserToken());
		preferencesEditor.commit();
		GCMRegistrar.unregister(this);
	}

	protected class PostUpdateListener extends AbstractUpdateListener<GcmItem> {
		private int requestCode;

		public PostUpdateListener(int requestCode) {
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
                        editor.putString(AppConstants.PREF_TEMP_TOKEN_GCM, StaticData.SYMBOL_EMPTY);
                        editor.commit();
                        break;
                }
            } else {
                if (requestCode == GcmHelper.REQUEST_REGISTER && getContext() != null) {
                    Toast.makeText(getContext(), R.string.gcm_not_registered, Toast.LENGTH_SHORT).show();
                }
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
			string += "a";
		}
		return string;
	}

	private class LoginUpdateListenerNew extends AbstractUpdateListener<LoginItem> {
		public LoginUpdateListenerNew() {
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
//						String serverMessage = ServerErrorCodes.getUserFriendlyMessage(); // TODO restore
//						showToast(serverMessage);

						break;
				}
			}
		}

		@Override
		public void errorHandle(String resultMessage) {
			if (resultMessage.contains(RestHelper.R_FB_USER_HAS_NO_ACCOUNT)) {
				popupItem.setPositiveBtnId(R.string.sign_up);
				showPopupDialog(R.string.no_chess_account_signup_please, CHESS_NO_ACCOUNT_TAG);
			}
		}
	}


//	public class SampleAuthListener implements SessionEvents.AuthListener {
//		@Override
//		public void onAuthSucceed() {
//			LoadItem loadItem = new LoadItem();
////			loadItem.setLoadPath(RestHelper.LOGIN);
//			loadItem.setLoadPath(RestHelper.CMD_LOGIN);
//			loadItem.setRequestMethod(RestHelper.POST);
//			loadItem.addRequestParams(RestHelper.P_FACEBOOK_ACCESS_TOKEN, facebook.getAccessToken());
////			loadItem.addRequestParams(RestHelper.P_RETURN, RestHelper.V_USERNAME);
//			loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.V_USERNAME);
//
//			new RequestJsonTask<LoginItem>(loginUpdateListener).executeTask(loadItem);
//			loginReturnCode = SIGNIN_FACEBOOK_CALLBACK_CODE;
//		}
//
//		@Override
//		public void onAuthFail(String error) {
//			showToast(getString(R.string.login_failed)+ StaticData.SYMBOL_SPACE + error);
//		}
//	}
//	public class SampleLogoutListener implements SessionEvents.LogoutListener {
//		@Override
//		public void onLogoutBegin() {
//			showToast(R.string.login_out);
//		}
//
//		@Override
//		public void onLogoutFinish() {
//			showToast(R.string.you_logged_out);
//		}
//	}

	protected void processLogin(RegisterItem.Data returnedObj) {
		if (passwordEdt == null) { // if accidently return in wrong callback, when widgets are not initialized
			return;
		}

		preferencesEditor.putString(AppConstants.PASSWORD, passwordEdt.getText().toString().trim());
		preferencesEditor.putString(AppConstants.USER_TOKEN, returnedObj.getLoginToken());
		preferencesEditor.commit();

		getAppData().setLiveChessMode(false);
		DataHolder.reset();
		TacticsDataHolder.reset();

		afterLogin();
	}

	protected void afterLogin(){ }

//	protected void backToHomeActivity() {
//		Intent intent = new Intent(this, HomeScreenActivity.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		startActivity(intent);
//		finish();
//	}

//	protected void backToLoginActivity() {
//		Intent intent = new Intent(this, LoginScreenActivity.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		startActivity(intent);
//		finish();
//	}

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

//	protected void checkMove(){
//		LoadItem loadItem = new LoadItem();
//		loadItem.setLoadPath(RestHelper.CMD_MOVES);
//		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, appData.getUserToken());
//
//		new RequestJsonTask<MovesStatusItem>(new CheckMoveUpdateListener()).executeTask(loadItem);
//	}

	private class CheckMoveUpdateListener extends AbstractUpdateListener<MovesStatusItem> {
		public CheckMoveUpdateListener() {
			super(getContext(), MovesStatusItem.class);
		}

		@Override
		public void updateData(MovesStatusItem returnedObj) {
			int haveMoves = 0;
/*			List<GameListCurrentItem> itemList = ChessComApiParser.getCurrentOnlineGames(returnedObj);

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
				SharedPreferences preferences = appData.getPreferences(getMeContext());
				boolean playSounds = preferences.getBoolean(appData.getUsername(getMeContext()) + AppConstants.PREF_SOUNDS, false);
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
			}*/
		}
	}
}
