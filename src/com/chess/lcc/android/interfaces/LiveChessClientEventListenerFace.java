package com.chess.lcc.android.interfaces;

/**
 * @author Alexey Schekin (schekin@azoft.com)
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

	void onObsoleteProtocolVersion();

    void onConnectionBlocked();

    void onFriendsStatusChanged();
}
