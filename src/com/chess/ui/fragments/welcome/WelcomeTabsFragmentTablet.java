package com.chess.ui.fragments.welcome;

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
	private View tourLeftView;
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
		return inflater.inflate(R.layout.new_welcome_tabs_frame_tablet, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		enableSlideMenus(false);

		view.findViewById(R.id.centerTabBtn).setOnClickListener(this);

		tourLeftView = view.findViewById(R.id.tourLeftView);
		tourLeftView.setVisibility(View.GONE);

		showActionBar(false);

		loginBtn = view.findViewById(R.id.loginBtn);
		loginBtn.setOnClickListener(this);

		signUpBtn = view.findViewById(R.id.signUpBtn);
		signUpBtn.setOnClickListener(this);
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

			tourLeftView.setVisibility(View.VISIBLE);
			loginBtn.setVisibility(View.VISIBLE);
			signUpBtn.setVisibility(View.VISIBLE);
		} else if (code == SIGN_IN_FRAGMENT) {

			openSignInFragment();
			tourLeftView.setVisibility(View.VISIBLE);
		} else if (code == SIGN_UP_FRAGMENT) {

			openSingUpFragment();
			tourLeftView.setVisibility(View.VISIBLE);
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
		loginBtn.setVisibility(View.GONE);
		signUpBtn.setVisibility(View.GONE);
	}

	private void openSingUpFragment() {
		CommonLogicFragment fragment = (CommonLogicFragment) findFragmentByTag(SignUpFragment.class.getSimpleName());
		if (fragment == null) {
			fragment = new SignUpFragment();
		}
		openInternalFragment(fragment);
		signUpBtn.setVisibility(View.GONE);
		loginBtn.setVisibility(View.GONE);
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

				openWelcomeFragment = false;
				return true;
			} else if (entry!= null && (entry.getName().equals(SignUpFragment.class.getSimpleName())
								|| entry.getName().equals(SignInFragment.class.getSimpleName()))
								&& !openWelcomeFragment) {
				getChildFragmentManager().popBackStackImmediate();
				tourLeftView.setVisibility(View.GONE);

				return true;
			} else if (entry!= null && entry.getName().equals(WelcomeTourFragmentTablet.class.getSimpleName())) {
				tourLeftView.setVisibility(View.GONE);
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
