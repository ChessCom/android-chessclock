package com.chess.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import com.chess.R;
import com.chess.ui.fragments.*;
import com.chess.ui.interfaces.ActiveFragmentInterface;
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
//	private List<BadgeItem> badgeItems;
	private Hashtable<Integer, Integer> badgeItems;
	private CommonLogicFragment rightMenuFragment;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSlidingActionBarEnabled(true);

		setContentView(R.layout.new_main_active_screen);

		// change left menu fragment
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		mFrag = new MainMenuFragment();
		ft.replace(R.id.menu_frame_left, mFrag);
		ft.commit();

		// set the Above View
		switchFragment(new SignInFragment());

		// set right menu. Left Menu is already set in BaseActivity
		rightMenuFragment = new DailyGamesFragment();
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

//		badgeItems = new ArrayList<BadgeItem>();
		badgeItems = new Hashtable<Integer, Integer>();
	}

	@Override
	protected void onStart() {
		super.onStart();
		showActionNewGame = true;
	}

	@Override
	public void openFragment(BasePopupsFragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

//		ft.setCustomAnimations(R.anim.hold, R.anim.slide_from_0_to_minus100, R.anim.hold, android.R.anim.slide_out_right);
		ft.replace(R.id.content_frame, fragment);
		ft.addToBackStack(null);
		ft.commit();
	}

	@Override
	public void openFragment(BasePopupsFragment fragment, int code) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

//		ft.setCustomAnimations(R.anim.hold, R.anim.slide_from_0_to_minus100, R.anim.hold, android.R.anim.slide_out_right);
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
		return getSlidingMenu().isMenuShowing();
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
				rightMenuFragment.onVisibilityChanged(visible);
				break;
		}
	}

	@Override
	public void closeMenu(int code) {
		getSlidingMenu().toggle();
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