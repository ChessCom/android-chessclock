package com.chess.backend.interfaces;

import actionbarcompat.ActionBarHelper;
import com.chess.ui.activities.CoreActivityActionBar2;

/**
 * ChessUpdateListener class
 *
 * @author alien_roger
 * @created at: 07.05.12 5:26
 */
public abstract class ActionBarUpdateListener<T> extends AbstractUpdateListener<T> {
	private ActionBarHelper actionBarHelper;

	public ActionBarUpdateListener(CoreActivityActionBar2 coreActivityActionBar) {
		super(coreActivityActionBar);
		actionBarHelper = coreActivityActionBar.provideActionBarHelper();
	}

	@Override
	public void showProgress(boolean show) {
		actionBarHelper.setRefreshActionItemState(show);
	}
}
