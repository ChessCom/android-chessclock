package com.chess.lcc.android.interfaces;

/**
 * @author alien_roger
 * @created 15.06.12
 * @modified 15.06.12
 */
public interface LiveChessClientEventListener {
	//void onConnectionEstablished();

	/**
	 * This method invoked in non UI thread so be wise using screen updates here.
	 * @param message
	 */
    void onConnectionFailure(String message);

	void onObsoleteProtocolVersion();

    void onConnectionBlocked(boolean blocked);

	void onAdminAnnounce(String message);
}
