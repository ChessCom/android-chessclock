package com.chess.ui.fragments.profiles;

import android.os.Bundle;
import com.chess.backend.LoadHelper;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.DailyGamesAllItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.fragments.daily.DailyGamesFragment;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.08.13
 * Time: 10:33
 */
public class ProfileGamesFragment extends DailyGamesFragment {

	private static final String USERNAME = "username";

	private String username;

	public ProfileGamesFragment() {
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, "");
		setArguments(bundle);
	}

	public static ProfileGamesFragment createInstance(String username) {
		ProfileGamesFragment fragment = new ProfileGamesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, username);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			username = getArguments().getString(USERNAME);
		} else {
			username = savedInstanceState.getString(USERNAME);
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(USERNAME, username);
	}

	@Override
	protected void updateData() {
		if (!AppUtils.isNetworkAvailable(getActivity())) {
			return;
		}

		// First we check ids of games what we have. Challenges also will be stored in DB
		// when we ask server about new ids of games and challenges
		// if server have new ids we get those games with ids

		LoadItem loadItem = LoadHelper.getAllGames(getUserToken());
		loadItem.addRequestParams(RestHelper.P_USERNAME, username);

		new RequestJsonTask<DailyGamesAllItem>(dailyGamesUpdateListener).executeTask(loadItem);
	}

}