package com.chess.model;

import android.util.SparseIntArray;
import com.chess.backend.gcm.LastMoveInfoItem;
import com.chess.ui.engine.ChessBoardComp;
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
public class DataHolder {
	// Shouldn't be used as a data holder due unreliable use in context of Android Lifecycle
	public static final int SETTINGS = 0;
	private static DataHolder ourInstance = new DataHolder();
	private final SparseIntArray positionsArray;

	private boolean isAdsLoading;

	// Echess mode game variables
	private final GamePlayingItem playingGameItem;  // will be re-created every time user open the game. If it's killed, means we are not in the game.

	/**
	 * This list holds the info about last received notification to prevent overflow
	 * with the same move notification comes from server. In case the data can be killed,
	 * we might not care much as notifications comes in very short time period(in few seconds).
	 */
	private final List<LastMoveInfoItem> lastMoveInfoItems; // TODO should be saved in store and tied to GCM usage
	private boolean isMainActivityVisible;
	private boolean liveGameOpened;
	private boolean performingRelogin;


	private DataHolder() {
		playingGameItem = new GamePlayingItem();
		lastMoveInfoItems = new ArrayList<LastMoveInfoItem>();
		positionsArray = new SparseIntArray();
	}

	public static DataHolder getInstance() {
		return ourInstance;
	}

	public boolean isAdsLoading() {
		return isAdsLoading;
	}

	public void setAdsLoading(boolean adsLoading) {
		isAdsLoading = adsLoading;
	}

	public static void reset() {
		ourInstance = new DataHolder();
		ChessBoardTactics.resetInstance();
		ChessBoardLive.resetInstance();
		ChessBoardOnline.resetInstance();
		ChessBoardComp.resetInstance();
	}

	/**
	 * Checks if game with this Id is currently open to the user
	 *
	 * @param gameId id of the game
	 * @return true if gameBoard is open now
	 */
	public synchronized boolean inOnlineGame(long gameId) {
		synchronized (playingGameItem) {
			if (playingGameItem.getGameId() == gameId) {
				return playingGameItem.isBoardOpen();
			}
		}

		return false;
	}

	/**
	 * Set flag for notifications to avoid every move notification
	 *
	 * @param gameId   id of the game
	 * @param gameOpen flag that shows if current game board is opened to user
	 */
	public void setInDailyGame(long gameId, boolean gameOpen) {
		synchronized (playingGameItem) {
			playingGameItem.setGameId(gameId);
			playingGameItem.setBoardOpen(gameOpen);
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

	public synchronized void setMainActivityVisible(boolean visible) {
		isMainActivityVisible = visible;
	}

	public synchronized boolean isMainActivityVisible() {
		return isMainActivityVisible;
	}

	public void setSelectedPositionForId(int id, int position) {
		positionsArray.put(id, position);
	}

	public int getSelectedPositionForId(int id) {
		return positionsArray.get(id);
	}

	public boolean isLiveGameOpened() {
		return liveGameOpened;
	}

	public void setLiveGameOpened(boolean liveGameOpened) {
		this.liveGameOpened = liveGameOpened;
	}

	public boolean isPerformingRelogin() {
		return performingRelogin;
	}

	public void setPerformingRelogin(boolean performingRelogin) {
		this.performingRelogin = performingRelogin;
	}
}
