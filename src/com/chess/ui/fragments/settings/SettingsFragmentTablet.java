package com.chess.ui.fragments.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import com.chess.R;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.interfaces.FragmentParentFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.11.13
 * Time: 10:02
 */
public class SettingsFragmentTablet extends SettingsFragment implements FragmentParentFace{

	private boolean noCategoriesFragmentsAdded;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		openInternalFragment(new SettingsProfileFragment());

		noCategoriesFragmentsAdded = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_common_tablet_content_frame, container, false);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		for (SettingsMenuItem menuItem : menuItems) {
			menuItem.selected = false;
		}

		menuItems.get(position).selected = true;
		SettingsMenuItem menuItem = (SettingsMenuItem) listView.getItemAtPosition(position);
		menuItem.selected = true;
		((BaseAdapter)parent.getAdapter()).notifyDataSetChanged();

		// TODO adjust switch/closeBoard when the same fragment opened
		switch (menuItem.iconRes) {
//			case R.string.ic_key:
//				getActivityFace().openFragment(new SettingsApiFragment());
//				break;
			case R.string.ic_profile:
				changeInternalFragment(new SettingsProfileFragment());
				break;
			case R.string.ic_board:
				changeInternalFragment(new SettingsGeneralFragmentTablet(this));
				break;
			case R.string.ic_daily_game:
				changeInternalFragment(new SettingsDailyChessFragment());
				break;
			case R.string.ic_live_standard:
				changeInternalFragment(new SettingsLiveChessFragment());
				break;
			case R.string.ic_theme:
				changeInternalFragment(new SettingsThemeFragmentTablet(this));
				break;
			case R.string.ic_key_badge:
				changeInternalFragment(new SettingsPasswordFragment());
				break;
			case R.string.ic_close:
				logoutFromLive();
				performLogout();

				break;
		}
	}

	@Override
	public void changeFragment(BasePopupsFragment fragment) {
		openInternalFragment(fragment);
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.innerFragmentContainer, fragment).commitAllowingStateLoss();
	}

	private void openInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.innerFragmentContainer, fragment, fragment.getClass().getSimpleName());
		transaction.addToBackStack(fragment.getClass().getSimpleName());
		transaction.commit();
	}

	@Override
	public boolean showPreviousFragment() {
		if (getActivity() == null) {
			return false;
		}
		int entryCount = getChildFragmentManager().getBackStackEntryCount();
		if (entryCount > 0) {
			int last = entryCount - 1;
			FragmentManager.BackStackEntry stackEntry = getChildFragmentManager().getBackStackEntryAt(last);
			if (stackEntry != null && stackEntry.getName().equals(SettingsThemeCustomizeFragment.class.getSimpleName())) {
				noCategoriesFragmentsAdded = true;
			}

			return getChildFragmentManager().popBackStackImmediate();
		} else {
			return super.showPreviousFragment();
		}
	}
}
