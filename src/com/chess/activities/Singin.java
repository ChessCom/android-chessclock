package com.chess.activities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.chess.R;
import com.chess.core.CoreActivity;
import com.chess.core.Tabs;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.MyProgressDialog;
import com.chess.utilities.Notifications;
import com.chess.utilities.Web;
import com.chess.views.BackgroundChessDrawable;
import com.facebook.android.Facebook;
import com.facebook.android.LoginButton;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionEvents.AuthListener;
import com.facebook.android.SessionEvents.LogoutListener;
import com.facebook.android.SessionStore;
import com.flurry.android.FlurryAgent;

public class Singin extends CoreActivity {

	private EditText username, password;

	private Facebook mFacebook;
	private LoginButton mLoginButton;
	private static int SIGNIN_CALLBACK_CODE = 16;
	private static int SIGNIN_FACEBOOK_CALLBACK_CODE = 128;

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		getWindow().setFormat(PixelFormat.RGBA_8888);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.singin);
		findViewById(R.id.mainView).setBackgroundDrawable(new BackgroundChessDrawable(this));

		/*
		 * getWindow().setFormat(PixelFormat.RGBA_8888);
		 * getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		 * 
		 * BitmapFactory.Options options = new BitmapFactory.Options();
		 * options.inPreferredConfig = Bitmap.Config.ARGB_8888; Bitmap gradient
		 * = BitmapFactory.decodeResource(getResources(), R.drawable.back_image,
		 * options);
		 * 
		 * findViewById(R.id.back).setBackgroundDrawable(new
		 * BitmapDrawable(gradient));
		 */

		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);

		findViewById(R.id.singin).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (username.getText().toString().length() < 3 || username.getText().toString().length() > 20) {
					mainApp.ShowDialog(Singin.this, getString(R.string.error), getString(R.string.validateUsername));
					return;
				}
				/*
				 * if(password.getText().toString().length() < 6 ||
				 * password.getText().toString().length() > 20){
				 * mainApp.ShowDialog(Singin.this, getString(R.string.error),
				 * getString(R.string.validatePassword)); return; }
				 */

				String query = "http://www." + LccHolder.HOST + "/api/v2/login";
				// String query = "http://" + LccHolder.HOST + "/api/v2/login";
				try {
					if (appService != null) {
						appService.RunSingleTaskPost(SIGNIN_CALLBACK_CODE, query, PD = new MyProgressDialog(
								ProgressDialog.show(Singin.this, null, getString(R.string.signingin), true)),
								"username", /* URLEncoder.encode( */username.getText().toString()/*
																								 * ,
																								 * "UTF-8"
																								 * )
																								 */, "password", /*
																												 * URLEncoder
																												 * .
																												 * encode
																												 * (
																												 */
								password.getText().toString()/* , "UTF-8") */
						);
					}
				} catch (Exception e) {
				}
			}
		});
		findViewById(R.id.singup).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LoadNext(1);
			}
		});
		findViewById(R.id.guestplay).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LoadNext(2);
			}
		});

		mLoginButton = (LoginButton) findViewById(R.id.fb_connect);

		mFacebook = new Facebook();

		mFacebook = new Facebook();
		SessionStore.restore(mFacebook, this);

		SessionEvents.addAuthListener(new SampleAuthListener());
		SessionEvents.addLogoutListener(new SampleLogoutListener());
		mLoginButton.init(mFacebook, new String[] {});
	}

	public class SampleAuthListener implements AuthListener {
		@Override
		public void onAuthSucceed() {
			String query = "http://www." + LccHolder.HOST + "/api/v2/login?facebook_access_token="
					+ mFacebook.getAccessToken() + "&return=username";
			response = Web.Request(query, "GET", null, null);
			if (response.contains("Success+")) {
				Update(SIGNIN_FACEBOOK_CALLBACK_CODE);
			} else if (response.contains("Error+Facebook user has no Chess.com account")) {
				mainApp.ShowMessage("You have no Chess.com account, sign up, please.");
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST
						+ "/register.html")));
			}
		}

		@Override
		public void onAuthFail(String error) {
			mainApp.ShowMessage("Login Failed: " + error);
		}
	}

	public class SampleLogoutListener implements LogoutListener {
		@Override
		public void onLogoutBegin() {
			mainApp.ShowMessage("Logging out...");
		}

		@Override
		public void onLogoutFinish() {
			mainApp.ShowMessage("You have logged out!");
		}
	}

	@Override
	protected void onResume() {
		if (mainApp.isLiveChess()) {
			mainApp.setLiveChess(false);
		}
		super.onResume();
		username.setText(mainApp.getSharedData().getString("username", ""));
		password.setText(mainApp.getSharedData().getString("password", ""));
	}

	@Override
	public void LoadNext(int code) {
		switch (code) {
		case 0: {
			FlurryAgent.onEvent("Logged In", null);
			if (mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString("username", "") + "notifE", true))
				startService(new Intent(this, Notifications.class));
			mainApp.guest = false;
			startActivity(new Intent(this, Tabs.class));
			finish();
			break;
		}
		case 1: {
			startActivity(new Intent(this, Register.class));
			break;
		}
		case 2: {
			FlurryAgent.onEvent("Guest Login", null);
			mainApp.guest = true;
			startActivity(new Intent(this, Tabs.class));
			break;
		}
		default:
			break;
		}
	}

	@Override
	public void LoadPrev(int code) {
		finish();
	}

	@Override
	public void Update(int code) {
		if (response.length() > 0) {
			final String[] responseArray = response.split(":");
			if (responseArray.length >= 4) {
				if (code == SIGNIN_CALLBACK_CODE) {
					mainApp.getSharedDataEditor().putString("username", username.getText().toString().trim().toLowerCase());
					doUpdate(responseArray);
				} else if (code == SIGNIN_FACEBOOK_CALLBACK_CODE && responseArray.length >= 5) {
					FlurryAgent.onEvent("FB Login", null);
					mainApp.getSharedDataEditor().putString("username", responseArray[4].trim().toLowerCase());
					doUpdate(responseArray);
				}
			}
		}
	}

	private void doUpdate(String[] response) {
		mainApp.getSharedDataEditor().putString("password", password.getText().toString().trim());
		mainApp.getSharedDataEditor().putString("premium_status", response[0].split("[+]")[1]);
		mainApp.getSharedDataEditor().putString("api_version", response[1]);
		try {
			mainApp.getSharedDataEditor().putString("user_token", URLEncoder.encode(response[2], "UTF-8"));
		} catch (UnsupportedEncodingException e) {
		}
		mainApp.getSharedDataEditor().putString("user_session_id", response[3]);

		mainApp.getSharedDataEditor().commit();
		LoadNext(0);
	}
}
