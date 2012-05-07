package com.chess.backend.interfaces;

import actionbarcompat.ActionBarHelper;
import com.chess.ui.core.CoreActivityActionBar;

/**
 * ChessUpdateListener class
 *
 * @author alien_roger
 * @created at: 07.05.12 5:26
 */
public abstract class ChessUpdateListener extends AbstractUpdateListener<String>{
	private ActionBarHelper actionBarHelper;

	public ChessUpdateListener(CoreActivityActionBar coreActivityActionBar) {
		super(coreActivityActionBar);
		actionBarHelper = coreActivityActionBar.provideActionBarHelper();
	}

	@Override
	public void showProgress(boolean show) {
		actionBarHelper.setRefreshActionItemState(show);
	}
}
