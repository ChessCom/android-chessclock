package com.chess.ui.fragments;

import actionbarcompat.BadgeItem;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 22:04
 */
public class HomeRatingsFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_home_ratings_frame, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		getActivityFace().setBadgeValueForId(new BadgeItem(R.id.menu_new_game, 1));
	}
}
