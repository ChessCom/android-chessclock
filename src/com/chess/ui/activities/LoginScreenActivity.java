package com.chess.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.CoreActivity;
import com.chess.ui.views.BackgroundChessDrawable;
import com.chess.utilities.MyProgressDialog;
import com.chess.utilities.Notifications;
import com.chess.utilities.Web;
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
public class LoginScreenActivity extends CoreActivity implements View.OnClickListener {

	private EditText username;
	private EditText password;

	private Facebook facebook;
	private LoginButton facebookLoginButton;
	private static int SIGNIN_CALLBACK_CODE = 16;
	private static int SIGNIN_FACEBOOK_CALLBACK_CODE = 128;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);


		findViewById(R.id.mainView).setBackgroundDrawable(new BackgroundChessDrawable(this));

		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);

		findViewById(R.id.singin).setOnClickListener(this);
		findViewById(R.id.singup).setOnClickListener(this);
		findViewById(R.id.guestplay).setOnClickListener(this);

		facebookLoginButton = (LoginButton) findViewById(R.id.fb_connect);

		facebook = new Facebook(AppConstants.FACEBOOK_APP_ID);
		SessionStore.restore(facebook, this);

		SessionEvents.addAuthListener(new SampleAuthListener());
		SessionEvents.addLogoutListener(new SampleLogoutListener());
		facebookLoginButton.init(this, facebook);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.singin) {

			if (username.getText().toString().length() < 3 || username.getText().toString().length() > 20) {
				mainApp.showDialog(context, getString(R.string.error), getString(R.string.validateUsername));
				return;
			}
			/*
			 * if(password.getText().toString().length() < 6 ||
			 * password.getText().toString().length() > 20){
			 * mainApp.showDialog(Singin.this, getString(R.string.error),
			 * getString(R.string.validatePassword)); return; }
			 */

			String query = "http://www." + LccHolder.HOST + AppConstants.API_V2_LOGIN;
			// String query = "http://" + LccHolder.HOST + "/api/v2/login";
			try {
				if (appService != null) {
					appService.RunSingleTaskPost(SIGNIN_CALLBACK_CODE, query, progressDialog = new MyProgressDialog(
							ProgressDialog.show(context, null, getString(R.string.signingin), true)),
							AppConstants.USERNAME, /* URLEncoder.encode( */username.getText().toString()/*
																								 * ,
																								 * "UTF-8"
																								 * )
																								 */, AppConstants.PASSWORD, /*
																												 * URLEncoder
																												 * .
																												 * encode
																												 * (
																												 */
							password.getText().toString()/* , "UTF-8") */
					);
				}
			} catch (Exception ignored) {
			}
		} else if (view.getId() == R.id.singup) {
			startActivity(new Intent(this, SignUpScreenActivity.class));
//			LoadNext(1);
		} else if (view.getId() == R.id.guestplay) {
			startActivity(new Intent(this, HomeScreenActivity.class));
//			LoadNext(2);
		}
	}

	public class SampleAuthListener implements SessionEvents.AuthListener {
		@Override
		public void onAuthSucceed() {
			String query = "http://www." + LccHolder.HOST + "/api/v2/login?facebook_access_token="
					+ facebook.getAccessToken() + "&return=username";
			response = Web.Request(query, "GET", null, null);
			if (response.contains("Success+")) {
				update(SIGNIN_FACEBOOK_CALLBACK_CODE);
			} else if (response.contains("Error+Facebook user has no Chess.com account")) {
				mainApp.showToast("You have no Chess.com account, sign up, please.");
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST
						+ "/register.html")));
			}
		}

		@Override
		public void onAuthFail(String error) {
			mainApp.showToast("Login Failed: " + error);
		}
	}

	public class SampleLogoutListener implements SessionEvents.LogoutListener {
		@Override
		public void onLogoutBegin() {
			mainApp.showToast("Logging out...");
		}

		@Override
		public void onLogoutFinish() {
			mainApp.showToast("You have logged out!");
		}
	}

	@Override
	protected void onResume() {
		if (mainApp.isLiveChess()) {
			mainApp.setLiveChess(false);
		}
		super.onResume();
		username.setText(mainApp.getSharedData().getString(AppConstants.USERNAME, ""));
		password.setText(mainApp.getSharedData().getString(AppConstants.PASSWORD, ""));
	}

	@Override
	public void update(int code) {
		if (response.length() > 0) {
			final String[] responseArray = response.split(":");
			if (responseArray.length >= 4) {
				if (code == SIGNIN_CALLBACK_CODE) {
					mainApp.getSharedDataEditor().putString(AppConstants.USERNAME, username.getText().toString().trim().toLowerCase());
					doUpdate(responseArray);
				} else if (code == SIGNIN_FACEBOOK_CALLBACK_CODE && responseArray.length >= 5) {
					FlurryAgent.onEvent("FB Login", null);
					mainApp.getSharedDataEditor().putString(AppConstants.USERNAME, responseArray[4].trim().toLowerCase());
					doUpdate(responseArray);
				}
			}
		}
	}

	private void doUpdate(String[] response) {
		mainApp.getSharedDataEditor().putString(AppConstants.PASSWORD, password.getText().toString().trim());
		mainApp.getSharedDataEditor().putString(AppConstants.USER_PREMIUM_STATUS, response[0].split("[+]")[1]);
		mainApp.getSharedDataEditor().putString(AppConstants.API_VERSION, response[1]);
		try {
			mainApp.getSharedDataEditor().putString(AppConstants.USER_TOKEN, URLEncoder.encode(response[2], "UTF-8"));
		} catch (UnsupportedEncodingException ignored) {
		}
		mainApp.getSharedDataEditor().putString(AppConstants.USER_SESSION_ID, response[3]);

		mainApp.getSharedDataEditor().commit();

		FlurryAgent.onEvent("Logged In", null);
		if (mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "")
				+ AppConstants.PREF_NOTIFICATION, true))
			startService(new Intent(this, Notifications.class));
		mainApp.guest = false;

        Intent intent = new Intent(this, HomeScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
	}
}