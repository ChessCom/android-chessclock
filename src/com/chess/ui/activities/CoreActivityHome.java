package com.chess.ui.activities;

import actionbarcompat.ActionBarActivityHome;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.chess.R;
import com.chess.SerialLinLay;
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
import com.chess.lcc.android.LccHolder;
import com.chess.lcc.android.interfaces.LiveChessClientEventListenerFace;
import com.chess.model.PopupItem;
import com.chess.ui.fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.PopupDialogFace;
import com.facebook.android.Facebook;
import com.facebook.android.LoginButton;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.flurry.android.FlurryAgent;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public abstract class CoreActivityHome extends ActionBarActivityHome implements PopupDialogFace,
		LiveChessClientEventListenerFace, View.OnClickListener {

	private static final String TAG = "CoreActivityHome";
	private static final String CONNECT_FAILED_TAG = "connect_failed";
	public static final String OBSOLETE_VERSION_TAG = "obsolete version";

	private static int SIGNIN_CALLBACK_CODE = 16;
	private static int SIGNIN_FACEBOOK_CALLBACK_CODE = 128;
	private static final int MIN_USERNAME_LENGTH = 3;
	private static final int MAX_USERNAME_LENGTH = 20;

	private boolean forceFlag;

	private Facebook facebook;
	private LoginUpdateListener loginUpdateListener;
	private PopupCustomViewFragment reLoginFragment;
	private EditText usernameEdt;
	private EditText passwordEdt;
	private int loginReturnCode;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean(StaticData.SAVED_STATE)) {
				checkUserTokenAndStartActivity();
			}
		}

		LccHolder.getInstance(this).setLiveChessClientEventListener(this);

		loginUpdateListener = new LoginUpdateListener();
		LccHolder.getInstance(this).setLiveChessClientEventListener(this);

		facebook = new Facebook(AppConstants.FACEBOOK_APP_ID);
		SessionStore.restore(facebook, this);

		SessionEvents.dropAuthListeners();
		SessionEvents.addAuthListener(new SampleAuthListener());
		SessionEvents.dropLogoutListeners();
		SessionEvents.addLogoutListener(new SampleLogoutListener());

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(StaticData.SAVED_STATE, true);
	}

	@Override
	protected void onResume() {
		super.onResume();

		long startDay = preferences.getLong(AppConstants.START_DAY, 0);
		Log.d("CheckUpdateTask", "startDay loaded, = " + startDay);

		if (startDay == 0 || !DateUtils.isToday(startDay)) {
			checkUpdate();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		preferencesEditor.putLong(AppConstants.LAST_ACTIVITY_PAUSED_TIME, System.currentTimeMillis());
		preferencesEditor.commit();

		//mainApp.setForceBannerAdOnFailedLoad(false);
	}


	private void checkUserTokenAndStartActivity() {
		if (!AppData.getUserName(this).equals(StaticData.SYMBOL_EMPTY)) {
			Intent intent = new Intent(this, HomeScreenActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} else {
			startActivity(new Intent(this, LoginScreenActivity.class));
		}
	}

	protected LccHolder getLccHolder() {
		return LccHolder.getInstance(this);
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if (fragment.getTag().equals(CONNECT_FAILED_TAG)) {
			if (DataHolder.getInstance().isLiveChess()) {
				getLccHolder().logout();
			}
		}
		 else if (fragment.getTag().equals(OBSOLETE_VERSION_TAG)) {
			// Show site and
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					DataHolder.getInstance().setLiveChess(false);
					LccHolder.getInstance(getContext()).setConnected(false);
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse(RestHelper.PLAY_ANDROID_HTML)));
				}
			});
		}
		if (fragment.getTag().equals(CHECK_UPDATE_TAG)) {
			if (forceFlag) {
				// drop start day
				preferencesEditor.putLong(AppConstants.START_DAY, 0);
				preferencesEditor.commit();

				backToLoginActivity();
			}
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.GOOGLE_PLAY_URI));
			startActivity(intent);
		}
	}

	// ---------- LiveChessClientEventListenerFace ----------------
	@Override
	public void onConnecting() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActionBarHelper().showMenuItemById(R.id.menu_singOut, false);
				getActionBarHelper().setRefreshActionItemState(true);
			}
		});
	}

	@Override
	public void onConnectionEstablished() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActionBarHelper().setRefreshActionItemState(false);
				getActionBarHelper().showMenuItemById(R.id.menu_singOut, true);
			}
		});
	}

	@Override
	public void onSessionExpired(String message) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final SerialLinLay customView = (SerialLinLay) inflater.inflate(R.layout.popup_relogin_frame, null, false);

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(customView);

		reLoginFragment = PopupCustomViewFragment.newInstance(popupItem);
		reLoginFragment.show(getSupportFragmentManager(), RE_LOGIN_TAG);

		getLccHolder().logout();

		((TextView) customView.findViewById(R.id.titleTxt)).setText(message);

		usernameEdt = (EditText) customView.findViewById(R.id.usernameEdt);
		passwordEdt = (EditText) customView.findViewById(R.id.passwordEdt);
		customView.findViewById(R.id.re_signin).setOnClickListener(this);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				LoginButton facebookLoginButton = (LoginButton) customView.findViewById(R.id.re_fb_connect);
				facebookLoginButton.init(CoreActivityHome.this, facebook);
				facebookLoginButton.logout();
			}
		});


		usernameEdt.setText(AppData.getUserName(this));
	}


	@Override
	public void onConnectionFailure(String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActionBarHelper().setRefreshActionItemState(false);
				getActionBarHelper().showMenuItemById(R.id.menu_singOut, false);
			}
		});

		showPopupDialog(R.string.error, message, CONNECT_FAILED_TAG);
//		popupDialogFragment.setButtons(1);
		getLastPopupFragment().setButtons(1);

	}

    @Override
    public void onConnectionBlocked(boolean blocked) {
    }

    @Override
	public void onObsoleteProtocolVersion() {
		showPopupDialog(R.string.version_check, R.string.version_is_obsolete_update, OBSOLETE_VERSION_TAG);
		getLastPopupFragment().setButtons(1);
		getLastPopupFragment().setCancelable(false);
//		popupDialogFragment.setButtons(1);
//		popupDialogFragment.setCancelable(false);
	}

	@Override
	public void onFriendsStatusChanged(){
	}

	@Override
	public void onAdminAnnounce(String message) {
		showSinglePopupDialog(message);
		getLastPopupFragment().setButtons(1);
//		popupDialogFragment.setButtons(1);
	}

	// -----------------------------------------------------


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
		}
	}

	protected void backToLoginActivity() {
		Intent intent = new Intent(this, LoginScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.re_signin){
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
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK && requestCode == Facebook.DEFAULT_AUTH_ACTIVITY_CODE){
			Log.d("TEST", "HomeActivity onActivityResult -> facebook authorize");
			facebook.authorizeCallback(requestCode, resultCode, data);
		}
	}

	private class SampleAuthListener implements SessionEvents.AuthListener {
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
		}
	}

	private class LoginUpdateListener extends AbstractUpdateListener<String> {
		public LoginUpdateListener() {
			super(getContext());
		}

		@Override
		public void showProgress(boolean show) {
			if(show){
				showPopupHardProgressDialog(R.string.signingin);
			}else {
				Log.d("TEST", "HomeActivity LoginUpdateListener ->  dismissProgressDialog();, isPaused = " + isPaused);
				dismissProgressDialog();
			}
		}

		@Override
		public void updateData(String returnedObj) {
			if(isPaused)
				return;

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
				if(message.equals(RestHelper.R_INVALID_PASS)){
					passwordEdt.setError(getResources().getString(R.string.invalid_password));
					passwordEdt.requestFocus();
				}else{
					showSinglePopupDialog(R.string.error, message);
				}
			}
		}
	}

	private void processLogin(String[] response) {
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

		restartActivity();
	}

	private class SampleLogoutListener implements SessionEvents.LogoutListener {
		@Override
		public void onLogoutBegin() {
		}

		@Override
		public void onLogoutFinish() {
		}
	}
}