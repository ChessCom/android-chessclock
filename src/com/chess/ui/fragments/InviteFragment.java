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
 * Date: 04.01.13
 * Time: 13:39
 */
public class InviteFragment extends CommonLogicFragment implements View.OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		showActionBar(false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_invite_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (HONEYCOMB_PLUS_API) {
			view.findViewById(R.id.mainFrame).setBackground(new NewBackgroundChessDrawable(getActivity()));
		} else {
			view.findViewById(R.id.mainFrame).setBackgroundDrawable(new NewBackgroundChessDrawable(getActivity()));
		}

		view.findViewById(R.id.startPlayBtn).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.startPlayBtn) {
			getActivityFace().switchFragment(new HomeTabsFragment());
		}
	}
}
