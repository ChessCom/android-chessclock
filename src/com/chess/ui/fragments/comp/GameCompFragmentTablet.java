package com.chess.ui.fragments.comp;

import android.os.Bundle;
import com.chess.ui.engine.configs.CompGameConfig;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 9:29
 */
public class GameCompFragmentTablet extends GameCompFragment {

//	private ControlsCompViewTablet controlsView;

	public GameCompFragmentTablet() {
		CompGameConfig config = new CompGameConfig.Builder().build();
		Bundle bundle = new Bundle();
		bundle.putParcelable(CONFIG, config);
		setArguments(bundle);
	}

	public static GameCompFragmentTablet createInstance(CompGameConfig config) {
		GameCompFragmentTablet fragment = new GameCompFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putParcelable(CONFIG, config);
		fragment.setArguments(bundle);
		return fragment;
	}

//	@Override
//	protected ControlsCompViewTablet getControlsView() {
//		return controlsView;
//	}
//
//	@Override
//	protected void setControlsView(View controlsView) {
//		this.controlsView = (ControlsCompViewTablet) controlsView;
//	}

}
