package com.chess.backend.entity;

import com.chess.model.TacticItem;
import com.chess.model.TacticResultItem;

import java.util.ArrayList;
import java.util.List;

/**
 * TacticsDataHolder class
 *
 * @author alien_roger
 * @created at: 22.10.12 4:27
 */
public class TacticsDataHolder {
	private static TacticsDataHolder ourInstance = new TacticsDataHolder();


	private List<TacticItem> tacticsBatch;
	private TacticItem tactic;
	private TacticResultItem tacticResultItem;
	private int currentTacticProblem;
	private boolean tacticLimitReached;
	private List<Long> showedTacticsIds;


	public static TacticsDataHolder getInstance() {
		return ourInstance;
	}

	private TacticsDataHolder() {
		showedTacticsIds = new ArrayList<Long>();
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
	public void addShowedTacticId(long id) {
		if(!showedTacticsIds.contains(id))
			showedTacticsIds.add(id);
	}

	/**
	 * Check if user used that tactic for showAnswer option
	 * @param id
	 * @return
	 */
	public boolean tacticWasShowed(long id){
		return showedTacticsIds.contains(id);
	}

	public static void reset() {
		ourInstance = new TacticsDataHolder();
	}

}
