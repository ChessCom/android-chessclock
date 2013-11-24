package com.chess.ui.fragments.daily;

import android.os.Bundle;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 6:27
 */
public class GameDailyAnalysisFragmentTablet extends GameDailyAnalysisFragment {

//	private ControlsAnalysisViewTablet controlsView;

	public GameDailyAnalysisFragmentTablet(){}

	public static GameDailyAnalysisFragmentTablet createInstance(long gameId, String username) {
		GameDailyAnalysisFragmentTablet fragment = new GameDailyAnalysisFragmentTablet();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		arguments.putString(USERNAME, username);
		fragment.setArguments(arguments);

		return fragment;
	}

//	@Override
//	protected ControlsAnalysisViewTablet getControlsView() {
//		return controlsView;
//	}
//
//	@Override
//	protected void setControlsView(View controlsView) {
//		this.controlsView = (ControlsAnalysisViewTablet) controlsView;
//	}
}
