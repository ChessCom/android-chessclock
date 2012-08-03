package com.chess.backend.entity;

import com.chess.model.TacticItem;
import com.chess.model.TacticResultItem;

import java.util.List;

/**
 * DataHolder class
 *
 * @author alien_roger
 * @created at: 26.04.12 6:11
 */
public class DataHolder {
	private static DataHolder ourInstance = new DataHolder();

	public static String APP_ID = "2427617054";

	private boolean guest = false;
	private boolean offline = false;
	private boolean acceptDraw = false;
	private boolean liveChess;
	private boolean pendingTacticsLoad;
	private TacticResultItem tacticResultItem;

	// Singletones for Tactics mode
	private List<TacticItem> tacticsBatch;
	private TacticItem tactic;
	private int currentTacticProblem = 0;


	private DataHolder() {}

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

	public void setPendingTacticsLoad(boolean pendingTacticsLoad) {
		this.pendingTacticsLoad = pendingTacticsLoad;
	}

	public boolean isPendingTacticsLoad() {
		return pendingTacticsLoad;
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
}
