package com.chess.ui.fragments.tactics;

import android.view.View;
import com.chess.ui.views.game_controls.ControlsTacticsView;
import com.chess.ui.views.game_controls.ControlsTacticsViewTablet;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 19:14
 */
public class GameTacticsFragmentTablet extends GameTacticsFragment {

	private ControlsTacticsViewTablet controlsView;

	@Override
	protected ControlsTacticsView getControlsView() {
		return controlsView;
	}

	@Override
	protected void setControlsView(View controlsView) {
		this.controlsView = (ControlsTacticsViewTablet) controlsView;
	}
}
