package com.chess.lcc.android;

import android.util.Log;
import com.chess.live.client.AdminEventListener;
import com.chess.live.client.LiveChessClient;
import com.chess.live.client.User;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 19.07.12
 * Time: 21:38
 * To change this template use File | Settings | File Templates.
 */
public class LccAdminEventListener implements AdminEventListener {

	private static final String TAG = "LCCLOG-ADMIN";

	public void onAdminMessageReceived(User sender, User target,
									   LiveChessClient.AdminMessageType messageType, String reason, String message, Long period) {
		Log.d(TAG, "onAdminMessageReceived: sender=" + (sender != null ? sender.getUsername() : null) + ", "
				+ "targetedUser=" + target.getUsername() + ", messageType=" + messageType + ", reason=" + reason + ", "
				+ "message=" + message);
	}

	public void onServerShutdownAlertReceived(User sender, String message) {
		Log.d(TAG, "onServerShutdownAlertReceived: sender=" + (sender != null ? sender.getUsername() : null)
				+ ", message=" + message);
	}

	public void onServerMaintenanceAlertReceived(User sender, String codeMessage) {
		Log.d(TAG, "onServerMaintenanceAlertReceived: sender=" + (sender != null ? sender.getUsername() : null) + ", codeMessage=" + codeMessage);
	}
}
