package com.chess.backend.entity;

import android.util.Log;
import com.chess.model.GamePlayingItem;
import com.chess.ui.engine.ChessBoardLive;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.engine.ChessBoardTactics;

import java.util.ArrayList;
import java.util.List;

/**
 * DataHolder class
 *
 * @author alien_roger
 * @created at: 26.04.12 6:11
 */
public class DataHolder { // Shouldn't be used as a data holder due unreliable use in context of Android Lifecycle
	private static DataHolder ourInstance = new DataHolder();

//	private boolean guest; // TODO should be stored as a persistent state
	private boolean acceptDraw; // TODO should be stored
	private boolean liveChess; // TODO should be stored as a persistent state
	private boolean isAdsLoading;  // TODO should be tied to adFetcher process lifecycle


	// Echess mode game variables
	private final List<GamePlayingItem> playingGamesList;  // will be re-created every time user open the game. If it's killed, means we are not in the game.

	/**
	 * This list holds the info about last received notification to prevent overflow
	 * with the same move notification comes from server. In case the data can be killed,
	 * we might not care much as notifications comes in very short time period(in few seconds).
	 */
	private List<LastMoveInfoItem> lastMoveInfoItems; // TODO should be saved in store and tied to GCM usage


	private DataHolder() {
		playingGamesList = new ArrayList<GamePlayingItem>();
		lastMoveInfoItems = new ArrayList<LastMoveInfoItem>();
	}

	public static DataHolder getInstance() {
		return ourInstance;
	}

	public boolean isLiveChess() {
		return liveChess;
	}

	public void setLiveChess(boolean liveChess) {
		this.liveChess = liveChess;
	}



	public boolean isAcceptDraw() { // will be droped after onResume
		return acceptDraw;
	}

	public void setAcceptDraw(boolean acceptDraw) {
		this.acceptDraw = acceptDraw;
	}

	public boolean isAdsLoading() {
		return isAdsLoading;
	}

	public void setAdsLoading(boolean adsLoading) {
		isAdsLoading = adsLoading;
	}



	public void reset() {
		ourInstance = new DataHolder();
		ChessBoardTactics.resetInstance();
		ChessBoardLive.resetInstance();
		ChessBoardOnline.resetInstance();
	}

	/**
	 * Checks if game with this Id is currently open to the user
	 * @param gameId id of the game
	 * @return
	 */
	public synchronized boolean inOnlineGame(long gameId) {
		synchronized (playingGamesList) {
			for (GamePlayingItem gamePlayingItem : playingGamesList) {
				Log.d("TEST", " in game = " + gamePlayingItem.getGameId());
			}

			for (GamePlayingItem gamePlayingItem : playingGamesList) {
				if (gamePlayingItem.getGameId() == gameId){
					return gamePlayingItem.isBoardOpen();
				}
			}
		}

		return false;
	}

	/**
	 *  Set flag for notifications to avoid every move notification
	 * @param gameId id of the game
	 * @param gameOpen flag that shows if current game board is opened to user
	 */
	public void setInOnlineGame(long gameId, boolean gameOpen) {
		synchronized (playingGamesList) {
			for (GamePlayingItem gamePlayingItem : playingGamesList) {
				if(gamePlayingItem.getGameId() == gameId){
					gamePlayingItem.setBoardOpen(gameOpen);
					return;
				}
			}

			GamePlayingItem newGameItem = new GamePlayingItem();
			newGameItem.setGameId(gameId);
			newGameItem.setBoardOpen(gameOpen);
			playingGamesList.add(newGameItem);
		}
	}


	public List<LastMoveInfoItem> getLastMoveInfoItems() {
		return lastMoveInfoItems;
	}

	public void addLastMoveInfo(LastMoveInfoItem lastMoveInfoItem) {
		synchronized (lastMoveInfoItems) {
			lastMoveInfoItems.add(lastMoveInfoItem);
		}
	}
}
