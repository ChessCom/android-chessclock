package com.chess.lcc.android;

import com.chess.live.client.AdminEventListener;
import com.chess.live.client.LiveChessClient;
import com.chess.live.client.User;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 19.07.12
 * Time: 21:38
 */
public class LccAdminEventListener implements AdminEventListener {

	private static final String TAG = "LCCLOG-ADMIN";

	@Override
	public void onAdminMessageReceived(User sender, User target,
									   LiveChessClient.AdminMessageType messageType, String reason, String message, Long period) {
//		LogMe.dl(TAG, "onAdminMessageReceived: sender=" + (sender != null ? sender.getUsername() : null) + ", "
//				+ "targetedUser=" + target.getUsername() + ", messageType=" + messageType + ", reason=" + reason + ", "
//				+ "message=" + message);
	}

	@Override
	public void onServerShutdownAlertReceived(User sender, String message) {
//		LogMe.dl(TAG, "onServerShutdownAlertReceived: sender=" + (sender != null ? sender.getUsername() : null)
//				+ ", message=" + message);
	}

	@Override
	public void onServerMaintenanceAlertReceived(User sender, String codeMessage) {
//		LogMe.dl(TAG, "onServerMaintenanceAlertReceived: sender=" + (sender != null ? sender.getUsername() : null) + ", codeMessage=" + codeMessage);
	}

	@Override
	public void onHotConfigPropertySet(User user, String s, String s2) {
	}
}
