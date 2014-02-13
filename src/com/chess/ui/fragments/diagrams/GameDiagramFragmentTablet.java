package com.chess.ui.fragments.diagrams;

import android.os.Bundle;
import com.chess.model.GameDiagramItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.11.13
 * Time: 6:08
 */
public class GameDiagramFragmentTablet extends GameDiagramFragment {

	public GameDiagramFragmentTablet() {
	}

	public static GameDiagramFragmentTablet createInstance(GameDiagramItem analysisItem) {
		GameDiagramFragmentTablet fragment = new GameDiagramFragmentTablet();
		Bundle arguments = new Bundle();
		arguments.putParcelable(GAME_ITEM, analysisItem);
		fragment.setArguments(arguments);

		return fragment;
	}

}
