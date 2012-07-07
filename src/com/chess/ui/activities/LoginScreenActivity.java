package com.chess.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.view.KeyEvent;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * LoginScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 6:23
 */
public class LoginScreenActivity extends BaseFragmentActivity implements View.OnClickListener, TextView.OnEditorActionListener {

	private static final String CHECK_UPDATE_TAG = "check update";
	private static int SIGNIN_CALLBACK_CODE = 16;
	private static int SIGNIN_FACEBOOK_CALLBACK_CODE = 128;
	private static final int MIN_USERNAME_LENGTH = 3;
	private static final int MAX_USERNAME_LENGTH = 20;


	private EditText usernameEdt;
	private EditText passwordEdt;

	private Facebook facebook;
	private LoginUpdateListener loginUpdateListener;
	private int loginReturnCode;
	private AsyncTask<LoadItem, Void, Integer> loginTask;
	private AsyncTask<LoadItem, Void, Integer> postDataTask;
    private boolean forceFlag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);


		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);

		usernameEdt = (EditText) findViewById(R.id.username);
		passwordEdt = (EditText) findViewById(R.id.password);
		passwordEdt.setOnEditorActionListener(this);

		findViewById(R.id.singin).setOnClickListener(this);
		findViewById(R.id.singup).setOnClickListener(this);
		findViewById(R.id.guestplay).setOnClickListener(this);

		LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.fb_connect);

		facebook = new Facebook(AppConstants.FACEBOOK_APP_ID);
		SessionStore.restore(facebook, this);

		SessionEvents.dropAuthListeners();
		SessionEvents.addAuthListener(new SampleAuthListener());
		SessionEvents.dropLogoutListeners();
		SessionEvents.addLogoutListener(new SampleLogoutListener());
		facebookLoginButton.init(this, facebook);

		loginUpdateListener = new LoginUpdateListener();
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

		postDataTask = new PostDataTask(loginUpdateListener).executeTask(loadItem);

		loginReturnCode = SIGNIN_CALLBACK_CODE;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.singin) {
			signInUser();
		} else if (view.getId() == R.id.singup) {
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
			signInUser();
		}
		return false;
	}

	public class SampleAuthListener implements SessionEvents.AuthListener {
		@Override
		public void onAuthSucceed() {

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.LOGIN);
			loadItem.addRequestParams(RestHelper.P_FACEBOOK_ACCESS_TOKEN, facebook.getAccessToken());
			loadItem.addRequestParams(RestHelper.P_RETURN, RestHelper.V_USERNAME);

			loginTask = new GetStringObjTask(loginUpdateListener).executeTask(loadItem);

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
			showLoginProgress(show);
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				if (returnedObj.length() > 0) {
					final String[] responseArray = returnedObj.split(":");
					if (responseArray.length >= 4) {
						if (loginReturnCode == SIGNIN_CALLBACK_CODE) {
							preferencesEditor.putString(AppConstants.USERNAME, usernameEdt.getText().toString().trim().toLowerCase());
							processLogin(responseArray);
						} else if (loginReturnCode == SIGNIN_FACEBOOK_CALLBACK_CODE && responseArray.length >= 5) {
							FlurryAgent.onEvent(FlurryData.FB_LOGIN, null);
							preferencesEditor.putString(AppConstants.USERNAME, responseArray[4].trim().toLowerCase());
							processLogin(responseArray);
						}
					}
				}
			} else if (returnedObj.contains(RestHelper.R_FB_USER_HAS_ACCOUNT)) {
				showToast(R.string.no_chess_account_signup_please);
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.REGISTER_HTML)));
			} else if(returnedObj.contains(RestHelper.R_ERROR)){
				// Error+<error_message>
				showSinglePopupDialog(R.string.error, returnedObj.substring(RestHelper.R_ERROR.length()));
			}
		}
	}

	private void showLoginProgress(boolean show){
		if (isPaused)
			return;

		if(show){
            showPopupHardProgressDialog(R.string.signingin);
		}else {
            dismissProgressDialog();
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
			preferencesEditor.putString(AppConstants.USER_TOKEN, URLEncoder.encode(response[2], AppConstants.UTF_8));
		} catch (UnsupportedEncodingException ignored) {
		}
		preferencesEditor.putString(AppConstants.USER_SESSION_ID, response[3]);
		preferencesEditor.commit();

		FlurryAgent.onEvent(FlurryData.LOGGED_IN);
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

			showPopupDialog(R.string.update_check, R.string.update_available_please_update,
					CHECK_UPDATE_TAG);
			popupDialogFragment.setButtons(1);
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

				// as we are already on login screen, we don't need to open it again
//				startActivity(new Intent(getContext(), LoginScreenActivity.class));
//				finish();
			}
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.GOOGLE_PLAY_URI));
			startActivity(intent);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}


	@Override
	protected void onPause() {
		super.onPause();

		if(loginTask != null)
			loginTask.cancel(true);
		if(postDataTask != null)
			postDataTask.cancel(true);

	}

	@Override
	public void onBackPressed() {
		finish();
	}
}