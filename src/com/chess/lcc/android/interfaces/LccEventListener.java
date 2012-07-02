package com.chess.lcc.android.interfaces;

import com.chess.model.GameItem;

/**
 * LccEventListener class
 *
 * @author alien_roger
 * @created at: 24.05.12 21:47
 */
public interface LccEventListener {
    void setWhitePlayerTimer(String timer);

    void setBlackPlayerTimer(String timer);

	void onGameRefresh(GameItem newGame);

    void onDrawOffered(String drawOfferUsername);

    void onGameEnd(String gameEndMessage);
}
