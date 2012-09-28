package com.chess.backend.entity;

import com.chess.model.GamePlayingItem;
import com.chess.model.TacticItem;
import com.chess.model.TacticResultItem;
import com.chess.ui.engine.ChessBoardLive;
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
	private static DataHolder ourInstance = new DataHolder();

	private boolean guest = false;
	private boolean offline = false;
	private boolean acceptDraw = false;
	private boolean liveChess;
	private boolean isAdsLoading;
	private TacticResultItem tacticResultItem;

	// Singletons for Tactics mode
	private List<TacticItem> tacticsBatch;
	private TacticItem tactic;
	private int currentTacticProblem = 0;
	private boolean tacticLimitReached;
	private List<String> showedTacticsIds;
	private List<GamePlayingItem> playingGamesList;


	private DataHolder() {
		showedTacticsIds = new ArrayList<String>();
		playingGamesList = new ArrayList<GamePlayingItem>();
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

	public boolean isGuest() {
		return guest;
	}

	public void setGuest(boolean guest) {
		this.guest = guest;
	}

	public boolean isOffline() {
		return offline;
	}

	public void setOffline(boolean offline) {
		this.offline = offline;
	}

	public boolean isAcceptDraw() {
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

	public TacticItem getTactic() {
		return tactic;
	}

	public void setTactic(TacticItem tactic) {
		this.tactic = tactic;
	}

	public List<TacticItem> getTacticsBatch() {
		return tacticsBatch;
	}

	public void setTacticsBatch(List<TacticItem> tacticsBatch) {
		this.tacticsBatch = tacticsBatch;
	}

	public int getCurrentTacticProblem() {
		return currentTacticProblem;
	}

	public void increaseCurrentTacticsProblem(){
		currentTacticProblem++;
	}

	public void setCurrentTacticProblem(int currentTacticProblem) {
		this.currentTacticProblem = currentTacticProblem;
	}

	public TacticResultItem getTacticResultItem() {
		return tacticResultItem;
	}

	public void setTacticResultItem(TacticResultItem tacticResultItem) {
		this.tacticResultItem = tacticResultItem;
	}

	public boolean isTacticLimitReached() {
		return tacticLimitReached;
	}

	public void setTacticLimitReached(boolean tacticLimitReached) {
		this.tacticLimitReached = tacticLimitReached;
	}


	/**
	 * Add id of showed answer tactic to prevent cheating
	 * @param id
	 */
	public void addShowedTacticId(String id) {
		if(!showedTacticsIds.contains(id))
			showedTacticsIds.add(id);
	}

	/**
	 * Check if user used that tactic for showAnswer option
	 * @param id
	 * @return
	 */
	public boolean tacticWasShowed(String id){
		return showedTacticsIds.contains(id);
	}

	public void reset() {
		ourInstance = new DataHolder();
		ChessBoardTactics.resetInstance();
		ChessBoardLive.resetInstance();
	}

	/**
	 * Checks if game with this Id is currently open to the user
	 * @param gameId id of the game
	 * @return
	 */
	public synchronized boolean inOnlineGame(long gameId) {
		for (GamePlayingItem gamePlayingItem : playingGamesList) {
			if (gamePlayingItem.getGameId() == gameId){
				return gamePlayingItem.isBoardOpen();
			}
		}
		return false;
	}

	/**
	 *  Set flag for notifications to avoid every move notification
	 * @param gameId id of the game
	 * @param gameOpen flag that shows if current game board is opened to user
	 */
	public synchronized void setInOnlineGame(long gameId, boolean gameOpen) {
		for (GamePlayingItem gamePlayingItem : playingGamesList) {
			if(gamePlayingItem.getGameId() == gameId){
				gamePlayingItem.setBoardOpen(gameOpen);
			} else { // add this game
				GamePlayingItem newGameItem = new GamePlayingItem();
				newGameItem.setGameId(gameId);
				newGameItem.setBoardOpen(gameOpen);
			}
		}
	}
}
