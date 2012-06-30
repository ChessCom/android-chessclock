package com.chess.backend.entity;

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
	private boolean noInternet = false;
	private boolean offline = false;
	private boolean acceptdraw = false;
	private boolean liveChess;
	private boolean pendingTacticsLoad;

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

	public boolean isNoInternet() {
		return noInternet;
	}

	public void setNoInternet(boolean noInternet) {
		this.noInternet = noInternet;
	}

	public boolean isOffline() {
		return offline;
	}

	public void setOffline(boolean offline) {
		this.offline = offline;
	}

	public boolean isAcceptdraw() {
		return acceptdraw;
	}

	public void setAcceptdraw(boolean acceptdraw) {
		this.acceptdraw = acceptdraw;
	}

	public void setPendingTacticsLoad(boolean pendingTacticsLoad) {
		this.pendingTacticsLoad = pendingTacticsLoad;
	}

	public boolean isPendingTacticsLoad() {
		return pendingTacticsLoad;
	}
}
