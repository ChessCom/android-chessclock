package com.chess.ui.fragments.live;

import android.os.Bundle;
import android.view.View;
import com.chess.ui.views.game_controls.ControlsDailyViewTablet;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 10:23
 */
public class GameLiveArchiveFragmentTablet extends GameLiveArchiveFragment {

	private ControlsDailyViewTablet controlsView;

	public GameLiveArchiveFragmentTablet() { }

	public static GameLiveArchiveFragmentTablet createInstance(long gameId) {
		GameLiveArchiveFragmentTablet fragment = new GameLiveArchiveFragmentTablet();
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
