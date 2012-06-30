package com.chess.backend.interfaces;

import com.chess.ui.activities.CoreActivityActionBar;

/**
 * ChessUpdateListener class
 *
 * @author alien_roger
 * @created at: 28.06.12 21:51
 */
public abstract class ChessUpdateListener extends ActionBarUpdateListener<String> {
	public ChessUpdateListener(CoreActivityActionBar coreActivityActionBar) {
		super(coreActivityActionBar);
	}
}
