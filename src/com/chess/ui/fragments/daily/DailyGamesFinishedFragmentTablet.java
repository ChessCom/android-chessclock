package com.chess.ui.fragments.daily;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.statics.Symbol;
import com.chess.ui.fragments.RightPlayFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.11.13
 * Time: 18:31
 */
public class DailyGamesFinishedFragmentTablet extends DailyGamesFinishedFragment {

	public DailyGamesFinishedFragmentTablet(){
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, Symbol.EMPTY);
		setArguments(bundle);
	}

	public static DailyGamesFinishedFragmentTablet createInstance(String username) {
		DailyGamesFinishedFragmentTablet fragment = new DailyGamesFinishedFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, username);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
			transaction.add(R.id.optionsFragmentContainer, RightPlayFragment.createInstance(RIGHT_MENU_MODE))
					.commitAllowingStateLoss();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_home_daily_finished_games_frame, container, false);
	}

	@Override
	protected void widgetsInit(View view) {
		super.widgetsInit(view);

		if (!username.equals(getUsername())) {
			View optionsFragmentContainerView = view.findViewById(R.id.optionsFragmentContainerView);
			if (optionsFragmentContainerView != null) {
				optionsFragmentContainerView.setVisibility(View.GONE);
			}
		}
	}

}

