package com.chess.ui.fragments;

import actionbarcompat.BadgeItem;
import android.os.Bundle;
import android.view.*;
import com.chess.R;
import com.slidingmenu.lib.SlidingMenu;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 22:04
 */
public class HomeRatingsFragment extends CommonLogicFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_home_ratings_frame, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		getActivityFace().setBadgeValueForId(new BadgeItem(R.id.menu_new_game, 1));
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
				break;
		}
		return true;
	}
}
