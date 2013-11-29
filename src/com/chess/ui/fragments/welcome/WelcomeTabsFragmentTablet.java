package com.chess.ui.fragments.welcome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.statics.WelcomeHolder;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.FragmentTabsFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.11.13
 * Time: 9:57
 */
public class WelcomeTabsFragmentTablet extends CommonLogicFragment implements FragmentTabsFace {

	public static final int WELCOME_FRAGMENT = 0;
	public static final int SIGN_IN_FRAGMENT = 1;
	public static final int SIGN_UP_FRAGMENT = 2;
	public static final int GAME_FRAGMENT = 3;

	private boolean openWelcomeFragment;
	private CompGameConfig config;
	private View loginButtonsView;
	private View loginBtn;
	private View signUpBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		config = new CompGameConfig.Builder().build();

		changeInternalFragment(GameWelcomeCompFragmentTablet.createInstance(this, config));
		if (WelcomeHolder.getInstance().isFullscreen()) {
			changeInternalFragment(WELCOME_FRAGMENT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_welcome_tabs_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		enableSlideMenus(false);

		view.findViewById(R.id.centerTabBtn).setOnClickListener(this);

		loginButtonsView = view.findViewById(R.id.loginButtonsView);
		loginButtonsView.setVisibility(View.GONE);
//		if (inPortrait()) {
//			int entryCount = getFragmentManager().getBackStackEntryCount();
//			if (entryCount > 0) {
//				FragmentManager.BackStackEntry entry = getFragmentManager().getBackStackEntryAt(entryCount - 1);
//
//				if (entry!= null && (entry.getName().equals(SignUpFragment.class.getSimpleName())
//						|| entry.getName().equals(SignInFragment.class.getSimpleName()))) {
//
//					// show bottom view
//					loginButtonsView.setVisibility(View.VISIBLE);
//				}
//			}
//		}

		showActionBar(false);

		loginBtn = view.findViewById(R.id.loginBtn);
		loginBtn.setOnClickListener(this);

		signUpBtn = view.findViewById(R.id.signUpBtn);
		signUpBtn.setOnClickListener(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && (requestCode & 0xFFFF) == 0xFACE) { // if it was request to authorize facebook user
			for (Fragment fragment : getChildFragmentManager().getFragments()) {
				if (fragment != null) {
					fragment.onActivityResult(requestCode, resultCode, data);
				}
			}
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();

		CommonLogicFragment fragment;
		if (id == R.id.centerTabBtn) {
			openWelcomeFragment = false;
			fragment = (CommonLogicFragment) findFragmentByTag(GameWelcomeCompFragmentTablet.class.getSimpleName());
			if (fragment == null) {
				fragment = GameWelcomeCompFragment.createInstance(this, config);
			}
			changeInternalFragment(fragment);
		} else if (id == R.id.loginBtn) {
			openSignInFragment();
			openWelcomeFragment = true;
		} else if (id == R.id.signUpBtn) {
			openSingUpFragment();
			openWelcomeFragment = true;
		}
	}

	@Override
	public void changeInternalFragment(int code) {
		if (code == WELCOME_FRAGMENT) {
			openInternalFragment(WelcomeTourFragmentTablet.createInstance(this));
			openWelcomeFragment = true;

			loginButtonsView.setVisibility(View.VISIBLE);
			loginBtn.setVisibility(View.VISIBLE);
			signUpBtn.setVisibility(View.VISIBLE);
		} else if (code == SIGN_IN_FRAGMENT) {

			openSignInFragment();
			if (inLandscape()) {
				loginButtonsView.setVisibility(View.VISIBLE);
			}
		} else if (code == SIGN_UP_FRAGMENT) {

			openSingUpFragment();
			if (inLandscape()) {
				loginButtonsView.setVisibility(View.VISIBLE);
			}
		} else if (code == GAME_FRAGMENT) {
			config.setMode(getAppData().getCompGameMode());
			config.setStrength(getAppData().getCompLevel());
			changeInternalFragment(GameWelcomeCompFragmentTablet.createInstance(this, config));
		}
	}

	public void openSignInFragment() {
		CommonLogicFragment fragment = (CommonLogicFragment) findFragmentByTag(SignInFragment.class.getSimpleName());
		if (fragment == null) {
			fragment = new SignInFragment();
		}
		openInternalFragment(fragment);
		if (inLandscape()) {
			loginBtn.setVisibility(View.GONE);
			signUpBtn.setVisibility(View.GONE);
		} else {
			loginButtonsView.setVisibility(View.GONE);
		}
	}

	private void openSingUpFragment() {
		CommonLogicFragment fragment = (CommonLogicFragment) findFragmentByTag(SignUpFragment.class.getSimpleName());
		if (fragment == null) {
			fragment = new SignUpFragment();
		}
		openInternalFragment(fragment);
		if (inLandscape()) {
			signUpBtn.setVisibility(View.GONE);
			loginBtn.setVisibility(View.GONE);
		} else {
		    loginButtonsView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onPageSelected(int page) {
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.tab_content_frame, fragment).commitAllowingStateLoss();
	}

	private void openInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.tab_content_frame, fragment)
				.addToBackStack(fragment.getClass().getSimpleName()).commitAllowingStateLoss();
	}

	@Override
	public boolean showPreviousFragment() {
		int entryCount = getChildFragmentManager().getBackStackEntryCount();
		if (entryCount > 0) {

			// if last fragment is WelcomeTour then hide left side view
			FragmentManager.BackStackEntry entry = getChildFragmentManager().getBackStackEntryAt(entryCount - 1);

			if (entry!= null && (entry.getName().equals(SignUpFragment.class.getSimpleName())
								|| entry.getName().equals(SignInFragment.class.getSimpleName()))
								&& openWelcomeFragment) {
				getChildFragmentManager().popBackStackImmediate();
				loginBtn.setVisibility(View.VISIBLE);
				signUpBtn.setVisibility(View.VISIBLE);
				loginButtonsView.setVisibility(View.VISIBLE);

				openWelcomeFragment = false;
				return true;
			} else if (entry!= null && (entry.getName().equals(SignUpFragment.class.getSimpleName())
								|| entry.getName().equals(SignInFragment.class.getSimpleName()))
								&& !openWelcomeFragment) {
				getChildFragmentManager().popBackStackImmediate();
				loginButtonsView.setVisibility(View.GONE);

				return true;
			} else if (entry!= null && entry.getName().equals(WelcomeTourFragmentTablet.class.getSimpleName())) {
				if (loginButtonsView != null) { // can be null if user turned from port to land
					loginButtonsView.setVisibility(View.GONE);
				}
				getChildFragmentManager().popBackStackImmediate();
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
