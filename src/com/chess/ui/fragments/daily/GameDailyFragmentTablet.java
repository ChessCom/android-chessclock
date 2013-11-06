package com.chess.ui.fragments.daily;

import android.os.Bundle;
import android.view.View;
import com.chess.ui.views.game_controls.ControlsDailyViewTablet;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.11.13
 * Time: 18:42
 */
public class GameDailyFragmentTablet extends GameDailyFragment {

	private ControlsDailyViewTablet controlsView;

	public GameDailyFragmentTablet() {
	}

	public static GameDailyFragmentTablet createInstance(long gameId, String username) {
		GameDailyFragmentTablet fragment = new GameDailyFragmentTablet();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		arguments.putString(USERNAME, username);
		fragment.setArguments(arguments);

		return fragment;
	}

	public static GameDailyFragmentTablet createInstance(long gameId) {
		GameDailyFragmentTablet fragment = new GameDailyFragmentTablet();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	protected ControlsDailyViewTablet getControlsView() {
		return controlsView;
	}

	@Override
	protected void setControlsView(View controlsView) {
		this.controlsView = (ControlsDailyViewTablet) controlsView;
	}



}
