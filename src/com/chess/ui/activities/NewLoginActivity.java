package com.chess.ui.activities;

import actionbarcompat.BadgeItem;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import com.chess.R;
import com.chess.ui.fragments.BaseFragment;
import com.chess.ui.fragments.SignInFragment;
import com.chess.ui.fragments.RightMenuFragment;
import com.chess.ui.interfaces.ActiveFragmentInterface;
import com.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 13:37
 */
public class NewLoginActivity extends LiveBaseActivity implements ActiveFragmentInterface{

	private Fragment currentActiveFragment;
	private List<BadgeItem> badgeItems;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSlidingActionBarEnabled(true);

		setContentView(R.layout.new_main_active_screen);

		// set the Above View
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.content_frame, new SignInFragment())
				.commit();

		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT_RIGHT);
		sm.setSecondaryMenu(R.layout.slide_menu_right_frame);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.menu_frame_right, new RightMenuFragment())
				.commit();
		sm.setSecondaryShadowDrawable(R.drawable.defaultshadowright);
		sm.setShadowDrawable(R.drawable.defaultshadow);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		badgeItems = new ArrayList<BadgeItem>();
	}

	@Override
	protected void onStart() {
		super.onStart();
		showActionNewGame = true;
	}

	@Override
	public void openFragment(BaseFragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

//		ft.setCustomAnimations(R.anim.hold, R.anim.slide_from_0_to_minus100, R.anim.hold, android.R.anim.slide_out_right);
		ft.replace(R.id.content_frame, fragment);
		ft.addToBackStack(null);
		ft.commit();
	}

	@Override
	public void openFragment(BaseFragment fragment, int code) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

//		ft.setCustomAnimations(R.anim.hold, R.anim.slide_from_0_to_minus100, R.anim.hold, android.R.anim.slide_out_right);
		ft.replace(R.id.content_frame, fragment);
		ft.addToBackStack(null);
		ft.commit();
	}

	@Override
	public void switchFragment(BaseFragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

		ft.replace(R.id.content_frame, fragment);
		ft.commit();
	}

	@Override
	public void switchFragment(BaseFragment fragment, int code) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		currentActiveFragment = fragment;

		ft.replace(R.id.content_frame, fragment);
		ft.commit();
	}
	@Override
	public boolean isMenuActive(int code) {
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void toggleMenu(int code) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void closeMenu(int code) {
		//To change body of implemented methods use File | Settings | File Templates.
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
		}/*else{
			toggleMenu();
		}*/
	}

	@Override
	public void updateCurrentActiveFragment() {
	}

	@Override
	public void setBadgeValueForId(BadgeItem badgeItem) {
		badgeItems.add(badgeItem);
		getActionBarHelper().setBadgeValueForId(badgeItem);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		boolean displayMenu = super.onCreateOptionsMenu(menu);
		for (BadgeItem badgeItem : badgeItems) {
			getActionBarHelper().setBadgeValueForId(badgeItem, menu);
		}
		return displayMenu;
	}



}