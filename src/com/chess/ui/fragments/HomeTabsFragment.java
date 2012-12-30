package com.chess.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TabHost;
import com.chess.R;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 21:41
 */
public class HomeTabsFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener {

	private RadioGroup tabRadioGroup;
	private int previousCheckedId;

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
}
