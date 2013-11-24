package com.chess.ui.fragments.live;

import android.os.Bundle;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 10:15
 */
public class GameLiveFragmentTablet extends GameLiveFragment {

	public GameLiveFragmentTablet() { }

	public static GameLiveFragmentTablet createInstance(long id) {
		GameLiveFragmentTablet fragment = new GameLiveFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putLong(GAME_ID, id);
		fragment.setArguments(bundle);
		return fragment;
	}

}
