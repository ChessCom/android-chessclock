package com.chess.backend.interfaces;

import actionbarcompat.ActionBarHelper;
import com.chess.ui.activities.CoreActivityActionBar;

/**
 * ChessUpdateListener class
 *
 * @author alien_roger
 * @created at: 07.05.12 5:26
 */
public abstract class ActionBarUpdateListener<T> extends AbstractUpdateListener<T> {
	private ActionBarHelper actionBarHelper;

	public ActionBarUpdateListener(CoreActivityActionBar coreActivityActionBar) {
		super(coreActivityActionBar);
		actionBarHelper = coreActivityActionBar.provideActionBarHelper();
	}

	@Override
	public void showProgress(boolean show) {
		if(actionBarHelper != null)
			actionBarHelper.setRefreshActionItemState(show);
	}
}
