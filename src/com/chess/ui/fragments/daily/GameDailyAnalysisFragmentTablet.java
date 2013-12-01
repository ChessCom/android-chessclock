package com.chess.ui.fragments.daily;

import android.os.Bundle;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 6:27
 */
public class GameDailyAnalysisFragmentTablet extends GameDailyAnalysisFragment {

	public GameDailyAnalysisFragmentTablet(){}

	public static GameDailyAnalysisFragmentTablet createInstance(long gameId, String username) {
		GameDailyAnalysisFragmentTablet fragment = new GameDailyAnalysisFragmentTablet();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		arguments.putString(USERNAME, username);
		fragment.setArguments(arguments);

		return fragment;
	}
}
