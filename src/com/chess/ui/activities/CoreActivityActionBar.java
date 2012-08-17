package com.chess.ui.activities;

import actionbarcompat.ActionBarActivity;
import actionbarcompat.ActionBarHelper;
import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.SoundPlayer;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.backend.tasks.PostDataTask;
import com.chess.lcc.android.LccHolder;
import com.chess.lcc.android.interfaces.LiveChessClientEventListenerFace;
import com.chess.model.PopupItem;
import com.chess.ui.fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.ActiveFragmentInterface;
import com.chess.ui.interfaces.PopupDialogFace;
import com.facebook.android.Facebook;
import com.facebook.android.LoginButton;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.flurry.android.FlurryAgent;
import com.mopub.mobileads.MoPubView;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public abstract class CoreActivityActionBar extends ActionBarActivity implements View.OnClickListener
		, ActiveFragmentInterface, PopupDialogFace, LiveChessClientEventListenerFace {

	private static final String CONNECT_FAILED_TAG = "connect_failed";
	private static final String OBSOLETE_VERSION_TAG = "obsolete version";


	private static int SIGNIN_CALLBACK_CODE = 16;
	private static int SIGNIN_FACEBOOK_CALLBACK_CODE = 128;
	private static final int MIN_USERNAME_LENGTH = 3;
	private static final int MAX_USERNAME_LENGTH = 20;


	protected Bundle extras;
	protected Handler handler;
	protected boolean showActionSearch;
	protected boolean showActionSettings;
	protected boolean showActionNewGame;
	protected boolean showActionRefresh;

	// we may have this add on every screen, so control it on the lowest level
	protected MoPubView moPubView;
	private Facebook facebook;
	private LoginUpdateListener loginUpdateListener;
	private PopupCustomViewFragment reLoginFragment;
	private EditText usernameEdt;
	private EditText passwordEdt;
	private int loginReturnCode;

	public void setFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= StaticData.SDK_ICE_CREAM_SANDWICH && getActionBar() != null) {
			getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
					| ActionBar.DISPLAY_USE_LOGO
					| ActionBar.DISPLAY_SHOW_HOME
					| ActionBar.DISPLAY_SHOW_TITLE);
		}

		handler = new Handler();

		extras = getIntent().getExtras();

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
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		View mainView = findViewById(R.id.mainView);
		if (mainView != null)
			mainView.setBackgroundDrawable(backgroundChessDrawable);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(StaticData.SAVED_STATE, true);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		backgroundChessDrawable.updateConfig();
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume() {
		super.onResume();

		adjustActionBar();
	}

	@Override
	public void switchFragment(Fragment fragment) {

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.commit();
	}

	public void showProgress(boolean show) {
		getActionBarHelper().setRefreshActionItemState(show);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// try to destroy ad here as MoPub team suggested
		if (moPubView != null) {
			moPubView.destroy();
		}

		preferencesEditor.putLong(AppConstants.LAST_ACTIVITY_PAUSED_TIME, System.currentTimeMillis());
		preferencesEditor.commit();
	}

	protected void blockScreenRotation(boolean block){
		if(block){
			// Stop the screen orientation changing during an event
			if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}else{
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
	}

	protected void backToHomeActivity() {
		Intent intent = new Intent(this, HomeScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	protected void backToLoginActivity() {
		Intent intent = new Intent(this, LoginScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if (fragment.getTag().equals(CONNECT_FAILED_TAG)) {
			if (DataHolder.getInstance().isLiveChess()) {
				getLccHolder().logout();
			}
			backToHomeActivity();
		} else if (fragment.getTag().equals(OBSOLETE_VERSION_TAG)) {
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

			backToHomeActivity();
		}
	}

	private void adjustActionBar() {
		getActionBarHelper().showMenuItemById(R.id.menu_settings, showActionSettings);
		getActionBarHelper().showMenuItemById(R.id.menu_new_game, showActionNewGame);
		getActionBarHelper().showMenuItemById(R.id.menu_refresh, showActionRefresh);
		getActionBarHelper().showMenuItemById(R.id.menu_search, showActionSearch);
		getActionBarHelper().showMenuItemById(R.id.menu_singOut, LccHolder.getInstance(this).isConnected());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.sign_out, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_singOut, LccHolder.getInstance(this).isConnected(), menu);
		getActionBarHelper().showMenuItemById(R.id.menu_search, showActionSearch, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_settings, showActionSettings, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_new_game, showActionNewGame, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_refresh, showActionRefresh, menu);

		if(Build.VERSION.SDK_INT >= StaticData.SDK_HONEYCOMB){
			// Get the SearchView and set the searchable configuration
			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				backToHomeActivity();
				break;
			case R.id.menu_settings:
				startActivity(new Intent(this, PreferencesScreenActivity.class));
				break;
		}
		return super.onOptionsItemSelected(item);
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
		final View customView = inflater.inflate(R.layout.popup_relogin_frame, null, false);

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
				facebookLoginButton.init(CoreActivityActionBar.this, facebook);
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
				Log.d("TEST", "onConnectionFailure/ another login");
				getActionBarHelper().setRefreshActionItemState(false);
				getActionBarHelper().showMenuItemById(R.id.menu_singOut, false);
			}
		});

		if(isPaused)
			return;

		popupDialogFragment.setButtons(1);
		showPopupDialog(R.string.error, message, CONNECT_FAILED_TAG);
	}

    @Override
    public void onConnectionBlocked(final boolean blocked) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActionBarHelper().setRefreshActionItemState(blocked);
			}
		});

    }

    @Override
	public void onObsoleteProtocolVersion() {
		showPopupDialog(R.string.version_check, R.string.version_is_obsolete_update,
				OBSOLETE_VERSION_TAG);
		popupDialogFragment.setButtons(1);
		popupDialogFragment.setCancelable(false);
	}

	@Override
	public void onFriendsStatusChanged(){

	}

	@Override
	public void onAdminAnnounce(String message) {
		showSinglePopupDialog(message);
		popupDialogFragment.setButtons(1);
	}

	// -----------------------------------------------------


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
			facebook.authorizeCallback(requestCode, resultCode, data);
		}
	}

	protected LccHolder getLccHolder() {
		return LccHolder.getInstance(this);
	}

	public ActionBarHelper provideActionBarHelper() {
		return getActionBarHelper();
	}

	protected CoreActivityActionBar getInstance() {
		return this;
	}

	public SoundPlayer getSoundPlayer() {
		return SoundPlayer.getInstance(this);
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
							FlurryAgent.onEvent(FlurryData.FB_LOGIN, null);
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

