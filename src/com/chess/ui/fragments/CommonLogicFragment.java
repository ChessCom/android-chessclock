package com.chess.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCode;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.TacticsDataHolder;
import com.chess.backend.entity.new_api.LoginItem;
import com.chess.backend.entity.new_api.RegisterItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.*;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.activities.CoreActivityActionBar;
import com.chess.ui.fragments.daily.DailyGamesNotificationFragment;
import com.chess.ui.fragments.home.HomePlayFragment;
import com.chess.ui.fragments.home.HomeTabsFragment;
import com.chess.ui.fragments.welcome.SignInFragment;
import com.chess.ui.interfaces.ActiveFragmentInterface;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.flurry.android.FlurryAgent;
import com.slidingmenu.lib.SlidingMenu;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.01.13
 * Time: 10:18
 */
public abstract class CommonLogicFragment extends BasePopupsFragment implements View.OnClickListener {

	private static final int SIGNIN_FACEBOOK_CALLBACK_CODE = 128;
	private static final int SIGNIN_CALLBACK_CODE = 16;
	protected static final long FACEBOOK_DELAY = 200;
	private static final int MIN_USERNAME_LENGTH = 3;
	private static final int MAX_USERNAME_LENGTH = 20;

	protected static final String MODE = "mode";

	public static final int CENTER_MODE = 1;
	public static final int RIGHT_MENU_MODE = 2;

	protected static final int DEFAULT_ICON = 0;
	protected static final int ONE_ICON = 1;
	protected static final int TWO_ICON = 2;
	protected static final long SIDE_MENU_DELAY = 100;

	private LoginUpdateListenerNew loginUpdateListener;

	private int loginReturnCode;
	private ActiveFragmentInterface activityFace;
	protected static Handler handler;
	private EditText loginUsernameEdt;
	private EditText passwordEdt;

	protected SharedPreferences preferences;
	protected SharedPreferences.Editor preferencesEditor;
	private int titleId;
	private GraphUser facebookUser;
	private UiLifecycleHelper facebookUiHelper;
	private boolean facebookActive;
	protected View loadingView;
	private int padding;
	private int paddingCode;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activityFace = (ActiveFragmentInterface) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		preferences = AppData.getPreferences(getActivity());
		preferencesEditor = preferences.edit();

		handler = new Handler();
		setHasOptionsMenu(true);

		padding = (int) (48 * getResources().getDisplayMetrics().density);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		loadingView = view.findViewById(R.id.loadingView);

		getActivityFace().showActionMenu(R.id.menu_add, false);
		getActivityFace().showActionMenu(R.id.menu_search, false);
		getActivityFace().showActionMenu(R.id.menu_share, false);
		getActivityFace().showActionMenu(R.id.menu_notifications, true);
		getActivityFace().showActionMenu(R.id.menu_games, true);

		setTitlePadding(DEFAULT_ICON);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// set full screen touch
		getActivityFace().setTouchModeToSlidingMenu(SlidingMenu.TOUCHMODE_FULLSCREEN);

		LoginButton loginButton = (LoginButton) getView().findViewById(R.id.fb_connect);
		if (loginButton != null) {
			facebookUiHelper = new UiLifecycleHelper(getActivity(), callback);
			facebookUiHelper.onCreate(savedInstanceState);
			facebookActive = true;
			facebookInit(loginButton);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// update title and padding here when activity will finish it's setups for actionbar
		updateTitle();
		setTitlePadding();
		getActivityFace().updateActionBarIcons();

		if (facebookActive) {
			facebookUiHelper.onResume();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (facebookActive) {
			facebookUiHelper.onPause();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (facebookActive) {
			facebookUiHelper.onSaveInstanceState(outState);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (facebookActive) {
			facebookUiHelper.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (facebookActive) {
			facebookUiHelper.onDestroy();
		}
	}

	protected void showActionBar(boolean show) {
		getActivityFace().showActionBar(show);
	}

	protected void setTitle(int titleId) {
		this.titleId = titleId;
	}

	/**
	 * ONE_ICON means minus 1 from default state, because be default there are 2 icons on right side
	 * @param code can be 1 or 2 - corresponds for one or two icons offset
	 */
	protected void setTitlePadding(int code) {
		paddingCode = code;
	}

	private void setTitlePadding() {
		if (paddingCode == ONE_ICON) {
			getActivityFace().setTitlePadding(0);
		} else if (paddingCode == TWO_ICON) { // TODO
			getActivityFace().setTitlePadding(padding);
		} else if (paddingCode == DEFAULT_ICON) {
			getActivityFace().setTitlePadding(padding);
		}
	}

	private void updateTitle() {
		if (titleId != 0) {
			getActivityFace().updateTitle(titleId);
		}
	}

	protected void unRegisterGcmService() {
		getActivityFace().unRegisterGcm();
	}

	protected void facebookInit(LoginButton loginBtn) {
		loginBtn.setFragment(this);
		loginBtn.setReadPermissions(Arrays.asList("user_status", "email"));
		loginBtn.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
			@Override
			public void onUserInfoFetched(GraphUser user) {
				facebookUser = user;
			}
		});


		loginUpdateListener = new LoginUpdateListenerNew();
	}

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception); // TODO create protected method to inform who need
		}
	};

	protected void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state != null && state.isOpened()) {
			loginWithFacebook(session);
		}
	}

	private void loginWithFacebook(Session session) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_LOGIN);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_FACEBOOK_ACCESS_TOKEN, session.getAccessToken());
		loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.V_USERNAME);
		loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.V_TACTICS_RATING);

		new RequestJsonTask<LoginItem>(loginUpdateListener).executeTask(loadItem);
		loginReturnCode = SIGNIN_FACEBOOK_CALLBACK_CODE;
	}

	protected void setLoginFields(EditText passedUsernameEdt, EditText passedPasswordEdt) {
		this.loginUsernameEdt = passedUsernameEdt;
		this.passwordEdt = passedPasswordEdt;
	}

	protected CoreActivityActionBar getInstance() {
		return activityFace.getActionBarActivity();
	}

	protected ActiveFragmentInterface getActivityFace() {
		return activityFace;
	}

	protected void registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
		getActivity().registerReceiver(broadcastReceiver, intentFilter);
	}

	protected void unRegisterMyReceiver(BroadcastReceiver broadcastReceiver) {
		if (broadcastReceiver != null) {
			getActivity().unregisterReceiver(broadcastReceiver);
		}
	}

	protected void backToHomeFragment() {
		getActivityFace().clearFragmentStack();
		getActivityFace().switchFragment(new HomeTabsFragment());
	}

	protected void backToLoginFragment() {
		getActivityFace().switchFragment(new SignInFragment());
	}

	public SoundPlayer getSoundPlayer() {
		return SoundPlayer.getInstance(getActivity());
	}

	protected void signInUser() {
		String userName = getTextFromField(loginUsernameEdt);
		if (userName.length() < MIN_USERNAME_LENGTH || userName.length() > MAX_USERNAME_LENGTH) {
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
		loadItem.setLoadPath(RestHelper.CMD_LOGIN);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_DEVICE_ID, getDeviceId());
		loadItem.addRequestParams(RestHelper.P_USER_NAME_OR_MAIL, userName);
		loadItem.addRequestParams(RestHelper.P_PASSWORD, getTextFromField(passwordEdt));
		loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.P_USERNAME);
		loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.P_TACTICS_RATING);

		new RequestJsonTask<LoginItem>(loginUpdateListener).executeTask(loadItem);

		loginReturnCode = SIGNIN_CALLBACK_CODE;
	}

	@Override
	public void onClick(View view) {

	}

	protected void setBadgeValueForId(int menuId, int value) {
		getActivityFace().setBadgeValueForId(menuId, value);
	}

	protected String getDeviceId() {
		FragmentActivity activity = getActivity();
		if (activity != null) {
			String string = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
			while ((string != null ? string.length() : 0) < 32) {
				string += "a";
			}
			return string;
		} else {
			return StaticData.SYMBOL_EMPTY;
		}
	}

	protected class ChessUpdateListener<ItemType> extends ActionBarUpdateListener<ItemType> {

		public ChessUpdateListener(Class<ItemType> clazz) {
			super(getInstance(), CommonLogicFragment.this, clazz);
		}

		public ChessUpdateListener() {
			super(getInstance(), CommonLogicFragment.this);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			if (loadingView != null) {
				loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
			}
		}
	}

	private class LoginUpdateListenerNew extends AbstractUpdateListener<LoginItem> {
		public LoginUpdateListenerNew() {
			super(getContext(), LoginItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			if (show) {
				showPopupHardProgressDialog(R.string.signing_in_);
			} else {
				if (isPaused)
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
			if (returnedObj.getData().getTacticsRating() != 0) {
				AppData.setUserTacticsRating(getActivity(), returnedObj.getData().getTacticsRating());
			}
			preferencesEditor.putInt(AppConstants.USER_PREMIUM_STATUS, returnedObj.getData().getPremiumStatus());
			processLogin(returnedObj.getData());
		}

		@Override
		public void errorHandle(Integer resultCode) {
			dismissProgressDialog();

			if (RestHelper.containsServerCode(resultCode)) {
				// get server code
				int serverCode = RestHelper.decodeServerCode(resultCode);
				switch (serverCode) {
					case ServerErrorCode.INVALID_USERNAME_PASSWORD:
						showSinglePopupDialog(R.string.login, R.string.invalid_username_or_password);
						passwordEdt.requestFocus();
						break;
					case ServerErrorCode.FACEBOOK_USER_NO_ACCOUNT:
						popupItem.setPositiveBtnId(R.string.sign_up);
						showPopupDialog(R.string.no_chess_account_signup_please, CHESS_NO_ACCOUNT_TAG);
						break;
					default:
						String serverMessage = ServerErrorCode.getUserFriendlyMessage(getActivity(), serverCode); // TODO restore
						showToast(serverMessage);

						break;
				}
			}
		}

		@Override
		public void errorHandle(String resultMessage) {
			if (resultMessage.contains(RestHelper.R_FB_USER_HAS_NO_ACCOUNT)) {
				popupItem.setPositiveBtnId(R.string.sign_up);
				showPopupDialog(R.string.no_chess_account_signup_please, CHESS_NO_ACCOUNT_TAG);
			} else {
			}
		}
	}

	protected void processLogin(RegisterItem.Data returnedObj) {
		if (passwordEdt == null || getActivity() == null) { // if accidentally return in wrong callback, when widgets are not initialized
			return;
		}

		preferencesEditor.putLong(AppConstants.USER_ID, returnedObj.getUserId());
		AppData.setUserCountryId(getActivity(), returnedObj.getCountryId());
		logTest("setting avatar url = " + returnedObj.getAvatarUrl());
		AppData.setUserAvatar(getActivity(), returnedObj.getAvatarUrl());
		preferencesEditor.putString(AppConstants.PASSWORD, passwordEdt.getText().toString().trim());

		try {
			preferencesEditor.putString(AppConstants.USER_TOKEN, URLEncoder.encode(returnedObj.getLoginToken(), HTTP.UTF_8));
		} catch (UnsupportedEncodingException ignored) {
			preferencesEditor.putString(AppConstants.USER_TOKEN, returnedObj.getLoginToken());
//			showSinglePopupDialog(R.string.error, R.string.error_occurred_while_login); // or use that logic?
//			return;
		}
// 		preferencesEditor.putString(AppConstants.USER_SESSION_ID, response[3]); // TODO used only for live, so should be separate connection to live
		preferencesEditor.commit();

		AppData.setLiveChessMode(getActivity(), false);
		DataHolder.reset();
		TacticsDataHolder.reset();

		getActivityFace().registerGcm();

		afterLogin();
	}

	protected void afterLogin() {
		FlurryAgent.logEvent(FlurryData.LOGGED_IN);
		backToHomeFragment();
	}


	protected Fragment findFragmentByTag(String tag) {
		return getFragmentManager().findFragmentByTag(tag);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {   // Should be called to enable OptionsMenu handle
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_games:
				getActivityFace().changeRightFragment(HomePlayFragment.newInstance(RIGHT_MENU_MODE));
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						getActivityFace().toggleRightMenu();
					}
				}, SIDE_MENU_DELAY);
				break;
			case R.id.menu_notifications:
				CommonLogicFragment fragment = (CommonLogicFragment) findFragmentByTag(DailyGamesNotificationFragment.class.getSimpleName());
				if (fragment == null) {
					fragment = new DailyGamesNotificationFragment();
				}
				getActivityFace().changeRightFragment(fragment);
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						getActivityFace().toggleRightMenu();
					}
				}, SIDE_MENU_DELAY);
				break;
		}
		return true;
	}

	protected void logTest(String messageToLog) {
		Log.d("TEST", messageToLog);
	}
}
