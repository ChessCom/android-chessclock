package com.chess.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.RadioGroup;
import com.chess.R;
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
		showActionBar(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_home_tabs_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.tab_content_frame);

		Fragment homeGamesFragment = new HomeGamesFragment();
		changeInternalFragment(homeGamesFragment);

		tabRadioGroup = (RadioGroup) view.findViewById(R.id.tabRadioGroup);
		tabRadioGroup.setOnCheckedChangeListener(this);

		previousCheckedId = tabRadioGroup.getCheckedRadioButtonId();
	}

	@Override
	public void onResume() {
		super.onResume();
		updateTabs();

		getActivityFace().setBadgeValueForId(R.id.menu_new_game, 1);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		updateTabs();
	}

	private void updateTabs() {
		int checkedButtonId = tabRadioGroup.getCheckedRadioButtonId();
		if (checkedButtonId != previousCheckedId){
			previousCheckedId = checkedButtonId;
			switch (checkedButtonId) {
				case R.id.gamesBtn:
					changeInternalFragment(new HomeGamesFragment());
					break;
				case R.id.chesscomBtn:
					changeInternalFragment(new HomeChessComFragment());
					break;
				case R.id.ratingsBtn:
					changeInternalFragment(new HomeRatingsFragment());
					break;
			}
		}

	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.tab_content_frame, fragment).commit();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {   // Should be called to enable OptionsMenu handle
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_new_game:
				getActivityFace().toggleMenu(SlidingMenu.RIGHT);

//				showPopupDialog(R.string.invalidId, "Hello", "test");   // Works!
				break;
		}
		return true;
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (isTagEmpty(fragment)) {
			return;
		}

		if (tag.equals("test")) {
			showToast("test ok passed");
		}
		super.onPositiveBtnClick(fragment);
	}
}
