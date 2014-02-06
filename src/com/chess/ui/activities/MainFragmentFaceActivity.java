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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.*;
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
import com.chess.model.PopupItem;
import com.chess.statics.*;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.LeftNavigationFragment;
import com.chess.ui.fragments.RightNotificationsFragment;
import com.chess.ui.fragments.daily.GameDailyFragment;
import com.chess.ui.fragments.daily.GameDailyFragmentTablet;
import com.chess.ui.fragments.home.HomeTabsFragment;
import com.chess.ui.fragments.lessons.LessonsFragment;
import com.chess.ui.fragments.live.GameLiveFragment;
import com.chess.ui.fragments.live.LiveHomeFragment;
import com.chess.ui.fragments.live.LiveHomeFragmentTablet;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.settings.SettingsFragmentTablet;
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
import com.google.analytics.tracking.android.EasyTracker;
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
	private static final long CHECK_THEMES_TO_LOAD_DELAY = 5 * 1000;
	private static final String ASK_FOR_REVIEW_TAG = "ask for review popup";

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
	private PullToRefreshAttacher pullToRefreshAttacher;
	private Bitmap backgroundBitmap;
	private PopupCustomViewFragment reviewPopupFragment;

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
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		checkThemesToLoad();
		Intent intent = getIntent();

		if (savedInstanceState == null) {
			final String action = intent.getAction();

			if (Intent.ACTION_VIEW.equals(action)) {
				Uri data = intent.getData();
				BasePopupsFragment fragment = new HomeTabsFragment();
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

				// force create pullToRefreshAttacher bcz in some cases we skip home screen and it's not created, and we use chess spinner
				createPullToRefreshAttacher();

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

		handleOpenDailyGames(intent);
	}

	private void createPullToRefreshAttacher() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			pullToRefreshAttacher = PullToRefreshAttacher.get(this);
		} else {
			pullToRefreshAttacher = AbcPullToRefreshAttacher.get(this);
		}
	}

	private void handleOpenDailyGames(Intent intent) {
		if (intent != null && intent.hasExtra(IntentConstants.USER_MOVE_UPDATE)) {
			if (intent.hasExtra(BaseGameItem.GAME_ID)) {
				long gameId = intent.getLongExtra(BaseGameItem.GAME_ID, 0);
				if (gameId != 0) {
					if (!isTablet) {
						openFragment(GameDailyFragment.createInstance(gameId, true));
					} else {
						openFragment(GameDailyFragmentTablet.createInstance(gameId, true));
					}
					setTouchModeToSlidingMenu(SlidingMenu.TOUCHMODE_NONE);
				}
			} else {
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						clearFragmentStack();
						switchFragment(new HomeTabsFragment());
						setTouchModeToSlidingMenu(SlidingMenu.TOUCHMODE_FULLSCREEN);
					}
				}, CommonLogicFragment.VIEW_UPDATE_DELAY);
			}
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		getActionBarHelper().setCustomView(customActionBarViewId);
		super.onPostCreate(savedInstanceState);

		getActionBarHelper().showActionBar(showActionBar);

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
			GameLiveFragment gameLiveFragment = getGameLiveFragment();
			if (gameLiveFragment != null) {
				openFragment(gameLiveFragment);
				return;
			}

			LiveHomeFragment fragmentByTag = getLiveHomeFragment();
			if (fragmentByTag == null) {
				if (isTablet) {
					fragmentByTag = new LiveHomeFragmentTablet();
				} else {
					fragmentByTag = new LiveHomeFragment();
				}
			}

//			openFragment(fragmentByTag, true);
			openFragment(fragmentByTag);
		}

		handleOpenDailyGames(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();

		updateNotificationsBadges();

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

		checkAndShowAppReviewPopup();
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

		if (pullToRefreshAttacher != null && !show) {
			pullToRefreshAttacher.removePaddingForHeader();
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

		displayHeight -= AppUtils.getStatusBarHeight(this);
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
			drawable.setBounds(0, 0, backgroundBitmap.getWidth(), backgroundBitmap.getHeight());
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
					if (fragment != null && fragment.isVisible() && fragment instanceof LeftNavigationFragment) {
						((LeftNavigationFragment) fragment).onOpened();
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
	public void changeRightFragment(CommonLogicFragment fragment) {
		// set right menu. Left Menu is already set in BaseActivity
		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT_RIGHT);
		sm.setBehindRightOffsetRes(R.dimen.slidingmenu_offset_right);
		sm.setSecondaryMenu(R.layout.slide_menu_right_frame);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.menu_frame_right, fragment)
				.commitAllowingStateLoss();
		sm.setSecondaryShadowDrawable(R.drawable.defaultshadow_right);
		sm.setShadowDrawable(R.drawable.defaultshadow);
	}

	@Override
	public void changeLeftFragment(CommonLogicFragment fragment) {
		// change left menu fragment
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		leftMenuFragment = fragment;
		transaction.replace(R.id.menu_frame_left, leftMenuFragment);
		transaction.commitAllowingStateLoss();
	}

	@Override
	public void setTouchModeToSlidingMenu(int touchMode) {
		SlidingMenu sm = getSlidingMenu();
		sm.setTouchModeAbove(touchMode);
	}

	@Override
	public Fragment getCurrentActiveFragment() {
		return currentActiveFragment;
	}

	//	@Override
//	public void openFragment(BasePopupsFragment fragment, boolean rewind) {
//
//		if (rewind) {
//
//			// rewind to latest backstack position of fragment
//
//			FragmentManager fragmentManager = getSupportFragmentManager();
//			String simpleName = fragment.getClass().getSimpleName();
//
//			int count = fragmentManager.getBackStackEntryCount();
//
//			for (int i = count - 1; i >= 0; i--) {
//				if (fragmentManager.getBackStackEntryAt(i).getName().equals(simpleName)) {
//					for (int j = 0; j < count - i; j++) {
//						fragmentManager.popBackStackImmediate();
//					}
//					break;
//				}
//			}
//
//		}
//
//		openFragment(fragment);
//	}

	@Override
	public void openFragment(BasePopupsFragment fragment) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

		String simpleName = fragment.getClass().getSimpleName();
		transaction.replace(R.id.content_frame, fragment, simpleName);
		transaction.addToBackStack(simpleName);
		transaction.commitAllowingStateLoss();

//		FlurryAgent.logEvent(FlurryData.OPEN_FRAME + simpleName);

		checkNoLiveFragment(simpleName);
	}

	private void checkNoLiveFragment(String fragmentName) {
		if (!isLiveFragment(fragmentName)) {
			if (isLCSBound) {
				getDataHolder().setLiveChessMode(false);
				liveHelper.startIdleTimeOutCounter();
				isLCSBound = false;
			}
		}
	}

	@Override
	public void switchFragment(BasePopupsFragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

		String simpleName = fragment.getClass().getSimpleName();
		ft.replace(R.id.content_frame, fragment, simpleName);
		ft.commitAllowingStateLoss();

		checkNoLiveFragment(simpleName);
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
				((CommonLogicFragment) fragment).updateFontColors();
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
				if (fragment != null && fragment.isVisible() && fragment instanceof RightNotificationsFragment) {
					((RightNotificationsFragment) fragment).onOpenedRight();
					break;
				}
			}
		}
	}

	@Override
	public void updateNotificationsBadges() {
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getDailyCurrentMyListGamesCnt(getMeUsername()));
		int playMovesCnt = 0;
		if (cursor != null) {
			playMovesCnt = cursor.getCount();
		}

//		int playMovesCnt = DbDataManager.getPlayMoveNotificationsCnt(getContentResolver(), getMeUsername());
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
		} else if (isTablet && (requestCode & 0xFFFF) == VideoDetailsFragment.WATCH_VIDEO_REQUEST) { // TODO improve that non universal logic
			FragmentManager fragmentManager = getSupportFragmentManager();
			Fragment fragment = fragmentManager.findFragmentByTag(VideosFragmentTablet.class.getSimpleName());
			if (fragment != null) {
				fragment.onActivityResult(requestCode, resultCode, data);
			}
		} else if (isTablet && (requestCode & 0xFFFF) == SettingsProfileFragment.REQ_CODE_PICK_IMAGE) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			Fragment fragment = fragmentManager.findFragmentByTag(SettingsFragmentTablet.class.getSimpleName());
			if (fragment != null) {
				fragment.onActivityResult(requestCode & 0xffff, resultCode, data);
			}
		} else if (isTablet && (requestCode & 0xFFFF) == SettingsProfileFragment.REQ_CODE_TAKE_IMAGE) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			Fragment fragment = fragmentManager.findFragmentByTag(SettingsFragmentTablet.class.getSimpleName());
			if (fragment != null) {
				fragment.onActivityResult(requestCode & 0xffff, resultCode, data);
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
				if (((CommonLogicFragment) lastFragment).showPreviousFragment()) {
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
			fragmentManager.popBackStackImmediate(firstFragmentId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}

	@Override
	public void onClick(View view) {

		if (view.getId() == R.id.positiveBtn) { // Yes (leave us feedback)
			getAppData().setUserAskedForFeedback(true);

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(AppUtils.getGooglePlayLinkForApp(this)));
			startActivity(intent);

			if (reviewPopupFragment != null) {
				reviewPopupFragment.dismiss();
			}
		} else if (view.getId() == R.id.negativeBtn) { // No (send us suggestions)
			getAppData().setUserAskedForFeedback(true);

			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.setType(AppConstants.MIME_TYPE_MESSAGE_RFC822);
			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{AppConstants.FEEDBACK_EMAIL});
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, AppConstants.FEEDBACK_SUBJECT);
			emailIntent.putExtra(Intent.EXTRA_TEXT, feedbackBodyCompose(getMeUsername()));
			startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail)));

			if (reviewPopupFragment != null) {
				reviewPopupFragment.dismiss();
			}

		} else if (view.getId() == R.id.ignoreBtn) {
			if (reviewPopupFragment != null) {
				reviewPopupFragment.dismiss();
			}
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
			((CommonLogicFragment) currentActiveFragment).onSearchQuery(query);
		}
	}

	@Override
	protected void onSearchAutoCompleteQuery(String query) {
		if (currentActiveFragment instanceof CommonLogicFragment) {
			((CommonLogicFragment) currentActiveFragment).onSearchAutoCompleteQuery(query);
		}
	}

	@Override
	public void setPullToRefreshView(View view, PullToRefreshAttacher.OnRefreshListener refreshListener) {
		if (pullToRefreshAttacher != null) {
			pullToRefreshAttacher.clearRefreshableViews();
		} else {

			/**
			 * Here we create a PullToRefreshAttacher manually without an Options instance.
			 * PullToRefreshAttacher will manually create one using default values.
			 */
			createPullToRefreshAttacher();
		}

		// Set the Refreshable View to be the ListView and the refresh listener to be this.
		pullToRefreshAttacher.addRefreshableView(view, refreshListener);
	}

	@Override
	public PullToRefreshAttacher getPullToRefreshAttacher() {
		if (pullToRefreshAttacher == null) {
			createPullToRefreshAttacher();
		}
		return pullToRefreshAttacher;
	}

	private void checkThemesToLoad() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (getContext() == null) {
					return;
				}

				boolean needToLoadThemes = DbDataManager.haveSavedThemesToLoad(getContext());
				if (needToLoadThemes) {
					bindService(new Intent(getContext(), GetAndSaveTheme.class), new LoadServiceConnectionListener(),
							Activity.BIND_AUTO_CREATE);
				}
			}
		}, CHECK_THEMES_TO_LOAD_DELAY);
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

	@Override
	public void updateLocale() {
		setLocale();
		restartActivity();
	}

	private void checkAndShowAppReviewPopup() {
		// check if it's 7th day after install, then ask for feedback
		if (!getAppData().isUserAskedForFeedback() && !getMeUsername().equals(Symbol.EMPTY)
				&& !getMeUsername().equals(AppConstants.GUEST_NAME)) {

			// first we check if 7 days has passed
			long timeNow = System.currentTimeMillis();
			long firstTimeStart = getAppData().getFirstTimeStart();
			boolean askForReviewNow = timeNow - firstTimeStart > AppConstants.TIME_FOR_APP_REVIEW;
			if (!askForReviewNow) {
				return;
			}

			// if 2 weeks passed, then ask again
			long lastTimeAskedForFeedback = getAppData().getLastTimeAskedForFeedback();
			boolean reAskForReviewNow = timeNow - lastTimeAskedForFeedback > AppConstants.TIME_FOR_APP_REVIEW * 2;// two times longer
			if (!reAskForReviewNow) {
				return;
			}

			// avoid multiple popup appears
			getAppData().setLastTimeAskedForFeedback(System.currentTimeMillis());

			View layout = LayoutInflater.from(this).inflate(R.layout.new_review_app_popup, null, false);
			layout.findViewById(R.id.positiveBtn).setOnClickListener(this);
			layout.findViewById(R.id.negativeBtn).setOnClickListener(this);
			layout.findViewById(R.id.ignoreBtn).setOnClickListener(this);

			PopupItem popupItem = new PopupItem();
			popupItem.setCustomView(layout);

			reviewPopupFragment = PopupCustomViewFragment.createInstance(popupItem);
			reviewPopupFragment.show(getSupportFragmentManager(), ASK_FOR_REVIEW_TAG);
			reviewPopupFragment.setCancelable(false);
			reviewPopupFragment.setCancelableOnTouch(false);
		}
	}

	protected String feedbackBodyCompose(String username) {
		AppUtils.DeviceInfo deviceInfo = new AppUtils.DeviceInfo().getDeviceInfo(this);

		return getResources().getString(R.string.feedback_mail_body) + ": \n\n"
				+ AppConstants.OS_VERSION + AppConstants.SDK_API + deviceInfo.SDK_API + Symbol.NEW_STR
				+ AppConstants.DEVICE + deviceInfo.MODEL + Symbol.NEW_STR
				+ AppConstants.APP_VERSION + deviceInfo.APP_VERSION_CODE + Symbol.SLASH + deviceInfo.APP_VERSION_NAME + Symbol.NEW_STR
				+ AppConstants.USERNAME_ + username;
	}

	@Override
	public EasyTracker getGATracker() {
		return provideGATracker();
	}
}