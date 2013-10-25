package com.chess.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.db.DbDataManager;
import com.chess.model.DataHolder;
import com.chess.statics.AppData;
import com.chess.statics.FlurryData;
import com.chess.statics.IntentConstants;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.NotificationsRightFragment;
import com.chess.ui.fragments.home.HomeTabsFragment;
import com.chess.ui.fragments.lessons.LessonsFragment;
import com.chess.ui.fragments.live.GameLiveFragment;
import com.chess.ui.fragments.live.LiveGameWaitFragment;
import com.chess.ui.fragments.settings.SettingsProfileFragment;
import com.chess.ui.fragments.tactics.GameTacticsFragment;
import com.chess.ui.fragments.upgrade.UpgradeDetailsFragment;
import com.chess.ui.fragments.welcome.WelcomeFragment;
import com.chess.ui.fragments.welcome.WelcomeTabsFragment;
import com.chess.ui.interfaces.ActiveFragmentInterface;
import com.flurry.android.FlurryAgent;
import com.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSlidingActionBarEnabled(true);

		setContentView(R.layout.new_main_active_screen);
		customActionBarViewId = R.layout.new_custom_actionbar;

		openMenuListeners = new ArrayList<SlidingMenu.OnOpenedListener>();
		closeMenuListeners = new ArrayList<SlidingMenu.OnClosedListener>();

		if (savedInstanceState == null) {
			final String action = getIntent().getAction();

			if (Intent.ACTION_VIEW.equals(action)) {
				Uri data = getIntent().getData();
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
//				switchFragment(new SettingsThemeCustomizeFragment());
				showActionBar = true;
			} else {
				switchFragment(new WelcomeTabsFragment());
				showActionBar = false;
			}
		} else { // fragments state will be automatically restored
			showActionBar = savedInstanceState.getBoolean(SHOW_ACTION_BAR);
		}

		slidingMenu = getSlidingMenu();
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		slidingMenu.setOnOpenedListener(onOpenMenuListener);
		slidingMenu.setOnCloseListener(onCloseMenuListener);

		badgeItems = new Hashtable<Integer, Integer>();

		String themeBackPath = getAppData().getThemeBackPath();
		if (!TextUtils.isEmpty(themeBackPath)) {
			setMainBackground(themeBackPath);
		} else {
			getWindow().setBackgroundDrawableResource(getAppData().getThemeBackId());
		}

		notificationsUpdateFilter = new IntentFilter(IntentConstants.NOTIFICATIONS_UPDATE);
		movesUpdateFilter = new IntentFilter(IntentConstants.USER_MOVE_UPDATE);

		// TODO remove after test!!!
		// restoring correct host
		RestHelper.resetInstance();
		RestHelper.HOST = getAppData().getApiRoute();

		// apply sound theme
		String soundThemePath = getAppData().getThemeSoundPath();
		if (!TextUtils.isEmpty(soundThemePath)) {
			SoundPlayer.setUseThemePack(true);
			SoundPlayer.setThemePath(soundThemePath);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		getActionBarHelper().setCustomView(customActionBarViewId);
		super.onPostCreate(savedInstanceState);

		getActionBarHelper().showActionBar(showActionBar);

		updateNotificationsBadges();
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

			GameLiveFragment gameLiveFragment = (GameLiveFragment) findFragmentByTag(GameLiveFragment.class.getSimpleName());
			if (gameLiveFragment != null) {
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				currentActiveFragment = gameLiveFragment;

				ft.replace(R.id.content_frame, gameLiveFragment, gameLiveFragment.getClass().getSimpleName());
				ft.commitAllowingStateLoss();
				return;
			}
			LiveGameWaitFragment fragmentByTag = (LiveGameWaitFragment) findFragmentByTag(LiveGameWaitFragment.class.getSimpleName());
			if (fragmentByTag == null) {
				fragmentByTag = LiveGameWaitFragment.createInstance(getAppData().getLiveGameConfig());
			}

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			currentActiveFragment = fragmentByTag;

			ft.replace(R.id.content_frame, fragmentByTag, fragmentByTag.getClass().getSimpleName());
			ft.commitAllowingStateLoss();
		}
		// TODO open from notification
	}


	@Override
	protected void onResume() {
		super.onResume();

		DataHolder.getInstance().setMainActivityVisible(true);

		notificationsUpdateReceiver = new NotificationsUpdateReceiver();
		movesUpdateReceiver = new MovesUpdateReceiver();
		registerReceiver(notificationsUpdateReceiver, notificationsUpdateFilter);
		registerReceiver(movesUpdateReceiver, movesUpdateFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();

		DataHolder.getInstance().setMainActivityVisible(false);

		unRegisterMyReceiver(notificationsUpdateReceiver);
		unRegisterMyReceiver(movesUpdateReceiver);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
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
	}

	@Override
	public void setMainBackground(int drawableThemeId) {
		getWindow().setBackgroundDrawableResource(drawableThemeId);
	}

	@Override
	public void setMainBackground(String drawablePath) {
		getAppData().setThemeBackPath(drawablePath);
		Bitmap bitmap = BitmapFactory.decodeFile(drawablePath);
		if (bitmap != null) {
			BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
			getWindow().setBackgroundDrawable(drawable);
		} else { // If user removed SD card or clear folder
			getWindow().setBackgroundDrawableResource(getAppData().getThemeBackId());
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

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private SlidingMenu.OnOpenedListener onOpenMenuListener = new SlidingMenu.OnOpenedListener() {
		@Override
		public void onOpened() { // Don't remove reuse later
			if (slidingMenu.isSecondaryMenuShowing()) {
				for (SlidingMenu.OnOpenedListener openedListener : openMenuListeners) { // Inform listeners inside fragments
					openedListener.onOpenedRight();
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
		sm.setSecondaryShadowDrawable(R.drawable.defaultshadowright);
		sm.setShadowDrawable(R.drawable.defaultshadow);
//		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}

	@Override
	public void changeLeftFragment(CommonLogicFragment fragment) {
		// change left menu fragment
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		leftMenuFragment = fragment;
		ft.replace(R.id.menu_frame_left, leftMenuFragment);
		ft.commit();
	}

	@Override
	public void setTouchModeToSlidingMenu(int touchMode) {
		SlidingMenu sm = getSlidingMenu();
		sm.setTouchModeAbove(touchMode);
	}

	@Override
	public void openFragment(BasePopupsFragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

		ft.replace(R.id.content_frame, fragment, fragment.getClass().getSimpleName());
		ft.addToBackStack(fragment.getClass().getSimpleName());
		ft.commit();

		FlurryAgent.logEvent(FlurryData.OPEN_FRAME + fragment.getClass().getSimpleName());
	}

	@Override
	public void openFragment(BasePopupsFragment fragment, int code) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

		ft.replace(R.id.content_frame, fragment, fragment.getClass().getSimpleName());
		ft.addToBackStack(fragment.getClass().getSimpleName());
		ft.commit();
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
			Log.d("TEST", " onReceive = " + intent);
			updateNotificationsBadges();
		}
	}

	private class NotificationsUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("TEST", " onReceive = " + intent);

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
		Log.d("TEST", " total unread notifications = " + notificationsCnt);
		setBadgeValueForId(R.id.menu_notifications, notificationsCnt);
		setBadgeValueForId(R.id.menu_games, playMovesCnt);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == UpgradeDetailsFragment.RC_REQUEST) {
			FragmentManager fragmentManager = getSupportFragmentManager(); // the only one way to call it after startIntentSenderForResult
			Fragment fragment = fragmentManager.findFragmentByTag(UpgradeDetailsFragment.class.getSimpleName());
			if (fragment != null) {
				fragment.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	@Override
	public void onBackPressed() {
		// there is no way to handle backPressed from fragment, so do this non OOP solution. Google, why are you so evil
		WelcomeFragment welcomeFragment = (WelcomeFragment) getSupportFragmentManager().findFragmentByTag(WelcomeFragment.class.getSimpleName());
		int orientation = getResources().getConfiguration().orientation;
		if (welcomeFragment != null && orientation == Configuration.ORIENTATION_LANDSCAPE) {
			if (welcomeFragment.hideYoutubeFullScreen()) {
				return;
			}
		} else if (welcomeFragment != null) { // if swipe to registration screen, handle back button to swipe back
			if (welcomeFragment.swipeBackFromSignUp()) {
				return;
			}
		}
		WelcomeTabsFragment welcomeTabsFragment = (WelcomeTabsFragment) getSupportFragmentManager().findFragmentByTag(WelcomeTabsFragment.class.getSimpleName());
		if (welcomeTabsFragment != null && welcomeTabsFragment.showPreviousFragment()) {
			return;
		}
		showPreviousFragment();
	}

	@Override
	public void showPreviousFragment() {
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
			SettingsProfileFragment fragmentByTag = (SettingsProfileFragment) getSupportFragmentManager().findFragmentByTag(SettingsProfileFragment.class.getSimpleName());
			if (fragmentByTag != null && fragmentByTag.isVisible()) {
				fragmentByTag.discardChanges();
				return super.onKeyUp(keyCode, event);
			}
		}
		return super.onKeyUp(keyCode, event);
	}

//	@Override
	private void setBadgeValueForId(int menuId, int value) {
		badgeItems.put(menuId, value);
		getActionBarHelper().setBadgeValueForId(menuId, value);
	}

//	@Override
//	public int getValueByBadgeId(int badgeId) {
//		return badgeItems.get(badgeId);
//	}

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

}