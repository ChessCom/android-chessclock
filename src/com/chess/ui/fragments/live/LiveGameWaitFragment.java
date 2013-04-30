package com.chess.ui.fragments.live;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.backend.statics.AppData;
import com.chess.ui.fragments.LiveBaseFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.04.13
 * Time: 9:30
 */
public class LiveGameWaitFragment extends LiveBaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_live_game_wait_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.live_chess);

		view.findViewById(R.id.cancelLiveBtn).setOnClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		AppData.setLiveChessMode(getActivity(), true);
		liveBaseActivity.connectLcc();
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.cancelLiveBtn) {
			logoutFromLive();

			getActivityFace().showPreviousFragment();
		}
	}
}
