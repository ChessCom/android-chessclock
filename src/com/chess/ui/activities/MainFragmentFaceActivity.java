package com.chess.ui.activities;

import android.app.Activity;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import com.chess.R;
import com.chess.backend.GetAndSaveTheme;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.themes.ThemeItem;
import com.chess.backend.image_load.bitmapfun.ImageCache;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.model.BaseGameItem;
import com.chess.model.DataHolder;
import com.chess.statics.*;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.NavigationMenuFragment;
import com.chess.ui.fragments.NotificationsRightFragment;
import com.chess.ui.fragments.articles.ArticleDetailsFragment;
import com.chess.ui.fragments.articles.ArticlesFragment;
import com.chess.ui.fragments.daily.GameDailyFragment;
import com.chess.ui.fragments.daily.GameDailyFragmentTablet;
import com.chess.ui.fragments.home.HomeTabsFragment;
import com.chess.ui.fragments.lessons.LessonsFragment;
import com.chess.ui.fragments.live.GameLiveFragment;
import com.chess.ui.fragments.live.GameLiveFragmentTablet;
import com.chess.ui.fragments.live.LiveGameWaitFragment;
import com.chess.ui.fragments.settings.SettingsProfileFragment;
import com.chess.ui.fragments.tactics.GameTacticsFragment;
import com.chess.ui.fragments.upgrade.UpgradeDetailsFragment;
import com.chess.ui.fragments.videos.VideoDetailsFragment;
import com.chess.ui.fragments.videos.VideosFragmentTablet;
import com.chess.ui.fragments.welcome.WelcomeTabsFragment;
import com.chess.ui.fragments.welcome.WelcomeTabsFragmentTablet;
import com.chess.ui.fragments.welcome.WelcomeTourFragment;
import com.chess.ui.interfaces.ActiveFragmentInterface;
import com.chess.utilities.AppUtils;
import com.flurry.android.FlurryAgent;
import com.slidingmenu.lib.SlidingMenu;
import uk.co.senab.actionbarpulltorefresh.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.extras.AbcPullToRefreshAttacher;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.chess.db.DbScheme.uriArray;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 13:37
 */
public class MainFragmentFaceActivity extends LiveBaseActivity implements ActiveFragmentInterface {

	private static final String SHOW_ACTION_BAR = "show_actionbar_in_activity";

	private Fragment currentActiveFragment;
	private Hashtable<Integer, Integer> badgeItems;
	private SlidingMenu slidingMenu;
	private List<SlidingMenu.OnOpenedListener> openMenuListeners;
	private List<SlidingMenu.OnClosedListener> closeMenuListeners;
	private boolean showActionBar;
	private int customActionBarViewId;
	private IntentFilter notificationsUpdateFilter;
	private IntentFilter movesUpdateFilter;
	private NotificationsUpdateReceiver notificationsUpdateReceiver;
	private MovesUpdateReceiver movesUpdateReceiver;
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private Bitmap backgroundBitmap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSlidingActionBarEnabled(true);

		setContentView(R.layout.new_main_active_screen);
		customActionBarViewId = R.layout.new_custom_actionbar;

		openMenuListeners = new ArrayList<SlidingMenu.OnOpenedListener>();
		closeMenuListeners = new ArrayList<SlidingMenu.OnClosedListener>();

		slidingMenu = getSlidingMenu();
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		slidingMenu.setOnOpenedListener(onOpenMenuListener);
		slidingMenu.setOnCloseListener(onCloseMenuListener);

		badgeItems = new Hashtable<Integer, Integer>();

		notificationsUpdateFilter = new IntentFilter(IntentConstants.NOTIFICATIONS_UPDATE);
		movesUpdateFilter = new IntentFilter(IntentConstants.USER_MOVE_UPDATE);

		// restoring correct host
		///////////////////////////////////////////////////
		if (StaticData.USE_SWITCH_API) {
			RestHelper.resetInstance();
			RestHelper.HOST = getAppData().getApiRoute();
		}
		///////////////////////////////////////////////////

		// lock portrait mode for handsets and unlock for tablets
		if (isTablet) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		checkThemesToLoad();
		Intent intent = getIntent();

		if (savedInstanceState == null) {
			final String action = intent.getAction();

			if (Intent.ACTION_VIEW.equals(action)) {
				Uri data = intent.getData();
				BasePopupsFragment fragment =  new HomeTabsFragment();
				if (data != null) {
					List<String> segments = data.getPathSegments();
					if (segments != null && segments.size() > 0) {
						String segment = segments.get(0);
						if (segment.contains("tactics")) {
							fragment = new GameTacticsFragment();
						} else if (segment.contains("lessons") || segment.contains("chessmentor")) {
							fragment = new LessonsFragment();
						}
						showToast(segment);
					}
				}

				switchFragment(fragment);
				showActionBar = true;

			} else if (!TextUtils.isEmpty(getAppData().getUserToken())) { // if user have login token already
				// set the Above View
				switchFragment(new HomeTabsFragment());
//				openFragment(ArticleDetailsFragment.createInstance(13936));
//				openFragment(ArticleDetailsFragment.createInstance(13982));
				showActionBar = true;
			} else {
				if (isTablet) {
					switchFragment(new WelcomeTabsFragmentTablet());
				} else {
					switchFragment(new WelcomeTabsFragment());
				}
				showActionBar = false;
			}
		} else { // fragments state will be automatically restored
			showActionBar = savedInstanceState.getBoolean(SHOW_ACTION_BAR);
		}

		if (intent != null && intent.hasExtra(IntentConstants.USER_MOVE_UPDATE)) {
			long gameId = intent.getLongExtra(BaseGameItem.GAME_ID, 0);
			if (gameId != 0) {
				if (!isTablet) {
					openFragment(GameDailyFragment.createInstance(gameId, true));
				} else {
					openFragment(GameDailyFragmentTablet.createInstance(gameId, true));
				}
				setTouchModeToSlidingMenu(SlidingMenu.TOUCHMODE_NONE);
			}
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		getActionBarHelper().setCustomView(customActionBarViewId);
		super.onPostCreate(savedInstanceState);

		getActionBarHelper().showActionBar(showActionBar);

		updateNotificationsBadges();
		updateMainBackground();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		final String action = intent.getAction();

		if (Intent.ACTION_VIEW.equals(action)) {
			Uri data = intent.getData();
			if (data != null) {
				final List<String> segments = data.getPathSegments();
				if (segments != null && segments.size() > 1) {
					showToast(segments.get(0));
				}
			}
		}

		if (intent.hasExtra(IntentConstants.LIVE_CHESS)) {

			GameLiveFragment gameLiveFragment = getLiveFragment();
			if (gameLiveFragment != null) {
				openFragment(gameLiveFragment);
				return;
			}

			LiveGameWaitFragment fragmentByTag = (LiveGameWaitFragment) findFragmentByTag(LiveGameWaitFragment.class.getSimpleName());
			if (fragmentByTag == null) {
				fragmentByTag = LiveGameWaitFragment.createInstance(getAppData().getLiveGameConfig());
			}

			openFragment(fragmentByTag);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		DataHolder.getInstance().setMainActivityVisible(true);

		notificationsUpdateReceiver = new NotificationsUpdateReceiver();
		movesUpdateReceiver = new MovesUpdateReceiver();
		registerReceiver(notificationsUpdateReceiver, notificationsUpdateFilter);
		registerReceiver(movesUpdateReceiver, movesUpdateFilter);

		// apply sound theme
		String soundThemePath = getAppData().getThemeSoundsPath();
		if (!TextUtils.isEmpty(soundThemePath)) {
			SoundPlayer.setUseThemePack(true);
			SoundPlayer.setThemePath(soundThemePath);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		DataHolder.getInstance().setMainActivityVisible(false);

		unRegisterMyReceiver(notificationsUpdateReceiver);
		unRegisterMyReceiver(movesUpdateReceiver);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(SHOW_ACTION_BAR, showActionBar);
	}

	@Override
	public void updateActionBarIcons() {
		if (!HONEYCOMB_PLUS_API) {
			adjustActionBar();
		} else {
			invalidateOptionsMenu();
		}
	}

	@Override
	public void updateActionBarBackImage() {
		getActionBarHelper().updateActionBarBackground();
	}

	@Override
	public void setTitle(CharSequence title) {
		getActionBarHelper().setTitle(title);
	}

	@Override
	public void updateTitle(CharSequence title) {
		if (!HONEYCOMB_PLUS_API) { // set title before custom view for pre-HC
			getActionBarHelper().setTitle(title);
		}
		getActionBarHelper().setCustomView(R.layout.new_custom_actionbar);
		getActionBarHelper().setTitle(title);

		if (!HONEYCOMB_PLUS_API) {
			for (Map.Entry<Integer, Integer> entry : badgeItems.entrySet()) {
				getActionBarHelper().setBadgeValueForId(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public void setTitlePadding(int padding) {
		getActionBarHelper().setTitlePadding(padding);
	}

	@Override
	public void showActionBar(boolean show) {
		showActionBar = show;
		getActionBarHelper().showActionBar(show);

		if (mPullToRefreshAttacher != null && !show) {
			mPullToRefreshAttacher.removePaddingForHeader();
		}
	}

	@Override
	public void setMainBackground(int drawableThemeId) {
		getWindow().setBackgroundDrawableResource(drawableThemeId);
	}

	private void setMainBackground(String drawablePath) {
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

		bitmapOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(drawablePath, bitmapOptions);

		Display display = getWindowManager().getDefaultDisplay();

		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getMetrics(displayMetrics);

		int displayHeight = displayMetrics.heightPixels;
		int displayWidth = displayMetrics.widthPixels;

		displayWidth -= AppUtils.getStatusBarHeight(this);
		bitmapOptions.inSampleSize = AppUtils.calculateInSampleSize(bitmapOptions, displayWidth, displayHeight);

		// Decode bitmap with inSampleSize set
		bitmapOptions.inJustDecodeBounds = false;
		if (backgroundBitmap != null) {
			backgroundBitmap.recycle();
			backgroundBitmap = null;
			Runtime.getRuntime().gc();
		}

		try {
			backgroundBitmap = BitmapFactory.decodeFile(drawablePath, bitmapOptions);
		} catch (OutOfMemoryError ignore) {
			showToast("Oops! Out of memory :(");
		}

		if (backgroundBitmap != null) {
			BitmapDrawable drawable = new BitmapDrawable(getResources(), backgroundBitmap);
			getWindow().setBackgroundDrawable(drawable);
		} else { // If user removed SD card or clear folder
			getWindow().setBackgroundDrawableResource(getAppData().getThemeBackId());

			// clear DB entity to refill it
			getContentResolver().delete(uriArray[DbScheme.Tables.THEMES_LOAD_STATE.ordinal()], null, null);

			AppData appData = getAppData();
			// clear themed settings
			appData.resetThemeToDefault();

			appData.setThemeName(AppConstants.DEFAULT_THEME_NAME);
			appData.setThemeBackgroundName(AppConstants.DEFAULT_THEME_NAME);
			updateActionBarBackImage();
		}
	}

	@Override
	public void showActionMenu(int menuId, boolean show) {
		enableActionMenu(menuId, show);
	}

	@Override
	public void setCustomActionBarViewId(int viewId) {
		customActionBarViewId = viewId;
		getActionBarHelper().setCustomView(customActionBarViewId);
	}

	private SlidingMenu.OnOpenedListener onOpenMenuListener = new SlidingMenu.OnOpenedListener() {
		@Override
		public void onOpened() { // Don't remove reuse later
			if (slidingMenu.isSecondaryMenuShowing()) {
				for (SlidingMenu.OnOpenedListener openedListener : openMenuListeners) { // Inform listeners inside fragments
					openedListener.onOpenedRight();
				}
			} else {
				for (Fragment fragment : getSupportFragmentManager().getFragments()) {
					if (fragment != null && fragment.isVisible() && fragment instanceof NavigationMenuFragment) {
						((NavigationMenuFragment) fragment).onOpened();
						break;
					}
				}
			}
		}

		@Override
		public void onOpenedRight() {
		}
	};

	private SlidingMenu.OnCloseListener onCloseMenuListener = new SlidingMenu.OnCloseListener() {
		@Override
		public void onClose() {
			hideKeyBoard();
			for (SlidingMenu.OnClosedListener closeMenuListener : closeMenuListeners) {
				closeMenuListener.onClosed();
			}
		}
	};

	@Override
	public void addOnOpenMenuListener(SlidingMenu.OnOpenedListener listener) {
		if (openMenuListeners != null)
			openMenuListeners.add(listener);
	}

	@Override
	public void removeOnOpenMenuListener(SlidingMenu.OnOpenedListener listener) {
		if (openMenuListeners != null)
			openMenuListeners.remove(listener);
	}

	@Override
	public void addOnCloseMenuListener(SlidingMenu.OnClosedListener listener) {
		if (closeMenuListeners != null)
			closeMenuListeners.add(listener);
	}

	@Override
	public void removeOnCloseMenuListener(SlidingMenu.OnClosedListener listener) {
		if (closeMenuListeners != null)
			closeMenuListeners.remove(listener);
	}

	@Override
	public void registerGcm() {
		registerGcmService();
	}

	@Override
	public void unRegisterGcm() {
		unRegisterGcmService();
	}

	@Override
	public void changeRightFragment(CommonLogicFragment fragment) {
		// set right menu. Left Menu is already set in BaseActivity
		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT_RIGHT);
		sm.setBehindRightOffsetRes(R.dimen.slidingmenu_offset_right);
		sm.setSecondaryMenu(R.layout.slide_menu_right_frame);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.menu_frame_right, fragment)
				.commit();
		sm.setSecondaryShadowDrawable(R.drawable.defaultshadow_right);
		sm.setShadowDrawable(R.drawable.defaultshadow);
	}

	@Override
	public void changeLeftFragment(CommonLogicFragment fragment) {
		// change left menu fragment
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		leftMenuFragment = fragment;
		transaction.replace(R.id.menu_frame_left, leftMenuFragment);
		transaction.commit();
	}

	@Override
	public void setTouchModeToSlidingMenu(int touchMode) {
		SlidingMenu sm = getSlidingMenu();
		sm.setTouchModeAbove(touchMode);
	}

	@Override
	public void openFragment(BasePopupsFragment fragment) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

		String simpleName = fragment.getClass().getSimpleName();
		transaction.replace(R.id.content_frame, fragment, simpleName);
		transaction.addToBackStack(simpleName);
		transaction.commitAllowingStateLoss();

		FlurryAgent.logEvent(FlurryData.OPEN_FRAME + simpleName);
	}

	@Override
	public void openFragment(BasePopupsFragment fragment, int code) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

		String simpleName = fragment.getClass().getSimpleName();
		transaction.replace(R.id.content_frame, fragment, simpleName);
		transaction.addToBackStack(simpleName);
		transaction.commit();
	}

	@Override
	public void switchFragment(BasePopupsFragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

		ft.replace(R.id.content_frame, fragment, fragment.getClass().getSimpleName());
		ft.commit();
	}

	@Override
	public void switchFragment(BasePopupsFragment fragment, int code) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

		ft.replace(R.id.content_frame, fragment, fragment.getClass().getSimpleName());
		ft.commit();
	}

	@Override
	public void toggleLeftMenu() {
		toggleMenu(SlidingMenu.LEFT);
	}

	@Override
	public void toggleRightMenu() {
		toggleMenu(SlidingMenu.RIGHT);
	}

	private void toggleMenu(int code) {
		switch (code) {
			case SlidingMenu.LEFT:
				if (getSlidingMenu().isMenuShowing()) {
					getSlidingMenu().toggle();
				} else {
					getSlidingMenu().showMenu();
				}
				break;
			case SlidingMenu.RIGHT:
				boolean visible = getSlidingMenu().isMenuShowing();
				if (visible) {
					getSlidingMenu().toggle();
				} else {
					getSlidingMenu().showSecondaryMenu();
				}
				break;
		}
	}

	private class MovesUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateNotificationsBadges();
		}
	}

	@Override
	public void updateMainBackground() {
		String themeBackPath;
		if (inPortrait()) {
			themeBackPath = getAppData().getThemeBackPathPort();
		} else {
			themeBackPath = getAppData().getThemeBackPathLand();
		}

		if (!TextUtils.isEmpty(themeBackPath)) {
			setMainBackground(themeBackPath);
		} else {
			try {
				getWindow().setBackgroundDrawableResource(getAppData().getThemeBackId());
			} catch (OutOfMemoryError ignore) {
				return;
			}
		}

		updateActionBarBackImage();
		for (Fragment fragment : getSupportFragmentManager().getFragments()) {
			if (fragment != null && fragment instanceof CommonLogicFragment) {
				((CommonLogicFragment)fragment).updateFontColors();
			}
		}

		// force update all views with fonts
		findViewById(R.id.content_frame).invalidate();
		sendBroadcast(new Intent(IntentConstants.BACKGROUND_LOADED));
	}

	private class NotificationsUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateNotificationsBadges();

			for (Fragment fragment : getSupportFragmentManager().getFragments()) {
				if (fragment != null && fragment.isVisible() && fragment instanceof NotificationsRightFragment) {
					((NotificationsRightFragment) fragment).onOpenedRight();
					break;
				}
			}
		}
	}

	@Override
	public void updateNotificationsBadges() {
		int playMovesCnt = DbDataManager.getPlayMoveNotificationsCnt(getContentResolver(), getMeUsername());
		int notificationsCnt = DbDataManager.getUnreadNotificationsCnt(getContentResolver(), getMeUsername());
		setBadgeValueForId(R.id.menu_notifications, notificationsCnt);
		setBadgeValueForId(R.id.menu_games, playMovesCnt);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if ((requestCode & 0xFFFF) == UpgradeDetailsFragment.RC_REQUEST) {
			FragmentManager fragmentManager = getSupportFragmentManager(); // the only one way to call it after startIntentSenderForResult
			Fragment fragment = fragmentManager.findFragmentByTag(UpgradeDetailsFragment.class.getSimpleName());
			if (fragment != null) {
				fragment.onActivityResult(requestCode, resultCode, data);
			}
		} else if (isTablet && (requestCode & 0xFFFF) == VideoDetailsFragment.WATCH_VIDEO_REQUEST) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			Fragment fragment = fragmentManager.findFragmentByTag(VideosFragmentTablet.class.getSimpleName());
			if (fragment != null) {
				fragment.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	@Override
	public void onBackPressed() {
		// there is no way to handle backPressed from fragment, so do this non OOP solution. Google, why are you so evil
		WelcomeTourFragment welcomeTourFragment = (WelcomeTourFragment) getSupportFragmentManager().findFragmentByTag(WelcomeTourFragment.class.getSimpleName());
		int orientation = getResources().getConfiguration().orientation;
		if (welcomeTourFragment != null && orientation == Configuration.ORIENTATION_LANDSCAPE) {
			if (welcomeTourFragment.hideYoutubeFullScreen()) {
				return;
			}
		} else if (welcomeTourFragment != null) { // if swipe to registration screen, handle back button to swipe back
			if (welcomeTourFragment.swipeBackFromSignUp()) {
				return;
			}
		}

		// check if child fragment manager want to consume event an pop something up
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		if (fragments != null && fragments.size() > 0) {
			int last = fragments.size() - 1;
			Fragment lastFragment = fragments.get(last);
			if (lastFragment == null) { // there is a bug, that size tell for one more
			    last--;
				lastFragment = fragments.get(last);
			}
			if (lastFragment instanceof ImageCache.RetainFragment) {
				last--;
				lastFragment = fragments.get(last);
			}
			if (lastFragment instanceof CommonLogicFragment) { // check if fragment want to consume back button event
				if (((CommonLogicFragment)lastFragment).showPreviousFragment()) {
					return;
				}
			}
		}

		showPreviousFragment();
	}

	@Override
	public void showPreviousFragment() {
		if (getSupportFragmentManager() == null) {
			return;
		}
		boolean fragmentsLeft = getSupportFragmentManager().popBackStackImmediate();
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		for (int i = fragments.size() - 1; i > 0; i--) {
			Fragment fragment = fragments.get(i);
			if (fragment != null) {
				currentActiveFragment = fragment;
				break;
			}
		}
		if (!fragmentsLeft) {
			super.onBackPressed();
		}
	}

	@Override
	public void clearFragmentStack() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		int count = fragmentManager.getBackStackEntryCount();
		if (count > 0) {
			int firstFragmentId = fragmentManager.getBackStackEntryAt(0).getId();
			fragmentManager.popBackStack(firstFragmentId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			SettingsProfileFragment fragmentByTag = (SettingsProfileFragment) getSupportFragmentManager()
					.findFragmentByTag(SettingsProfileFragment.class.getSimpleName());
			if (fragmentByTag != null && fragmentByTag.isVisible()) {
				fragmentByTag.discardChanges();
				return super.onKeyUp(keyCode, event);
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	private void setBadgeValueForId(int menuId, int value) {
		badgeItems.put(menuId, value);
		getActionBarHelper().setBadgeValueForId(menuId, value);
	}

	@Override
	public CoreActivityActionBar getActionBarActivity() {
		return getInstance();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		boolean displayMenu = super.onCreateOptionsMenu(menu);
		for (Map.Entry<Integer, Integer> entry : badgeItems.entrySet()) {
			getActionBarHelper().setBadgeValueForId(entry.getKey(), entry.getValue(), menu);
		}

		return displayMenu;
	}



	public void startActivityFromFragmentForResult(Intent intent, int requestCode) {
		if (currentActiveFragment != null) {
			startActivityFromFragment(currentActiveFragment, intent, requestCode);
		}
	}

	@Override
	public AppData getMeAppData() {
		return getAppData();
	}

	@Override
	public String getMeUsername() {
		return getCurrentUsername();
	}

	@Override
	public String getMeUserToken() {
		return getCurrentUserToken();
	}

	@Override
	protected void onSearchQuery(String query) {
		if (currentActiveFragment instanceof CommonLogicFragment) {
			((CommonLogicFragment)currentActiveFragment).onSearchQuery(query);
		}
	}

	@Override
	protected void onSearchAutoCompleteQuery(String query) {
		if (currentActiveFragment instanceof CommonLogicFragment) {
			((CommonLogicFragment)currentActiveFragment).onSearchAutoCompleteQuery(query);
		}
	}

	@Override
	public void setPullToRefreshView(View view, PullToRefreshAttacher.OnRefreshListener refreshListener) {
		if (mPullToRefreshAttacher != null) {
			mPullToRefreshAttacher.clearRefreshableViews();
		} else {

			/**
			 * Here we create a PullToRefreshAttacher manually without an Options instance.
			 * PullToRefreshAttacher will manually create one using default values.
			 */
			if (HONEYCOMB_PLUS_API){
				mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
			} else{
				mPullToRefreshAttacher = AbcPullToRefreshAttacher.get(this);
			}
		}

		// Set the Refreshable View to be the ListView and the refresh listener to be this.
		mPullToRefreshAttacher.addRefreshableView(view, refreshListener);
	}

	@Override
	public PullToRefreshAttacher getPullToRefreshAttacher() {
		return mPullToRefreshAttacher;
	}

	private void checkThemesToLoad() {
		boolean needToLoadThemes = DbDataManager.haveSavedThemesToLoad(this);
		if (needToLoadThemes) {
			bindService(new Intent(this, GetAndSaveTheme.class), new LoadServiceConnectionListener(),
					Activity.BIND_AUTO_CREATE);
		}
	}

	private class LoadServiceConnectionListener implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			int screenWidth = getResources().getDisplayMetrics().widthPixels;
			int screenHeight = getResources().getDisplayMetrics().heightPixels;

			GetAndSaveTheme.ServiceBinder serviceBinder = (GetAndSaveTheme.ServiceBinder) iBinder;
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.THEMES_LOAD_STATE));
			if (cursor != null && cursor.moveToFirst()) {
				do {
					int id = DbDataManager.getInt(cursor, DbScheme.V_ID);
					Cursor themeCursor = DbDataManager.query(getContentResolver(), DbHelper.getThemeById(id));

					if (themeCursor != null && themeCursor.moveToFirst()) {
						do {
							ThemeItem.Data themeItem = DbDataManager.getThemeItemFromCursor(themeCursor);
							serviceBinder.getService().loadTheme(themeItem, screenWidth, screenHeight);
						} while (themeCursor.moveToNext());
					}
				} while (cursor.moveToNext());
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
		}
	}

}