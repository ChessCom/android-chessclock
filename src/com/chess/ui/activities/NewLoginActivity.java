package com.chess.ui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Menu;
import com.chess.R;
import com.chess.backend.statics.AppData;
import com.chess.lcc.android.LccHolder;
import com.chess.ui.fragments.*;
import com.chess.ui.interfaces.ActiveFragmentInterface;
import com.chess.ui.views.drawables.LogoBackgroundDrawable;
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
public class NewLoginActivity extends LiveBaseActivity implements ActiveFragmentInterface{

	private Fragment currentActiveFragment;
	private Hashtable<Integer, Integer> badgeItems;
	private CommonLogicFragment rightMenuFragment;
	private LogoBackgroundDrawable logoBackground;
	private SlidingMenu slidingMenu;
	private List<SlidingMenu.OnOpenedListener> openMenuListeners;
	private boolean showActionBar;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSlidingActionBarEnabled(true);

		setContentView(R.layout.new_main_active_screen);

		openMenuListeners = new ArrayList<SlidingMenu.OnOpenedListener>();

		// set the Above View
		if (!TextUtils.isEmpty(AppData.getUserToken(this))) { // if user have login token already
//			switchFragment(new HomeTabsFragment());
			switchFragment(new StatsFragment());
			showActionBar = true;
		} else {
			switchFragment(new SignInFragment());
			showActionBar = false;
		}

		slidingMenu = getSlidingMenu();
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		slidingMenu.setShadowDrawable(R.drawable.defaultshadow);
		slidingMenu.setOnOpenedListener(openMenuListener);

		badgeItems = new Hashtable<Integer, Integer>();

		logoBackground = new LogoBackgroundDrawable(this);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		getActionBarHelper().showActionBar(showActionBar);

		if (HONEYCOMB_PLUS_API) {
			getActionBar().setCustomView(R.layout.new_custom_actionbar);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		showActionNewGame = true;
	}

	@Override
	public void setTitle(int titleId) {
		getActionBarHelper().setTitle(titleId);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		logoBackground.updateConfig();
	}


	private SlidingMenu.OnOpenedListener openMenuListener = new SlidingMenu.OnOpenedListener() {
		@Override
		public void onOpened() {
			for (SlidingMenu.OnOpenedListener openedListener : openMenuListeners) { // Inform listeners inside fragments
				openedListener.onOpened();
			}

			if (slidingMenu.isSecondaryMenuShowing()) {
//				showToast("Right");
			} else {
//				showToast("Left");
			}

		}
	};

	public void addOnOpenMenuListener(SlidingMenu.OnOpenedListener listener) {
		openMenuListeners.add(listener);
	}

	@Override
	public LccHolder getMeLccHolder() {
		return getLccHolder();
	}

	@Override
	public void registerGcm() {
		registerGcmService();
	}

	@Override
	public void changeRightFragment(CommonLogicFragment fragment) {
		// set right menu. Left Menu is already set in BaseActivity
//		rightMenuFragment = new HomeDailyGamesFragment();
		rightMenuFragment = fragment;
		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT_RIGHT);
		sm.setSecondaryMenu(R.layout.slide_menu_right_frame);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.menu_frame_right, rightMenuFragment)
				.commit();
		sm.setSecondaryShadowDrawable(R.drawable.defaultshadowright);
		sm.setShadowDrawable(R.drawable.defaultshadow);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}

	@Override
	public void changeLeftFragment(CommonLogicFragment fragment) {
		// change left menu fragment
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//		leftMenuFragment = new NavigationMenuFragment();
		leftMenuFragment = fragment;
		ft.replace(R.id.menu_frame_left, leftMenuFragment);
		ft.commit();
	}

	@Override
	public void setTouchModeToSlidingMenu(int touchMode){
		SlidingMenu sm = getSlidingMenu();
		sm.setTouchModeAbove(touchMode);
	}

	@Override
	public void openFragment(BasePopupsFragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

		ft.replace(R.id.content_frame, fragment);
		ft.addToBackStack(null);
		ft.commit();
	}

	@Override
	public void openFragment(BasePopupsFragment fragment, int code) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

		ft.replace(R.id.content_frame, fragment);
		ft.addToBackStack(null);
		ft.commit();
	}

	@Override
	public void switchFragment(BasePopupsFragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

		ft.replace(R.id.content_frame, fragment);
		ft.commit();
	}

	@Override
	public void switchFragment(BasePopupsFragment fragment, int code) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

		ft.replace(R.id.content_frame, fragment);
		ft.commit();
	}
	@Override
	public boolean isMenuActive() {
//		return getSlidingMenu().isMenuShowing();
		return false;
	}

	@Override
	public void toggleMenu(int code) {
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

	@Override
	public void closeMenu(int code) {
//		getSlidingMenu().toggle();
	}

	@Override
	public void onBackPressed() {
		showPreviousFragment();
	}

	@Override
	public void showPreviousFragment() {
		/*if(menuFace.isActive())
			finish();
		else*/
		if(getSupportFragmentManager().popBackStackImmediate()){
			if(getSupportFragmentManager().getBackStackEntryCount() == 0){ // means we have only home fragment in stack
			}
		}else{
			super.onBackPressed();
		}
	}

	@Override
	public void updateCurrentActiveFragment() {
	}

	@Override
	public void setBadgeValueForId(int menuId, int value) {
		badgeItems.put(menuId, value);
		getActionBarHelper().setBadgeValueForId(menuId, value);
	}

	@Override
	public CoreActivityActionBar getActionBarActivity() {
		return getInstance();
	}

	@Override
	public Drawable getLogoBackground() {
		return logoBackground;
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

}