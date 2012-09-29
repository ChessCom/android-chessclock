package com.chess.lcc.android.interfaces;

/**
 * @author alien_roger
 * @created 15.06.12
 * @modified 15.06.12
 */
public interface LiveChessClientEventListenerFace {
	void onConnecting();

    void onConnectionEstablished();

	/**
	 * This method invoked in non UI thread so be wise using screen updates here.
	 * @param message
	 */
    void onConnectionFailure(String message);

	/**
	 * This method invoked when authKey has expired on server, so we may re-login user with username/pass or via facebook
	 * To re-login via facebook we should normally clear FacebookSession
	 */
    void onSessionExpired(String message);

	void onObsoleteProtocolVersion();

    void onConnectionBlocked(boolean blocked);

    void onFriendsStatusChanged();

	void onAdminAnnounce(String message);
}
