package com.chess.backend.interfaces;

import com.chess.ui.activities.CoreActivityActionBar2;

/**
 * ChessUpdateListener class
 *
 * @author alien_roger
 * @created at: 07.05.12 5:26
 */
public abstract class ChessUpdateListener2 extends ActionBarUpdateListener<String> {
	public ChessUpdateListener2(CoreActivityActionBar2 coreActivityActionBar) {
		super(coreActivityActionBar);
	}
}
