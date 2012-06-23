package com.chess.lcc.android.interfaces;

/**
 * LccEventListener class
 *
 * @author alien_roger
 * @created at: 24.05.12 21:47
 */
public interface LccEventListener {
	void onGameRefresh();

	void setWhitePlayerTimer(String timer);

	void setBlackPlayerTimer(String timer);
}
