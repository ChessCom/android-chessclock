package com.chess.activities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.chess.R;
import com.chess.core.CoreActivity;
import com.chess.core.Tabs;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.Notifications;
import com.chess.utilities.Web;
import com.facebook.android.Facebook;
import com.facebook.android.LoginButton;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.facebook.android.SessionEvents.AuthListener;
import com.facebook.android.SessionEvents.LogoutListener;

public class Singin extends CoreActivity {

	private EditText username, password;

	private Facebook mFacebook;
  private LoginButton mLoginButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.singin);

		username = (EditText)findViewById(R.id.username);
		password = (EditText)findViewById(R.id.password);

		findViewById(R.id.singin).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if(username.getText().toString().length() < 3 || username.getText().toString().length() > 20){
					App.ShowDialog(Singin.this, getString(R.string.error), getString(R.string.validateUsername));
					return;
				}
				if(password.getText().toString().length() < 6 || password.getText().toString().length() > 20){
					App.ShowDialog(Singin.this, getString(R.string.error), getString(R.string.validatePassword));
					return;
				}

				String query = "";
				try {
					query = "http://www." + LccHolder.HOST + "/api/login?username="
                  +URLEncoder.encode(username.getText().toString(), "UTF-8")+"&password="
                  +URLEncoder.encode(password.getText().toString(), "UTF-8");
				} catch (Exception e) {}

				if(appService != null){
					appService.RunSingleTask(0,
						query,
						PD = ProgressDialog.show(Singin.this, null, getString(R.string.signingin), true)
					);
				}
			}
		});
		findViewById(R.id.singup).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				LoadNext(1);
			}
		});
		findViewById(R.id.guestplay).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				LoadNext(2);
			}
		});

		mLoginButton = (LoginButton) findViewById(R.id.fb_connect);

		mFacebook = new Facebook();

        SessionStore.restore(mFacebook, this);
        SessionEvents.addAuthListener(new SampleAuthListener());
        SessionEvents.addLogoutListener(new SampleLogoutListener());
        mLoginButton.init(mFacebook, new String[]{});
	}

	public class SampleAuthListener implements AuthListener {
		public void onAuthSucceed() {
			String query = "http://www." + LccHolder.HOST + "/api/login?facebook_access_token="+mFacebook.getAccessToken();

            response = Web.Request(query, "GET", null, null);
            if(response.contains("Success+")){
            	Update(0);
            } else if(response.contains("Error+Facebook user has no Chess.com account")){
            	App.ShowMessage("You have no Chess.com account, sign up, please.");
            	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://secure." + LccHolder.HOST + "/register.html")));
            }
		}
		public void onAuthFail(String error) {
			App.ShowMessage("Login Failed: " + error);
		}
	}
	public class SampleLogoutListener implements LogoutListener {
		public void onLogoutBegin() {
			App.ShowMessage("Logging out...");
		}
		public void onLogoutFinish() {
			App.ShowMessage("You have logged out!");
		}
	}

	@Override
	protected void onResume() {
    if (App.isLiveChess())
    {
      App.setLiveChess(false);
    }
		super.onResume();
		username.post(new Runnable() {
			public void run() {
				username.setText(App.sharedData.getString("username", ""));
			}
		});
		password.post(new Runnable() {
			public void run() {
				password.setText(App.sharedData.getString("password", ""));
			}
		});
	}

	@Override
	public void LoadNext(int code) {
		switch (code) {
			case 0:{
				if(App.sharedData.getBoolean(App.sharedData.getString("username", "")+"notifE", true))
		        	startService(new Intent(this, Notifications.class));
				App.guest = false;
				startActivity(new Intent(this, Tabs.class));
				finish();
				break;
			}
			case 1:{
				startActivity(new Intent(this, Register.class));
				break;
			}
			case 2:{
				App.guest = true;
				startActivity(new Intent(this, Tabs.class));
				break;
			}
			default: break;
		}
	}
	@Override
	public void LoadPrev(int code) {
		finish();
	}
	@Override
	public void Update(int code) {
		if(code == 0 && response.length()>0){
      String[] r = response.split(":");
			App.SDeditor.putString("username", username.getText().toString().trim().toLowerCase());
			App.SDeditor.putString("password", password.getText().toString().trim());

      App.SDeditor.putString("premium_status", r[0].split("[+]")[1]);
      App.SDeditor.putString("api_version", r[1]);
      try {
        App.SDeditor.putString("user_token", URLEncoder.encode(r[2], "UTF-8"));
      } catch (UnsupportedEncodingException e) {}
      App.SDeditor.putString("user_session_id", r[3]);

			App.SDeditor.commit();
			LoadNext(0);
		}
	}
}
