package com.chess.ui.fragments.daily;

import android.os.Bundle;
import android.view.View;
import com.chess.model.GameAnalysisItem;
import com.chess.model.GameExplorerItem;
import com.chess.ui.fragments.explorer.GameExplorerFragment;
import com.chess.ui.fragments.game.GameAnalyzeFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.03.14
 * Time: 7:28
 */
public class GameAnalyzeDailyFragment extends GameAnalyzeFragment {

	public GameAnalyzeDailyFragment() {

	}

	public static GameAnalyzeDailyFragment createInstance(GameAnalysisItem analysisItem, long gameId) {
		GameAnalyzeDailyFragment fragment = new GameAnalyzeDailyFragment();
		Bundle arguments = new Bundle();
		arguments.putParcelable(GAME_ITEM, analysisItem);
		arguments.putLong(GAME_ID, gameId);
		fragment.setArguments(arguments);

		return fragment;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			gameId = getArguments().getLong(GAME_ID);
		} else {
			gameId = savedInstanceState.getLong(GAME_ID);
		}
	}


	@Override
	public void openNotes() {
		getActivityFace().openFragment(DailyNotesFragment.createInstance(gameId));
	}

	@Override
	public void showExplorer() {
		GameExplorerItem explorerItem = new GameExplorerItem();
		explorerItem.setFen(getBoardFace().generateFullFen());
		explorerItem.setMovesList(getBoardFace().getMoveListSAN());
		explorerItem.setGameType(analysisItem.getGameType());
		explorerItem.setUserPlayWhite(userPlayWhite);

		getActivityFace().openFragment(GameExplorerFragment.createInstance(explorerItem));
	}


	@Override
	protected void widgetsInit(View view) {
		super.widgetsInit(view);
		controlsView.showDailyControls(true);
	}
}
