package com.chess.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.backend.tasks.PostDataTask;
import com.chess.ui.core.CoreActivity;
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
public class LoginScreenActivity extends CoreActivity implements View.OnClickListener, TextView.OnEditorActionListener {

	private EditText usernameEdt;
	private EditText passwordEdt;

	private Facebook facebook;
	private static int SIGNIN_CALLBACK_CODE = 16;
	private static int SIGNIN_FACEBOOK_CALLBACK_CODE = 128;
	private LoginUpdateListener loginUpdateListener;
	private int loginReturnCode;
	private ProgressDialog loginUpdateDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);

		usernameEdt = (EditText) findViewById(R.id.username);
		passwordEdt = (EditText) findViewById(R.id.password);
		passwordEdt.setOnEditorActionListener(this);

		findViewById(R.id.singin).setOnClickListener(this);
		findViewById(R.id.singup).setOnClickListener(this);
		findViewById(R.id.guestplay).setOnClickListener(this);

		LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.fb_connect);

		facebook = new Facebook(AppConstants.FACEBOOK_APP_ID);
		SessionStore.restore(facebook, this);

		SessionEvents.addAuthListener(new SampleAuthListener());
		SessionEvents.addLogoutListener(new SampleLogoutListener());
		facebookLoginButton.init(this, facebook);

		loginUpdateListener = new LoginUpdateListener();

		loginUpdateDialog = new ProgressDialog(this);
		loginUpdateDialog.setMessage(getString(R.string.signingin));
		loginUpdateDialog.setIndeterminate(true);
		loginUpdateDialog.setCancelable(false);
	}

	private void signInUser(){

		if (usernameEdt.getText().toString().length() < 3
				|| usernameEdt.getText().toString().length() > 20) {
			usernameEdt.setError(getString(R.string.check_field));
			usernameEdt.requestFocus();
			mainApp.showDialog(getContext(), getString(R.string.error), getString(R.string.validateUsername));
			return;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.LOGIN);
		loadItem.addRequestParams(RestHelper.P_USER_NAME, usernameEdt.getText().toString());
		loadItem.addRequestParams(RestHelper.P_PASSWORD, passwordEdt.getText().toString());

		new PostDataTask(loginUpdateListener).execute(loadItem);

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
		if(actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.FLAG_EDITOR_ACTION){
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

			new GetStringObjTask(loginUpdateListener).execute(loadItem);

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
			if (LoginScreenActivity.this.isFinishing())
				return;

			if(show){
				loginUpdateDialog.show();
			}else
				loginUpdateDialog.dismiss();
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				if (returnedObj.length() > 0) {
					final String[] responseArray = returnedObj.split(":");
					if (responseArray.length >= 4) {
						if (loginReturnCode == SIGNIN_CALLBACK_CODE) {
							preferencesEditor.putString(AppConstants.USERNAME, usernameEdt.getText().toString().trim().toLowerCase());
							doUpdate(responseArray);
						} else if (loginReturnCode == SIGNIN_FACEBOOK_CALLBACK_CODE && responseArray.length >= 5) {
							FlurryAgent.onEvent(FlurryData.FB_LOGIN, null);
							preferencesEditor.putString(AppConstants.USERNAME, responseArray[4].trim().toLowerCase());
							doUpdate(responseArray);
						}
					}
				}
			} else if (returnedObj.contains(RestHelper.R_FB_USER_HAS_ACCOUNT)) {
				showToast(R.string.no_chess_account_signup_please);
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.REGISTER_HTML)));
			} else if(returnedObj.contains(RestHelper.R_ERROR)){
				// Error+<error_message>
				showToast(returnedObj.substring(RestHelper.R_ERROR.length()));
			}
		}
	}

	@Override
	protected void onResume() {
		if (mainApp.isLiveChess()) {
			mainApp.setLiveChess(false);
		}
		super.onResume();
		usernameEdt.setText(AppData.getUserName(getContext()));
		passwordEdt.setText(preferences.getString(AppConstants.PASSWORD, StaticData.SYMBOL_EMPTY));
	}

	@Override
	public void update(int code) {

	}

	private void doUpdate(String[] response) {
		preferencesEditor.putString(AppConstants.PASSWORD, passwordEdt.getText().toString().trim());
		preferencesEditor.putString(AppConstants.USER_PREMIUM_STATUS, response[0].split("[+]")[1]);
		preferencesEditor.putString(AppConstants.API_VERSION, response[1]);
		try {
			preferencesEditor.putString(AppConstants.USER_TOKEN, URLEncoder.encode(response[2], AppConstants.UTF_8));
		} catch (UnsupportedEncodingException ignored) {
		}
		preferencesEditor.putString(AppConstants.USER_SESSION_ID, response[3]);
		preferencesEditor.commit();

		FlurryAgent.onEvent("Logged In"); // TODO hide to Flurry Data
		if (preferences.getBoolean(AppData.getUserName(getContext()) + AppConstants.PREF_NOTIFICATION, true)){
			AppUtils.startNotificationsUpdate(this);
		}

		mainApp.guest = false;

		Intent intent = new Intent(this, HomeScreenActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}