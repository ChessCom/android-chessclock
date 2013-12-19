package com.chess.lcc.android.interfaces;

import com.chess.live.client.Game;
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

    void onGameEnd(Game game, String gameEndMessage);
	
	void onInform(String title, String message);

	void startGameFromService();

	void createSeek();

	void expireGame();

	void updateOpponentOnlineStatus(boolean online);

	void onChallengeRejected(String by);
}
