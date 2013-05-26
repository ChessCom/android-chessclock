package com.chess.ui.fragments.welcome;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.WelcomeTabsFace;
import com.chess.ui.views.PanelInfoWelcomeView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.05.13
 * Time: 17:01
 */
public class WelcomeTabsFragment extends CommonLogicFragment implements WelcomeTabsFace {

	private View leftTabBtn;
	private View rightTabBtn;
	private boolean openWelcomeFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		changeInternalFragment(new WelcomeGameCompFragment(this));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_welcome_tabs_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

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

	@Override
	public void onClicked(View view) {
		if (view.getId() == PanelInfoWelcomeView.WHAT_IS_TXT_ID) {
			openInternalFragment(new WelcomeFragment(this));
			openWelcomeFragment = true;
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
				fragment = new WelcomeGameCompFragment(this);
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
