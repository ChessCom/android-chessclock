package com.chess.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.ui.views.NewBackgroundChessDrawable;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 03.01.13
 * Time: 9:09
 */
public class CreateProfileFragment extends CommonLogicFragment implements View.OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		showActionBar(false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_create_profile_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (HONEYCOMB_PLUS_API) {
			view.findViewById(R.id.mainFrame).setBackground(new NewBackgroundChessDrawable(getActivity()));
		} else {
			view.findViewById(R.id.mainFrame).setBackgroundDrawable(new NewBackgroundChessDrawable(getActivity()));
		}

		view.findViewById(R.id.createProfileBtn).setOnClickListener(this);
		view.findViewById(R.id.skipBtn).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.skipBtn) {
			getActivityFace().switchFragment(new HomeTabsFragment());
		} else if (v.getId() == R.id.createProfileBtn) {
			getActivityFace().openFragment(new InviteFragment());
		}
	}
}
