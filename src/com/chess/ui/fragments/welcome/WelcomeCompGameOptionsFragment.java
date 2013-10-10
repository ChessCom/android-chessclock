package com.chess.ui.fragments.welcome;

import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.fragments.comp.CompGameOptionsFragment;
import com.chess.ui.interfaces.FragmentTabsFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.08.13
 * Time: 7:53
 */
public class WelcomeCompGameOptionsFragment extends CompGameOptionsFragment {

	private FragmentTabsFace parentFace;

	public static WelcomeCompGameOptionsFragment createInstance(FragmentTabsFace parentFace) {
		WelcomeCompGameOptionsFragment fragment = new WelcomeCompGameOptionsFragment();
		fragment.parentFace = parentFace;
		return fragment;
	}

	@Override
	protected void startGame() {
		if (parentFace == null) {
			return;
		}

		ChessBoardComp.resetInstance();

		getAppData().clearSavedCompGame();

		// call config create to save default settings for vs comp game
		getNewCompGameConfig();

		parentFace.changeInternalFragment(WelcomeTabsFragment.GAME_FRAGMENT);
		getActivityFace().toggleRightMenu();
	}

}
