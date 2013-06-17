package com.chess.ui.fragments.welcome;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.backend.statics.AppData;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.FragmentTabsFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.05.13
 * Time: 17:01
 */
public class WelcomeTabsFragment extends CommonLogicFragment implements FragmentTabsFace {

	public static final int FEATURES_FRAGMENT = 0;
	public static final int SIGN_IN_FRAGMENT = 1;
	public static final int SIGN_UP_FRAGMENT = 2;
	public static final int GAME_FRAGMENT = 3;
	public static final int GAME_SETUP_FRAGMENT = 4;

	private View leftTabBtn;
	private View rightTabBtn;
	private boolean openWelcomeFragment;
	private CompGameConfig config;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		config = new CompGameConfig.Builder().build();

		changeInternalFragment(WelcomeGameCompFragment.createInstance(this, config));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_welcome_tabs_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		enableSlideMenus(false);

		leftTabBtn = view.findViewById(R.id.leftTabBtn);
		leftTabBtn.setOnClickListener(this);

		view.findViewById(R.id.centerTabBtn).setOnClickListener(this);

		rightTabBtn = view.findViewById(R.id.rightTabBtn);
		rightTabBtn.setOnClickListener(this);
		showActionBar(false);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		updateData(view);
	}

	private void updateData(View view) {
		int id = view.getId();
		CommonLogicFragment fragment;
		if (id == R.id.leftTabBtn) {
			openWelcomeFragment = false;
			fragment = (CommonLogicFragment) findFragmentByTag(SignInFragment.class.getSimpleName());
			if (fragment == null) {
				fragment = new SignInFragment();
			}
			getActivityFace().openFragment(fragment);

		} else if (id == R.id.centerTabBtn) {
			openWelcomeFragment = false;
			fragment = (CommonLogicFragment) findFragmentByTag(WelcomeGameCompFragment.class.getSimpleName());
			if (fragment == null) {
				fragment = WelcomeGameCompFragment.createInstance(this, config);
			}
			changeInternalFragment(fragment);
		} else if (id == R.id.rightTabBtn) {
			openWelcomeFragment = false;
			fragment = (CommonLogicFragment) findFragmentByTag(SignUpFragment.class.getSimpleName());
			if (fragment == null) {
				fragment = new SignUpFragment();
			}
			getActivityFace().openFragment(fragment);
		}
	}

	@Override
	public void changeInternalFragment(int code) {
		if (code == FEATURES_FRAGMENT) {
			openInternalFragment(WelcomeFragment.createInstance(this));
			openWelcomeFragment = true;
		} else if (code == SIGN_IN_FRAGMENT) {

			CommonLogicFragment fragment = (CommonLogicFragment) findFragmentByTag(SignInFragment.class.getSimpleName());
			if (fragment == null) {
				fragment = new SignInFragment();
			}
			getActivityFace().openFragment(fragment);
			openWelcomeFragment = true;
		} else if (code == SIGN_UP_FRAGMENT) {

			CommonLogicFragment fragment = (CommonLogicFragment) findFragmentByTag(SignUpFragment.class.getSimpleName());
			if (fragment == null) {
				fragment = new SignUpFragment();
			}
			getActivityFace().openFragment(fragment);
			openWelcomeFragment = true;
		} else if (code == GAME_SETUP_FRAGMENT) {
			changeInternalFragment(WelcomeGameSetupFragment.createInstance(this));
		} else if (code == GAME_FRAGMENT) {
			config.setMode(AppData.getCompGameMode(getActivity()));
			changeInternalFragment(WelcomeGameCompFragment.createInstance(this, config));
		}
	}

	@Override
	public void onPageSelected(int page) {
		if (page == WelcomeFragment.SIGN_UP_PAGE) {
			leftTabBtn.setVisibility(View.GONE);
			rightTabBtn.setVisibility(View.GONE);
		} else {
			leftTabBtn.setVisibility(View.VISIBLE);
			rightTabBtn.setVisibility(View.VISIBLE);
		}
	}



	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.tab_content_frame, fragment).commit();
	}

	private void openInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.tab_content_frame, fragment)
		.addToBackStack(fragment.getClass().getSimpleName()).commit();
	}

	public boolean showPreviousFragment(){
		if (getChildFragmentManager().getBackStackEntryCount() > 0) {
			FragmentManager.BackStackEntry entry = getChildFragmentManager().getBackStackEntryAt(0);  // findFragmentByTag gives null :(
			if (entry!= null && entry.getName().equals(WelcomeFragment.class.getSimpleName()) && openWelcomeFragment){
				getChildFragmentManager().popBackStackImmediate();
				openWelcomeFragment = false;
				return true;
			} else {  // TODO fix bad navigation behaviour when moving back from Welcome -> SignUp/SignIn
				return false;
			}
		} else {
			return false;
		}
	}

}
