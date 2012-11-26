package com.chess.backend.entity;

/**
 * TacticsDataHolder class
 *
 * @author alien_roger
 * @created at: 22.10.12 4:27
 */
public class TacticsDataHolder {
	private static TacticsDataHolder ourInstance = new TacticsDataHolder();

	private boolean tacticLimitReached;

	public static TacticsDataHolder getInstance() {
		return ourInstance;
	}

	public boolean isTacticLimitReached() {
		return tacticLimitReached;
	}

	public void setTacticLimitReached(boolean tacticLimitReached) {
		this.tacticLimitReached = tacticLimitReached;
	}

	public static void reset() {
		ourInstance = new TacticsDataHolder();
	}

}
