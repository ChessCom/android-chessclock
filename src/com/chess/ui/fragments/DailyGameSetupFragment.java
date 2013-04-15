package com.chess.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 15.04.13
 * Time: 12:29
 */
public class DailyGameSetupFragment extends CommonLogicFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_daily_game_setup_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		updateTitle(R.string.daily_chess);


		view.findViewById(R.id.dailyPlayBtn).setOnClickListener(this);
		view.findViewById(R.id.autoMatchBtn).setOnClickListener(this);
		view.findViewById(R.id.dailyTimeSelectionBtn).setOnClickListener(this);
		view.findViewById(R.id.inviteFriend1Btn).setOnClickListener(this);
		view.findViewById(R.id.inviteFriend2Btn).setOnClickListener(this);
		view.findViewById(R.id.playFriendView).setOnClickListener(this);
		view.findViewById(R.id.dailyOptionsView).setOnClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		// TODO -> File | Settings | File Templates.
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.dailyPlayBtn) {

		} else if (id == R.id.autoMatchBtn) {
		} else if (id == R.id.dailyTimeSelectionBtn) {
		} else if (id == R.id.inviteFriend1Btn) {
		} else if (id == R.id.inviteFriend2Btn) {
		} else if (id == R.id.playFriendView) {
		} else if (id == R.id.dailyOptionsView) {


		}
	}
}
