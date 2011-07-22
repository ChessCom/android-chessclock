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
import com.chess.utilities.MyProgressDialog;
import com.chess.utilities.Notifications;
import com.chess.utilities.Web;
import com.facebook.android.Facebook;
import com.facebook.android.LoginButton;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.facebook.android.SessionEvents.AuthListener;
import com.facebook.android.SessionEvents.LogoutListener;
import com.flurry.android.FlurryAgent;

public class Singin extends CoreActivity {

	private EditText username, password;

	private Facebook mFacebook;
  private LoginButton mLoginButton;
  private static int SIGNIN_CALLBACK_CODE = 16;
  private static int SIGNIN_FACEBOOK_CALLBACK_CODE = 128;

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
        String query = "http://www." + LccHolder.HOST + "/api/v2/login";
        try
        {
          if(appService != null)
          {
            appService.RunSingleTaskPost(SIGNIN_CALLBACK_CODE,
                                         query,
                                         PD = new MyProgressDialog(
                                           ProgressDialog.show(Singin.this, null, getString(R.string.signingin), true)),
                                         "username", /*URLEncoder.encode(*/username.getText().toString()/*, "UTF-8")*/,
                                         "password", /*URLEncoder.encode(*/password.getText().toString()/*, "UTF-8")*/
            );
          }
        }
        catch(Exception e) {}
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

    mFacebook = new Facebook();
    SessionStore.restore(mFacebook, this);

        SessionEvents.addAuthListener(new SampleAuthListener());
        SessionEvents.addLogoutListener(new SampleLogoutListener());
        mLoginButton.init(mFacebook, new String[]{});
	}

	public class SampleAuthListener implements AuthListener {
		public void onAuthSucceed() {
			String query = "http://www." + LccHolder.HOST + "/api/v2/login?facebook_access_token="+mFacebook.getAccessToken()+"&return=username";
            response = Web.Request(query, "GET", null, null);
            if(response.contains("Success+")){
            	Update(SIGNIN_FACEBOOK_CALLBACK_CODE);
            } else if(response.contains("Error+Facebook user has no Chess.com account")){
            	App.ShowMessage("You have no Chess.com account, sign up, please.");
            	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST + "/register.html")));
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
		username.setText(App.sharedData.getString("username", ""));
    password.setText(App.sharedData.getString("password", ""));
	}

	@Override
	public void LoadNext(int code) {
		switch (code) {
			case 0:{
        FlurryAgent.onEvent("Logged In", null);
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
				FlurryAgent.onEvent("Guest Login", null);
        App.guest = true;
				startActivity(new Intent(this, Tabs.class));
				break;
			}
			default: break;
		}
	}
	@Override
	public void LoadPrev(int code) {
		//finish();
	}
	@Override
  public void Update(int code)
  {
    if(response.length() > 0)
    {
      final String[] responseArray = response.split(":");
      if(responseArray.length >= 4)
      {
        if(code == SIGNIN_CALLBACK_CODE)
        {
          App.SDeditor.putString("username", username.getText().toString().trim().toLowerCase());
          doUpdate(responseArray);
        }
        else if(code == SIGNIN_FACEBOOK_CALLBACK_CODE && responseArray.length >= 5)
        {
          FlurryAgent.onEvent("FB Login", null);
          App.SDeditor.putString("username", responseArray[4].trim().toLowerCase());
          doUpdate(responseArray);
        }
      }
    }
  }

  private void doUpdate(String[] response)
  {
    App.SDeditor.putString("password", password.getText().toString().trim());
    App.SDeditor.putString("premium_status", response[0].split("[+]")[1]);
    App.SDeditor.putString("api_version", response[1]);
    try {
      App.SDeditor.putString("user_token", URLEncoder.encode(response[2], "UTF-8"));
    } catch (UnsupportedEncodingException e) {}
    App.SDeditor.putString("user_session_id", response[3]);

    App.SDeditor.commit();
    LoadNext(0);
  }
}
