package com.chess.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.CheckUpdateTask;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.backend.tasks.PostDataTask;
import com.chess.utilities.AppUtils;
import com.facebook.android.Facebook;
import com.facebook.android.LoginButton;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.flurry.android.FlurryAgent;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * LoginScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 6:23
 */
public class LoginScreenActivity extends BaseFragmentActivity implements View.OnClickListener, TextView.OnEditorActionListener, View.OnTouchListener {

	private static int SIGNIN_CALLBACK_CODE = 16;
	private static int SIGNIN_FACEBOOK_CALLBACK_CODE = 128;
	private static final int MIN_USERNAME_LENGTH = 3;
	private static final int MAX_USERNAME_LENGTH = 20;


	private EditText usernameEdt;
	private EditText passwordEdt;

	private Facebook facebook;
	private LoginUpdateListener loginUpdateListener;
	private int loginReturnCode;
    private boolean forceFlag;
	private Handler handler;
	private static final long FACEBOOK_DELAY = 200;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);


		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);

		usernameEdt = (EditText) findViewById(R.id.usernameEdt);
		passwordEdt = (EditText) findViewById(R.id.passwordEdt);
		passwordEdt.setOnEditorActionListener(this);
		passwordEdt.setOnTouchListener(this);
				
		findViewById(R.id.signin).setOnClickListener(this);
		findViewById(R.id.signup).setOnClickListener(this);
		findViewById(R.id.guestplay).setOnClickListener(this);

		LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.fb_connect);

		facebook = new Facebook(AppConstants.FACEBOOK_APP_ID);
		SessionStore.restore(facebook, this);

		SessionEvents.dropAuthListeners();
		SessionEvents.addAuthListener(new SampleAuthListener());
		SessionEvents.dropLogoutListeners();
		SessionEvents.addLogoutListener(new SampleLogoutListener());
		facebookLoginButton.init(this, facebook);

		handler = new Handler();

		loginUpdateListener = new LoginUpdateListener();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.signin) {
			if(!AppUtils.isNetworkAvailable(this)){ // check only if live
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
			}else{
				signInUser();
			}
		} else if (view.getId() == R.id.signup) {
			startActivity(new Intent(this, SignUpScreenActivity.class));
		} else if (view.getId() == R.id.guestplay) {
			Intent intent = new Intent(this, HomeScreenActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
		if(actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.FLAG_EDITOR_ACTION
                || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER ){
			if(!AppUtils.isNetworkAvailable(this)){ // check only if live
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
			}else{
				signInUser();
			}
		}
		return false;
	}

	private void signInUser(){
		String userName = getTextFromField(usernameEdt);
		if (userName.length() < MIN_USERNAME_LENGTH || userName.length() > MAX_USERNAME_LENGTH) {
			usernameEdt.setError(getString(R.string.validateUsername));
			usernameEdt.requestFocus();
			return;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.LOGIN);
		loadItem.addRequestParams(RestHelper.P_USER_NAME, userName);
		loadItem.addRequestParams(RestHelper.P_PASSWORD, getTextFromField(passwordEdt));

		new PostDataTask(loginUpdateListener).executeTask(loadItem);

		loginReturnCode = SIGNIN_CALLBACK_CODE;
	}

	public class SampleAuthListener implements SessionEvents.AuthListener {
		@Override
		public void onAuthSucceed() {

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

	private class LoginUpdateListener extends AbstractUpdateListener<String> {
		public LoginUpdateListener() {
			super(getContext());
		}

		@Override
		public void showProgress(boolean show) {
			if (show){
				showPopupHardProgressDialog(R.string.signingin);
			} else {
				dismissProgressDialog();
			}
		}

		@Override
		public void updateData(String returnedObj) {
//			if(isPaused)
//				return;

			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				if (returnedObj.length() > 0) {
					final String[] responseArray = returnedObj.split(":");
					if (responseArray.length >= 4) {
						if (loginReturnCode == SIGNIN_CALLBACK_CODE) {
							preferencesEditor.putString(AppConstants.USERNAME, usernameEdt.getText().toString().trim().toLowerCase());
							processLogin(responseArray);
						} else if (loginReturnCode == SIGNIN_FACEBOOK_CALLBACK_CODE && responseArray.length >= 5) {
							FlurryAgent.logEvent(FlurryData.FB_LOGIN, null);
							preferencesEditor.putString(AppConstants.USERNAME, responseArray[4].trim().toLowerCase());
							processLogin(responseArray);
						}
					}
				}
			} else if (returnedObj.contains(RestHelper.R_FB_USER_HAS_NO_ACCOUNT)) {
				popupItem.setPositiveBtnId(R.string.sing_up);
				showPopupDialog(R.string.no_chess_account_signup_please, CHESS_NO_ACCOUNT_TAG);
			} else if(returnedObj.contains(RestHelper.R_ERROR)){
				String message = returnedObj.substring(RestHelper.R_ERROR.length());
				if(message.equals("Invalid password.")){
					passwordEdt.setError(getResources().getString(R.string.invalid_password));
					passwordEdt.requestFocus();
				}else{

					showToast(message);
//					showSinglePopupDialog(R.string.error, message);
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		DataHolder.getInstance().setLiveChess(false);

		usernameEdt.setText(AppData.getUserName(this));
		passwordEdt.setText(AppData.getPassword(this));

		long startDay = preferences.getLong(AppConstants.START_DAY, 0);
		if (startDay == 0 || !DateUtils.isToday(startDay)) {
			checkUpdate();
		}
	}

	private void processLogin(String[] response) {
		preferencesEditor.putString(AppConstants.PASSWORD, passwordEdt.getText().toString().trim());
		preferencesEditor.putString(AppConstants.USER_PREMIUM_STATUS, response[0].split("[+]")[1]);
		preferencesEditor.putString(AppConstants.API_VERSION, response[1]);
		try {
			preferencesEditor.putString(AppConstants.USER_TOKEN, URLEncoder.encode(response[2], HTTP.UTF_8));
		} catch (UnsupportedEncodingException ignored) { // TODO handle more proper way
			preferencesEditor.putString(AppConstants.USER_TOKEN, response[2]);
		}
		preferencesEditor.putString(AppConstants.USER_SESSION_ID, response[3]);
		preferencesEditor.commit();

		FlurryAgent.logEvent(FlurryData.LOGGED_IN);
		if (preferences.getBoolean(AppData.getUserName(this) + AppConstants.PREF_NOTIFICATION, true)){
			AppUtils.startNotificationsUpdate(this);
		}

		DataHolder.getInstance().setGuest(false);

		Intent intent = new Intent(this, HomeScreenActivity.class);
		startActivity(intent);
		finish();
	}

	private void checkUpdate() {
		new CheckUpdateTask(new CheckUpdateListener()).executeTask(RestHelper.GET_ANDROID_VERSION);
	}

	private class CheckUpdateListener extends AbstractUpdateListener<Boolean> {
		public CheckUpdateListener() {
			super(getContext());
		}

		@Override
		public void showProgress(boolean show) {
		}

		@Override
		public void updateData(Boolean returnedObj) {
			forceFlag = returnedObj;
			if (isPaused)
				return;

			showPopupDialog(R.string.update_check, R.string.update_available_please_update, CHECK_UPDATE_TAG);
			getLastPopupFragment().setButtons(1);
//			popupDialogFragment.setButtons(1);
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);

		if(fragment.getTag().equals(CHECK_UPDATE_TAG)){
			if (forceFlag) {
				// drop start day
				preferencesEditor.putLong(AppConstants.START_DAY, 0);
				preferencesEditor.commit();

			}
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.GOOGLE_PLAY_URI)));
		}else if (fragment.getTag().equals(CHESS_NO_ACCOUNT_TAG)){
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.REGISTER_HTML)));
		}else if (fragment.getTag().equals(NETWORK_CHECK_TAG)){
			startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), NETWORK_REQUEST);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK ){
			if(requestCode == Facebook.DEFAULT_AUTH_ACTIVITY_CODE){
				handler.postDelayed(new DelayedCallback(data, requestCode, resultCode), FACEBOOK_DELAY);
			}else if(requestCode == NETWORK_REQUEST){
				signInUser();
			}
		}
	}

	/**
	 * Prevent earlier launch of task, as it finish right after onPause callback
	 */
	private class DelayedCallback implements Runnable {

		private Intent data;
		private int resultCode;
		private int requestCode;

		private DelayedCallback(Intent data, int requestCode, int resultCode) {
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

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if(view.getId() == R.id.usernameEdt){
			usernameEdt.setSelection(usernameEdt.getText().length());
		} else if(view.getId() == R.id.passwordEdt){
			passwordEdt.setError(null);
		}
		return false;
	}

}