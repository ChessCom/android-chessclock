package com.chess.ui.fragments.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.GetAndSaveUserStats;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.NavigationMenuFragment;
import com.chess.ui.fragments.daily.DailyGamesFragment;
import com.slidingmenu.lib.SlidingMenu;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 21:41
 */
public class HomeTabsFragment extends CommonLogicFragment implements RadioGroup.OnCheckedChangeListener {

	private RadioGroup tabRadioGroup;
	private int previousCheckedId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		getActivity().startService(new Intent(getActivity(), GetAndSaveUserStats.class));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// activate Left and right menu fragments
		getActivityFace().setTouchModeToSlidingMenu(SlidingMenu.TOUCHMODE_FULLSCREEN);
		getActivityFace().changeLeftFragment(new NavigationMenuFragment());
//		getActivityFace().changeRightFragment(new DailyGamesNotificationFragment());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_base_tabs_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		getActivityFace().setCustomActionBarViewId(R.layout.new_home_actionbar);

		((TextView) view.findViewById(R.id.leftTabBtn)).setText(R.string.play);
		((TextView) view.findViewById(R.id.centerTabBtn)).setText(R.string.learn);
		((TextView) view.findViewById(R.id.rightTabBtn)).setText(R.string.feed);

		showActionBar(true);

//		Fragment homeGamesFragment = new HomePlayFragment();
		Fragment homeGamesFragment = new DailyGamesFragment();
		changeInternalFragment(homeGamesFragment);

		tabRadioGroup = (RadioGroup) view.findViewById(R.id.tabRadioGroup);
		tabRadioGroup.setOnCheckedChangeListener(this);

		previousCheckedId = tabRadioGroup.getCheckedRadioButtonId();
	}

	@Override
	public void onResume() {
		super.onResume();
		updateTabs();

		getActivityFace().setBadgeValueForId(R.id.menu_games, 7);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		updateTabs();
	}

	private void updateTabs() {
		int checkedButtonId = tabRadioGroup.getCheckedRadioButtonId();
		if (checkedButtonId != previousCheckedId) {
			previousCheckedId = checkedButtonId;
			switch (checkedButtonId) {
				case R.id.leftTabBtn: {
//					Fragment fragment = findFragmentByTag(HomePlayFragment.class.getSimpleName());
//					if (fragment == null) {
//						fragment = new HomePlayFragment();
//					}
//					changeInternalFragment(fragment);
					Fragment fragment = findFragmentByTag(DailyGamesFragment.class.getSimpleName());
					if (fragment == null) {
						fragment = new DailyGamesFragment();
					}
					changeInternalFragment(fragment);
					break;
				}
				case R.id.centerTabBtn: {
					Fragment fragment = findFragmentByTag(HomeLearnFragment.class.getSimpleName());
					if (fragment == null) {
						fragment = new HomeLearnFragment();
					}
					changeInternalFragment(fragment);
					break;
				}
				case R.id.rightTabBtn: {
					Fragment fragment = findFragmentByTag(HomeFeedFragment.class.getSimpleName());
					if (fragment == null) {
						fragment = new HomeFeedFragment();
					}
					changeInternalFragment(fragment);
					break;
				}
			}
		}
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.tab_content_frame, fragment).commit();
	}

}
