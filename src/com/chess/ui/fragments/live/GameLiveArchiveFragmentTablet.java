package com.chess.ui.fragments.live;

import android.os.Bundle;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 10:23
 */
public class GameLiveArchiveFragmentTablet extends GameLiveArchiveFragment {

	public GameLiveArchiveFragmentTablet() { }

	public static GameLiveArchiveFragmentTablet createInstance(long gameId) {
		GameLiveArchiveFragmentTablet fragment = new GameLiveArchiveFragmentTablet();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		fragment.setArguments(arguments);

		return fragment;
	}

}
