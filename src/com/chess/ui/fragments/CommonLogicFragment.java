package com.chess.ui.fragments;

import android.app.Activity;
import android.content.*;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.LoginItem;
import com.chess.backend.entity.api.RegisterItem;
import com.chess.backend.image_load.ImageGetter;
import com.chess.backend.image_load.bitmapfun.ImageCache;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbDataProvider;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.model.DataHolder;
import com.chess.model.TacticsDataHolder;
import com.chess.statics.*;
import com.chess.ui.activities.CoreActivityActionBar;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.fragments.daily.DailyGamesRightFragment;
import com.chess.ui.fragments.home.HomeTabsFragment;
import com.chess.ui.fragments.live.LiveGameWaitFragment;
import com.chess.ui.fragments.profiles.ProfileTabsFragment;
import com.chess.ui.fragments.stats.StatsBasicFragment;
import com.chess.ui.fragments.stats.StatsGameFragment;
import com.chess.ui.fragments.welcome.SignInFragment;
import com.chess.ui.fragments.welcome.SignUpFragment;
import com.chess.ui.fragments.welcome.WelcomeTabsFragment;
import com.chess.ui.fragments.welcome.WelcomeTabsFragmentTablet;
import com.chess.ui.interfaces.ActiveFragmentInterface;
import com.chess.utilities.AppUtils;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.ProfileImageView;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.flurry.android.FlurryAgent;
import com.slidingmenu.lib.SlidingMenu;
import uk.co.senab.actionbarpulltorefresh.PullToRefreshAttacher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.chess.db.DbScheme.PROVIDER_NAME;
import static com.chess.statics.AppConstants.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.01.13
 * Time: 10:18
 */
public abstract class CommonLogicFragment extends BasePopupsFragment implements View.OnClickListener,
		PullToRefreshAttacher.OnRefreshListener, Session.StatusCallback, ProfileImageView.ProfileOpenFace {

	private static final int MIN_USERNAME_LENGTH = 3;
	private static final int MAX_USERNAME_LENGTH = 20;
	private static final String IMAGE_CACHE_DIR = "thumbs";
	protected static final int NON_INIT = -1;
	public static final long VIEW_UPDATE_DELAY = 500;

	protected static final String RE_LOGIN_TAG = "re-login popup";
	protected static final String NETWORK_CHECK_TAG = "network check popup";
	protected static final int NETWORK_REQUEST = 3456;
	protected static final String CHESS_NO_ACCOUNT_TAG = "chess no account popup";
	private static final String LOCKED_ACCOUNT_TAG = "locked account popup";
	protected static final String CHECK_UPDATE_TAG = "check update";

	protected static final String OPPONENT_NAME = "opponent_name";
	protected static final String MODE = "mode";
	protected static final String CONFIG = "config";
	protected static final String USERNAME = "username";

	public static final int CENTER_MODE = 1;
	public static final int RIGHT_MENU_MODE = 2;

	protected static final int DEFAULT_ICON = 0;
	protected static final int ONE_ICON = 1;
	protected static final int TWO_ICON = 2;
	//	protected static final long SIDE_MENU_DELAY = 150;
	protected static final long SIDE_MENU_DELAY = 50;
	private static final long SWITCH_DELAY = 50;
	private static final long PULL_TO_UPDATE_RELEASE_DELAY = 1000;

	private LoginUpdateListener loginUpdateListener;
	private LoginUpdateListener facebookLoginUpdateListener;

	private ActiveFragmentInterface activityFace;
	protected static Handler handler;
	private EditText loginUsernameEdt;
	private EditText passwordEdt;

	protected SharedPreferences preferences;
	protected SharedPreferences.Editor preferencesEditor;
	private CharSequence title;
	protected UiLifecycleHelper facebookUiHelper;
	private boolean facebookActive;
	protected View loadingView;
	private int padding;
	private int paddingCode;
	private boolean slideMenusEnabled;
	protected boolean need2update = true;
	protected boolean inSearch;
	private boolean needToChangeActionButtons = true;
	private boolean loadingImages;
	private SmartImageFetcher imageFetcher;
	protected float density;
	protected int screenWidth;
	protected int screenHeight;
	private HashMap<String, ImageGetter.TextImage> textViewsImageCache;
	private AbsListView listView;
	private boolean usePullToRefresh;
	protected boolean isTablet;
	private IntentFilter startLiveGameFilter;
	private StartLiveGameReceiver startLiveGameReceiver;
	protected ColorStateList themeFontColorStateList;
	/* Recent Opponents */
	protected View inviteFriendView1;
	protected View inviteFriendView2;
	protected TextView friendUserName1Txt;
	protected TextView friendUserName2Txt;
	protected TextView friendRealName1Txt;
	protected TextView friendRealName2Txt;
	protected String firstFriendUserName;
	protected String secondFriendUserName;

	protected AppUtils.DeviceInfo deviceInfo;


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activityFace = (ActiveFragmentInterface) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FragmentActivity activity = getActivity();

		deviceInfo = new AppUtils.DeviceInfo().getDeviceInfo(getActivity());

		preferences = getAppData().getPreferences();
		preferencesEditor = getAppData().getEditor();

		handler = new Handler();
		setHasOptionsMenu(true);
		if (StaticData.USE_TABLETS) {
			isTablet = AppUtils.is7InchTablet(activity) || AppUtils.is10InchTablet(activity);
		}

		density = getResources().getDisplayMetrics().density;
		screenWidth = getResources().getDisplayMetrics().widthPixels;
		screenHeight = getResources().getDisplayMetrics().heightPixels;
		padding = (int) (48 * density);

		textViewsImageCache = new HashMap<String, ImageGetter.TextImage>();

		{// initialize imageFetcher
			ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(activity, IMAGE_CACHE_DIR);
			cacheParams.setMemCacheSizePercent(0.15f); // Set memory cache to 25% of app memory

			// The ImageFetcher takes care of loading images into our ImageView children asynchronously
			imageFetcher = new SmartImageFetcher(activity);
			imageFetcher.setLoadingImage(R.drawable.img_profile_picture_stub);
			imageFetcher.addImageCache(activity.getSupportFragmentManager(), cacheParams);
		}
		startLiveGameFilter = new IntentFilter(IntentConstants.START_LIVE_GAME);

		themeFontColorStateList = FontsHelper.getInstance().getThemeColorStateList(activity, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		enableSlideMenus(true);

		loadingView = view.findViewById(R.id.loadingView);
		listView = (AbsListView) view.findViewById(R.id.listView);

		if (needToChangeActionButtons) {
			getActivityFace().showActionMenu(R.id.menu_add, false);
			getActivityFace().showActionMenu(R.id.menu_search, false);
			getActivityFace().showActionMenu(R.id.menu_share, false);
			getActivityFace().showActionMenu(R.id.menu_cancel, false);
			getActivityFace().showActionMenu(R.id.menu_accept, false);
			getActivityFace().showActionMenu(R.id.menu_edit, false);
			getActivityFace().showActionMenu(R.id.menu_message, false);
			getActivityFace().showActionMenu(R.id.menu_challenge, false);
			getActivityFace().showActionMenu(R.id.menu_search_btn, false);
			getActivityFace().showActionMenu(R.id.menu_notifications, true);
			getActivityFace().showActionMenu(R.id.menu_games, true);
		}

		if (loadingImages) {
			if (listView != null) {
				listView.setOnScrollListener(new AbsListView.OnScrollListener() {
					@Override
					public void onScrollStateChanged(AbsListView absListView, int scrollState) {
						// Pause fetcher to ensure smoother scrolling when flinging
						if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
							imageFetcher.setPauseWork(true);
						} else {
							imageFetcher.setPauseWork(false);
						}
					}

					@Override
					public void onScroll(AbsListView absListView, int firstVisibleItem,
										 int visibleItemCount, int totalItemCount) {
					}
				});
			}
		}

		setTitlePadding(DEFAULT_ICON);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		updateSlidingMenuState();

		loginUpdateListener = new LoginUpdateListener();
		facebookLoginUpdateListener = new LoginUpdateListener(LoginUpdateListener.FACEBOOK);

		LoginButton facebookButton = (LoginButton) getView().findViewById(R.id.fb_connect);
		if (facebookButton != null) {
			facebookUiHelper = new UiLifecycleHelper(getActivity(), this);
			facebookUiHelper.onCreate(savedInstanceState);
			facebookActive = true;
			facebookInit(facebookButton);
		}

		if (usePullToRefresh && listView != null) {
			getActivityFace().setPullToRefreshView(listView, CommonLogicFragment.this);
		}
		PullToRefreshAttacher pullToRefreshAttacher = getActivityFace().getPullToRefreshAttacher();
		if (pullToRefreshAttacher != null) {
			pullToRefreshAttacher.showProgress(false); // hide if it was showing from previous screen
		}
	}

	protected void updateSlidingMenuState() {
		getActivityFace().setTouchModeToSlidingMenu(slideMenusEnabled ? SlidingMenu.TOUCHMODE_FULLSCREEN
				: SlidingMenu.TOUCHMODE_NONE);
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

		// if we use image loading, then process imageFetcher states
		if (loadingImages) {
			imageFetcher.setExitTasksEarly(false);
		}

		// register receiver to start live game
		startLiveGameReceiver = new StartLiveGameReceiver();
		registerReceiver(startLiveGameReceiver, startLiveGameFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (facebookActive) {
			facebookUiHelper.onPause();
		}

		if (loadingImages) {
			imageFetcher.setPauseWork(false);
			imageFetcher.setExitTasksEarly(true);
			imageFetcher.flushCache();
		}

		dismissProgressDialog();

		unRegisterMyReceiver(startLiveGameReceiver);
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

		if (loadingImages) {
			imageFetcher.closeCache();
		}
	}

	protected void setNeedToChangeActionButtons(boolean change) {
		needToChangeActionButtons = change;
	}

	protected void showActionBar(boolean show) {
		getActivityFace().showActionBar(show);
	}

	protected void setTitle(int titleId) {
		this.title = getString(titleId);
	}

	protected void setTitle(String title) {
		this.title = title;
	}

	/**
	 * ONE_ICON means minus 1 from default state, because be default there are 2 icons on right side
	 *
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
		if (!TextUtils.isEmpty(title)) {
			getActivityFace().updateTitle(title);
		}
	}

	protected void enableSlideMenus(boolean enable) {
		slideMenusEnabled = enable;
	}

	protected void facebookInit(LoginButton loginBtn) {
		loginBtn.setFragment(this);
		loginBtn.setReadPermissions(Arrays.asList("user_status", "email"));
		loginBtn.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
			@Override
			public void onUserInfoFetched(GraphUser user) {
			}
		});
	}

	@Override
	public void call(Session session, SessionState state, Exception exception) {
		onSessionStateChange(session, state, exception); // TODO create protected method to inform who need
	}

	protected void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state != null && state.isOpened()) {
			loginWithFacebook(session.getAccessToken());
		} else if (exception != null) {
			logTest(exception.getMessage());
			showToast(exception.getMessage());
			session.close();
		}
	}

	private void loginWithFacebook(String accessToken) {
		if (getActivity() == null) {
			return;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_LOGIN);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_FACEBOOK_ACCESS_TOKEN, accessToken);
		loadItem.addRequestParams(RestHelper.P_DEVICE_ID, getDeviceId());
		loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.V_USERNAME);
		loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.V_TACTICS_RATING);

		facebookLoginUpdateListener.setFacebookToken(accessToken);
		new RequestJsonTask<LoginItem>(facebookLoginUpdateListener).executeTask(loadItem);
	}

	protected void setLoginFields(EditText passedUsernameEdt, EditText passedPasswordEdt) {
		this.loginUsernameEdt = passedUsernameEdt;
		this.passwordEdt = passedPasswordEdt;
	}

	protected CoreActivityActionBar getInstance() {
		return activityFace.getActionBarActivity();
	}

	public ActiveFragmentInterface getActivityFace() {
		return activityFace;
	}

	protected void registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
		getActivity().registerReceiver(broadcastReceiver, intentFilter);
	}

	protected void unRegisterMyReceiver(BroadcastReceiver broadcastReceiver) {
		if (broadcastReceiver != null) {
			try {
				getActivity().unregisterReceiver(broadcastReceiver);
			} catch (IllegalArgumentException ignore) {

			}
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
		String username = getTextFromField(loginUsernameEdt);
		if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
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
		loadItem.setLoadPath(RestHelper.getInstance().CMD_LOGIN);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_DEVICE_ID, getDeviceId());
		loadItem.addRequestParams(RestHelper.P_USER_NAME_OR_MAIL, username);
		loadItem.addRequestParams(RestHelper.P_PASSWORD, getTextFromField(passwordEdt));
		loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.P_USERNAME);
		loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.P_TACTICS_RATING);

		new RequestJsonTask<LoginItem>(loginUpdateListener).executeTask(loadItem);
	}

	protected void signInUser(EditText loginUsernameEdt, EditText passwordEdt) {
		String username = getTextFromField(loginUsernameEdt);
		if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
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

		this.passwordEdt = passwordEdt;

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_LOGIN);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_DEVICE_ID, getDeviceId());
		loadItem.addRequestParams(RestHelper.P_USER_NAME_OR_MAIL, username);
		loadItem.addRequestParams(RestHelper.P_PASSWORD, getTextFromField(passwordEdt));
		loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.P_USERNAME);
		loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.P_TACTICS_RATING);

		new RequestJsonTask<LoginItem>(loginUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onClick(View view) {

	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(RE_LOGIN_TAG)) {
			performLogout();
		} else if (tag.equals(LOCKED_ACCOUNT_TAG)) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.chess.com")));
		} else if (tag.equals(CHESS_NO_ACCOUNT_TAG)) {
			getActivityFace().openFragment(new SignUpFragment());
		}
		super.onPositiveBtnClick(fragment);
	}

	protected void updateNotificationBadges() {
		getActivityFace().updateNotificationsBadges();
	}

	protected String getDeviceId() {
		FragmentActivity activity = getActivity();
		if (activity != null) {
			String deviceId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
			if (TextUtils.isEmpty(deviceId)) {
				deviceId = getAppData().getDeviceId();
				if (TextUtils.isEmpty(deviceId)) { // generate a new one
					deviceId = "Hello" + (Math.random() * 100) + "There" + System.currentTimeMillis();
					getAppData().setDeviceId(deviceId);
				}
			}

			deviceId = ImageCache.hashKeyForDisk(deviceId);
			return deviceId.substring(0, 32);
		} else {
			return ImageCache.hashKeyForDisk("Hello" + (Math.random() * 100) + "There" + System.currentTimeMillis());
		}
	}

	public void onSearchQuery(String query) {
	}

	public void onSearchAutoCompleteQuery(String query) {
	}

	@Override
	public void onRefreshStarted(View view) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				getActivityFace().getPullToRefreshAttacher().setRefreshComplete();
			}
		}, PULL_TO_UPDATE_RELEASE_DELAY);
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
			if (getActivityFace().getPullToRefreshAttacher() != null) {
				getActivityFace().getPullToRefreshAttacher().showProgress(show);
			} else {
				if (loadingView != null) {
					loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			// perform auto re-login here
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.INVALID_LOGIN_TOKEN_SUPPLIED) {
					performReLogin();
					return;
				}
			}
			super.errorHandle(resultCode);
		}
	}

	private void performReLogin() {
		if (DataHolder.getInstance().isPerformingRelogin()) {
			return;
		}

		DataHolder.getInstance().setPerformingRelogin(true);

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_LOGIN);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_DEVICE_ID, getDeviceId());
		loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.P_USERNAME);
		loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.P_TACTICS_RATING);

		if (!TextUtils.isEmpty(getAppData().getFacebookToken())) { // Login with facebook
			loadItem.addRequestParams(RestHelper.P_FACEBOOK_ACCESS_TOKEN, getAppData().getFacebookToken());

			new RequestJsonTask<LoginItem>(facebookLoginUpdateListener).executeTask(loadItem);
		} else if (!TextUtils.isEmpty(getAppData().getPassword())) { // login with credentials
			loadItem.addRequestParams(RestHelper.P_USER_NAME_OR_MAIL, getUsername());
			loadItem.addRequestParams(RestHelper.P_PASSWORD, getAppData().getPassword());

			new RequestJsonTask<LoginItem>(loginUpdateListener).executeTask(loadItem);
		} else {
			if (!TextUtils.isEmpty(getUserToken())) {
				showToast(R.string.unable_to_relogin);
			}

			DataHolder.getInstance().setPerformingRelogin(false);
		}
	}

	protected class ChessLoadUpdateListener<ItemType> extends ChessUpdateListener<ItemType> {

		public ChessLoadUpdateListener() {
			super();
		}

		public ChessLoadUpdateListener(Class<ItemType> clazz) {
			super(clazz);
		}

		@Override
		public void showProgress(boolean show) {
			showLoadingProgress(show);
		}
	}

	private class LoginUpdateListener extends ChessLoadUpdateListener<LoginItem> {

		public static final int FACEBOOK = 1;
		private int loginReturnCode;
		private String facebookToken;

		public LoginUpdateListener() {
			super(LoginItem.class);
		}

		public LoginUpdateListener(int loginReturnCode) {
			super(LoginItem.class);

			this.loginReturnCode = loginReturnCode;
		}

		@Override
		public void showProgress(boolean show) {
			if (show) {
				showPopupProgressDialog();
			} else {
				dismissProgressDialog();
			}
		}

		@Override
		public void updateData(LoginItem returnedObj) {
			DataHolder.getInstance().setPerformingRelogin(false);

			LoginItem.Data loginData = returnedObj.getData();
			String username = loginData.getUsername();

			if (!TextUtils.isEmpty(username)) {
				preferencesEditor.putString(USERNAME, username);
			}

			preferencesEditor.putString(username + PREF_USER_AVATAR_URL, loginData.getAvatarUrl());
			preferencesEditor.putLong(username + PREF_USER_ID, loginData.getUserId());
			preferencesEditor.putInt(username + PREF_USER_COUNTRY_ID, loginData.getCountryId());

			if (loginData.getTacticsRating() != 0) {
				preferencesEditor.putInt(username + PREF_USER_TACTICS_RATING, loginData.getTacticsRating());
			}
			preferencesEditor.putInt(username + USER_PREMIUM_STATUS, loginData.getPremiumStatus());
			preferencesEditor.putString(LIVE_SESSION_ID, loginData.getSessionId());
			preferencesEditor.putLong(LIVE_SESSION_ID_SAVE_TIME, System.currentTimeMillis());
			preferencesEditor.commit();

			if (loginReturnCode == FACEBOOK && !TextUtils.isEmpty(facebookToken)) {
				FlurryAgent.logEvent(FlurryData.FB_LOGIN);
				// save facebook access token to appData for future re-login
				getAppData().setFacebookToken(facebookToken);
			}
			processLogin(loginData);

			// force to create pullToRefresh view, to display progress line instead of spinner with fade
			getActivityFace().setPullToRefreshView(null, CommonLogicFragment.this);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			dismissProgressDialog();

			if (RestHelper.containsServerCode(resultCode)) {
				// get server code
				int serverCode = RestHelper.decodeServerCode(resultCode);
				switch (serverCode) {
					case ServerErrorCodes.ACCOUNT_LOCKED:
						popupItem.setButtons(1);
						showPopupDialog(R.string.your_account_locked, LOCKED_ACCOUNT_TAG);
						return;
					case ServerErrorCodes.INVALID_USERNAME_PASSWORD:
						if (passwordEdt != null) {
							passwordEdt.setError(getResources().getString(R.string.invalid_username_or_password));
							passwordEdt.requestFocus();
						} else {
							showSinglePopupDialog(R.string.login, R.string.invalid_username_or_password);
						}
						getAppData().setPassword(Symbol.EMPTY);
						return;
					case ServerErrorCodes.FACEBOOK_USER_NO_ACCOUNT:
						showPopupDialog(R.string.no_chess_account_signup_please, CHESS_NO_ACCOUNT_TAG);
						return;
				}
			}
			super.errorHandle(resultCode);
		}

		public void setFacebookToken(String facebookToken) {
			this.facebookToken = facebookToken;
		}
	}

	protected void processLogin(RegisterItem.Data returnedObj) {
		if (getActivity() == null) { // if accidentally return in wrong callback, when widgets are not initialized
			Log.e("Login", "activity is dead");
			return;
		}

		if (passwordEdt != null) {
			preferencesEditor.putString(PASSWORD, getTextFromField(passwordEdt));
		}
		preferencesEditor.putString(USER_TOKEN, returnedObj.getLoginToken());
		preferencesEditor.putLong(USER_TOKEN_SAVE_TIME, System.currentTimeMillis());
		preferencesEditor.commit();

		getAppData().setLiveChessMode(false);
		DataHolder.reset();
		TacticsDataHolder.reset();

		getActivityFace().registerGcm();

		afterLogin();

		getActivityFace().updateMainBackground();
	}

	protected void afterLogin() {
		FlurryAgent.logEvent(FlurryData.LOGGED_IN);
		backToHomeFragment();
	}

	protected void showLoadingProgress(boolean show) {
		PullToRefreshAttacher pullToRefreshAttacher = getActivityFace().getPullToRefreshAttacher();
		if (pullToRefreshAttacher != null) {
			pullToRefreshAttacher.showProgress(show);
		} else {
			if (show) {
				showPopupProgressDialog();
			} else {
				dismissProgressDialog();
			}
		}
	}

	protected Fragment findFragmentByTag(String tag) {
		return getFragmentManager().findFragmentByTag(tag);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_games: {
				CommonLogicFragment fragment = (CommonLogicFragment) findFragmentByTag(DailyGamesRightFragment.class.getSimpleName());
				if (fragment == null) {
					fragment = new DailyGamesRightFragment();
				}
				getActivityFace().changeRightFragment(fragment);
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						getActivityFace().toggleRightMenu();
					}
				}, SIDE_MENU_DELAY);
				return true;
			}
			case R.id.menu_notifications: {// bell icon
				CommonLogicFragment fragment = (CommonLogicFragment) findFragmentByTag(NotificationsRightFragment.class.getSimpleName());
				if (fragment == null) {
					fragment = new NotificationsRightFragment();
				}

				getActivityFace().changeRightFragment(fragment);
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						getActivityFace().toggleRightMenu();
					}
				}, SIDE_MENU_DELAY);
				return true;
			}
		}
		return false;
	}

	protected void logTest(String messageToLog) {
		Log.d("TEST", messageToLog);
	}

	protected AppData getAppData() {
		return getActivityFace().getMeAppData();
	}

	protected String getUsername() {
		return getActivityFace().getMeUsername();
	}

	protected String getUserToken() {
		return getActivityFace().getMeUserToken();
	}

	protected void setFacebookActive(boolean active) {
		facebookActive = active;
	}

	protected void performLogout() {
		if (getActivity() == null) {
			return;
		}

		// logout from facebook
		Session facebookSession = Session.getActiveSession();
		if (facebookSession != null) {
			facebookSession.closeAndClearTokenInformation();
			Session.setActiveSession(null);
		} else {
			facebookSession = new Session(getActivity());
			Session.setActiveSession(facebookSession);
			facebookSession.closeAndClearTokenInformation();
		}

		preferencesEditor.putString(PASSWORD, Symbol.EMPTY);
		preferencesEditor.putString(USER_TOKEN, Symbol.EMPTY);
		preferencesEditor.putLong(USER_TOKEN_SAVE_TIME, System.currentTimeMillis());
		preferencesEditor.commit();

		AppUtils.cancelNotifications(getActivity());
		getActivityFace().clearFragmentStack();
		// make pause to wait while transactions complete
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!isTablet) {
					getActivityFace().switchFragment(new WelcomeTabsFragment());
				} else {
					getActivityFace().switchFragment(new WelcomeTabsFragmentTablet());
				}
			}
		}, SWITCH_DELAY);

		// set default theme
		getActivityFace().setMainBackground(R.drawable.img_theme_green_felt);

		// clear comp game
		ChessBoardComp.resetInstance();
		getAppData().clearSavedCompGame();

		// clear username
		getAppData().setUsername(AppConstants.GUEST_NAME);
	}

	protected void clearTempData() {
		// un-register from GCM
//		unRegisterGcmService();

		// logout from facebook
		Session facebookSession = Session.getActiveSession();
		if (facebookSession != null) {
			facebookSession.closeAndClearTokenInformation();
			Session.setActiveSession(null);
		}

		AppUtils.cancelNotifications(getActivity());

		// set default theme
		getActivityFace().setMainBackground(R.drawable.img_theme_green_felt);

		// clear comp game
		ChessBoardComp.resetInstance();
		getAppData().clearSavedCompGame();

		RestHelper.resetInstance();

		ContentProviderClient client = getContentResolver().acquireContentProviderClient(PROVIDER_NAME);
		SQLiteDatabase dbHandle = ((DbDataProvider) client.getLocalContentProvider()).getDbHandle();

		DbDataProvider.DatabaseHelper dbHelper = ((DbDataProvider) client.getLocalContentProvider()).getDbHelper();
		dbHelper.onUpgrade(dbHandle, DbDataProvider.getDbVersion(), DbDataProvider.getDbVersion() + 1);
	}

	public int getStatusBarHeight() {
		Rect rect = new Rect();
		Window window = getActivity().getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rect);
		return rect.top;
	}

	public boolean isNeedToUpgrade() {
		return getAppData().getUserPremiumStatus() < StaticData.GOLD_USER;
	}

	public boolean isNeedToUpgradePremium() {
		return getAppData().getUserPremiumStatus() < StaticData.DIAMOND_USER;
	}

	protected String upCaseFirst(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
	}

	protected List<String> getItemsFromEntries(int entries) {
		String[] array = getResources().getStringArray(entries);
		return getItemsFromArray(array);
	}

	protected List<String> getItemsFromArray(String[] array) {
		List<String> items = new ArrayList<String>();
		items.addAll(Arrays.asList(array));
		return items;
	}

	protected SmartImageFetcher getImageFetcher() {
		// most adapters are initiated in onCreate().
		// So when we reach onViewCreated callback we will add logic to listView scroll listener
		loadingImages = true;

		return imageFetcher;
	}

//	public void printHashKey() { //Don't remove, use to find needed facebook hashkey
//		try {
//			PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(),
//					PackageManager.GET_SIGNATURES);
//			for (Signature signature : info.signatures) {
//				MessageDigest md = MessageDigest.getInstance("SHA");
//				md.update(signature.toByteArray());
//				Log.d("TEMPTAGHASH KEY:",
//						Base64.encodeToString(md.digest(), Base64.DEFAULT));
//			}
//		} catch (PackageManager.NameNotFoundException e) {
//		} catch (NoSuchAlgorithmException e) {
//		}
//	}

	protected void loadTextWithImage(TextView textView, String sourceStr) {
		textView.setText(Html.fromHtml(sourceStr, getImageGetter(textView, sourceStr), null));
	}

	protected void loadTextWithImage(TextView textView, String sourceStr, int imageSize) {
		textView.setText(Html.fromHtml(sourceStr, getImageGetter(textView, sourceStr, imageSize), null));
	}

	protected ImageGetter getImageGetter(TextView textView, String sourceStr) {
		return new ImageGetter(getActivity(), textViewsImageCache, textView, sourceStr, screenWidth);
	}

	protected ImageGetter getImageGetter(TextView textView, String sourceText, int imageSize) {
		return new ImageGetter(getActivity(), textViewsImageCache, textView, sourceText, imageSize);
	}

	protected String getDaysString(int cnt) {
		if (cnt > 1) {
			return getString(R.string.days_arg, cnt);
		} else {
			return getString(R.string.day_arg, cnt);
		}
	}

	public void pullToRefresh(boolean usePullToRefresh) {
		this.usePullToRefresh = usePullToRefresh;
	}

	/**
	 * Use it in inherited fragment where you want to consume onBackPressed event
	 *
	 * @return {@code true} if event was successfully consumed
	 */
	public boolean showPreviousFragment() {
		return false;
	}

	protected boolean inLandscape() {
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	protected boolean inPortrait() {
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}

	protected boolean isNetworkAvailable() {
		return getActivity() != null && AppUtils.isNetworkAvailable(getActivity());
	}

	public void updateFontColors() {
		themeFontColorStateList = FontsHelper.getInstance().getThemeColorStateList(getActivity(), true);
	}

	private class StartLiveGameReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!DataHolder.getInstance().isLiveGameOpened()) {
				getActivityFace().openFragment(new LiveGameWaitFragment());
				DataHolder.getInstance().setLiveGameOpened(true);
			}
		}
	}

	protected void loadRecentOpponents() {
		if (inviteFriendView1 == null) { // if widgets init wasn't called yet, we skip
			return;
		}
		String username = getUsername();
		Cursor cursor = DbDataManager.getRecentOpponentsCursor(getActivity(), username);
		if (cursor != null && cursor.moveToFirst()) {
			if (cursor.getCount() > 1) {
				inviteFriendView1.setVisibility(View.VISIBLE);
				inviteFriendView1.setOnClickListener(this);
				inviteFriendView2.setVisibility(View.VISIBLE);
				inviteFriendView2.setOnClickListener(this);

				firstFriendUserName = DbDataManager.getString(cursor, DbScheme.V_BLACK_USERNAME);
				if (firstFriendUserName.equals(username)) {
					firstFriendUserName = DbDataManager.getString(cursor, DbScheme.V_WHITE_USERNAME);
				}
				friendUserName1Txt.setText(firstFriendUserName);

				cursor.moveToNext();
				secondFriendUserName = firstFriendUserName;

				boolean anotherPlayerFound = false;
				while (secondFriendUserName.equals(firstFriendUserName)) {
					if (cursor.isAfterLast()) {
						break;
					}
					secondFriendUserName = DbDataManager.getString(cursor, DbScheme.V_BLACK_USERNAME);
					if (secondFriendUserName.equals(username)) {
						secondFriendUserName = DbDataManager.getString(cursor, DbScheme.V_WHITE_USERNAME);
					}
					if (!secondFriendUserName.equals(firstFriendUserName)) {
						anotherPlayerFound = true;
						break;
					} else {
						cursor.moveToNext();
					}
				}

				if (anotherPlayerFound) {
					friendUserName2Txt.setText(secondFriendUserName);
				} else {
					inviteFriendView2.setVisibility(View.GONE);
				}

			} else if (cursor.getCount() == 1) {
				inviteFriendView1.setVisibility(View.VISIBLE);
				inviteFriendView1.setOnClickListener(this);

				firstFriendUserName = DbDataManager.getString(cursor, DbScheme.V_BLACK_USERNAME);
				if (firstFriendUserName.equals(username)) {
					firstFriendUserName = DbDataManager.getString(cursor, DbScheme.V_WHITE_USERNAME);
				}
				friendUserName1Txt.setText(firstFriendUserName);
			}

		} else {
			inviteFriendView1.setVisibility(View.GONE);
			inviteFriendView2.setVisibility(View.GONE);
		}

		if (cursor != null) {
			cursor.close();
		}
	}

	protected void loadRecentFriends() {
		if (inviteFriendView1 == null) { // if widgets init wasn't called yet, we skip
			return;
		}
		String username = getUsername();
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getRecentFriends(username));
		if (cursor != null && cursor.moveToFirst()) {
			if (cursor.getCount() > 1) {
				inviteFriendView1.setVisibility(View.VISIBLE);
				inviteFriendView1.setOnClickListener(this);
				inviteFriendView2.setVisibility(View.VISIBLE);
				inviteFriendView2.setOnClickListener(this);

				firstFriendUserName = DbDataManager.getString(cursor, DbScheme.V_USERNAME);
				friendUserName1Txt.setText(firstFriendUserName);

				cursor.moveToNext();
				secondFriendUserName = firstFriendUserName;

				boolean anotherPlayerFound = false;
				while (secondFriendUserName.equals(firstFriendUserName)) {
					if (cursor.isAfterLast()) {
						break;
					}
					secondFriendUserName = DbDataManager.getString(cursor, DbScheme.V_USERNAME);
					if (!secondFriendUserName.equals(firstFriendUserName)) {
						anotherPlayerFound = true;
						break;
					} else {
						cursor.moveToNext();
					}
				}

				if (anotherPlayerFound) {
					friendUserName2Txt.setText(secondFriendUserName);
				} else {
					inviteFriendView2.setVisibility(View.GONE);
				}

			} else if (cursor.getCount() == 1) {
				inviteFriendView1.setVisibility(View.VISIBLE);
				inviteFriendView1.setOnClickListener(this);

				firstFriendUserName = DbDataManager.getString(cursor, DbScheme.V_USERNAME);
				friendUserName1Txt.setText(firstFriendUserName);
			}

		} else {
			inviteFriendView1.setVisibility(View.GONE);
			inviteFriendView2.setVisibility(View.GONE);
		}

		if (cursor != null) {
			cursor.close();
		}
	}

	protected void openStatsForUser(int statId, String username) {
		if (isNeedToUpgrade()) {
			getActivityFace().openFragment(StatsBasicFragment.createInstance(username));
		} else {
			getActivityFace().openFragment(StatsGameFragment.createInstance(statId, username));
		}
	}

	protected String feedbackBodyCompose(String username) {
		return getResources().getString(R.string.feedback_mail_body) + ": \n\n"
				+ AppConstants.OS_VERSION + AppConstants.SDK_API + deviceInfo.SDK_API + Symbol.NEW_STR
				+ AppConstants.DEVICE + deviceInfo.MODEL + Symbol.NEW_STR
				+ AppConstants.APP_VERSION + deviceInfo.APP_VERSION_CODE + Symbol.SLASH + deviceInfo.APP_VERSION_NAME + Symbol.NEW_STR
				+ AppConstants.USERNAME_ + username;
	}

	@Override
	public void openProfile(String username) {
		getActivityFace().openFragment(ProfileTabsFragment.createInstance(username));
	}
}
