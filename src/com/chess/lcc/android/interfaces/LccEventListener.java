package com.chess.lcc.android.interfaces;

import com.chess.model.GameLiveItem;

/**
 * LccEventListener class
 *
 * @author alien_roger
 * @created at: 24.05.12 21:47
 */
public interface LccEventListener {
    void setWhitePlayerTimer(String timer);

    void setBlackPlayerTimer(String timer);

	void onGameRefresh(GameLiveItem gameItem);

    void onDrawOffered(String drawOfferUsername);

    void onGameEnd(String gameEndMessage);
	
	//void onInform(String title, String message);

	void onGameRecreate();
}
