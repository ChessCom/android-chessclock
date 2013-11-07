package com.chess.ui.fragments.daily;

import android.os.Bundle;
import android.view.View;
import com.chess.ui.views.game_controls.ControlsDailyViewTablet;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 9:13
 */
public class GameDailyFinishedFragmentTablet extends GameDailyFinishedFragment {

	private ControlsDailyViewTablet controlsView;

	public GameDailyFinishedFragmentTablet() { }

	public static GameDailyFinishedFragmentTablet createInstance(long gameId, String username) {
		GameDailyFinishedFragmentTablet fragment = new GameDailyFinishedFragmentTablet();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		arguments.putString(USERNAME, username);
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

	@Override
	public void switch2Analysis() {
		showSubmitButtonsLay(false);

		getActivityFace().openFragment(GameDailyAnalysisFragmentTablet.createInstance(gameId, username));
	}
}
