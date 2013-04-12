package com.chess.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.04.13
 * Time: 18:29
 */
public class HomePlayFragment extends CommonLogicFragment {

	private TextView liveRatingTxt;
	private TextView dailyRatingTxt;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_home_play_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		liveRatingTxt = (TextView) view.findViewById(R.id.liveRatingTxt);
		dailyRatingTxt = (TextView) view.findViewById(R.id.dailyRatingTxt);
		view.findViewById(R.id.liveTimeSelectBtn).setOnClickListener(this);
		view.findViewById(R.id.livePlayBtn).setOnClickListener(this);
		view.findViewById(R.id.dailyTimeSelectBtn).setOnClickListener(this);
		view.findViewById(R.id.dailyPlayBtn).setOnClickListener(this);

	}

	@Override
	public void onStart() {
		super.onStart();

		setRatings();
		// load friends, get only 2

	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.liveTimeSelectBtn) {

		} else if (view.getId() == R.id.livePlayBtn) {
		} else if (view.getId() == R.id.dailyTimeSelectBtn) {
		} else if (view.getId() == R.id.dailyPlayBtn) {
		} else if (view.getId() == R.id.playFriendView) {
			getActivityFace().openFragment(new FriendsFragment());
		}

	}

	private void setRatings() {
		// set live rating
		int liveRating = DBDataManager.getUserCurrentRating(getActivity(), DBConstants.GAME_STATS_LIVE_STANDARD);
//		liveRatingTxt.setText(String.valueOf(liveRating));

		// set daily rating
		int dailyRating = DBDataManager.getUserCurrentRating(getActivity(), DBConstants.GAME_STATS_DAILY_CHESS);
//		dailyRatingTxt.setText(String.valueOf(dailyRating));

	}
}
