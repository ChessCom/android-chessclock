package com.chess.lcc.android;

import android.util.Log;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.live.client.*;

/**
 * Created by IntelliJ IDEA. User: Vova Date: 28.02.2010 Time: 15:50:16 To change this template use File | Settings |
 * File Templates.
 */
public class LccConnectionListener implements ConnectionListener {
	public static final String TAG = "LccLog-Connection";
	private LccHelper lccHelper;

	public LccConnectionListener(LccHelper lccHelper) {
		this.lccHelper = lccHelper;
	}

	@Override
	public void onOtherClientEntered(User user) {
		Log.d(TAG, "Another client entered: user=" + user.getUsername());
		String message = lccHelper.getContext().getString(R.string.account_error)
				+ lccHelper.getContext().getString(R.string.another_login_detected);

		lccHelper.setConnected(false);
		lccHelper.onOtherClientEntered(message);
	}

	@Override
	public void onConnectionEstablished(User user, UserSettings settings, ServerStats stats) {
        lccHelper.setUser(user);
		Log.d(TAG, "onConnectionEstablished: lccHelper = " + lccHelper);
		lccHelper.setConnected(true);
		lccHelper.setFriends(settings.getFriends());
		lccHelper.storeBlockedUsers(settings.getBlockedUsers(), settings.getBlockingUsers());
		lccHelper.clearPausedEvents();

		Log.d(TAG, "User has been connected: name=" + user.getUsername() + ", authKey=" + user.getAuthKey() + ", user=" + user);
	}

	@Override
	public void onSettingsChanged(User user, UserSettings settings) {
		Log.d(TAG, "onSettingsChanged");
		lccHelper.setFriends(settings.getFriends());
		lccHelper.storeBlockedUsers(settings.getBlockedUsers(), settings.getBlockingUsers());
	}

	@Override
	public void onConnectionFailure(User user, String message, FailureDetails details, Throwable throwable) {
		Log.d(TAG, "User connection failure:" + message + ", details=" + details);

		/*if (details ==  null) {
			lccHelper.setConnected(false);
			lccHelper.resetClient();
			lccHelper.runConnectTask(true);
		} else {
			lccHelper.processConnectionFailure(details);
		}*/

		lccHelper.processConnectionFailure(details);
	}

	@Override
	public void onConnectionLost(User user, String message, FailureDetails details, Throwable throwable) {
		String failureId = null;
		String comments = null;
		if (details != null) {
			failureId = details.getFailureId();
			comments = details.getComments();
		}
		Log.d(TAG, "Connection Lost, with message = " + message
                + " \nDetails: id = " + failureId + " comments = " + comments);
		lccHelper.setConnected(false);
	}

	@Override
	public void onConnectionReestablished(User arg0) {
		Log.d(TAG, "onConnectionReestablished");
		lccHelper.clearChallenges();
		lccHelper.clearOwnChallenges();
		lccHelper.clearSeeks();
		/*lccHelper.clearGames();
		lccHelper.setCurrentGameId(null)*/;
		lccHelper.setConnected(true);
		lccHelper.clearPausedEvents();
	}

	@Override
	public void onPublishFailed(User user, Throwable th) {
		Log.d(TAG, "onPublishFailed");
	}

	@Override
	public void onConnectionRestored(User arg0) {
		Log.d(TAG, "Connection Restored");
		lccHelper.setConnected(true);
	}

	@Override
	public void onObsoleteProtocolVersion(User user, String serverProtocolVersion, String clientProtocolVersion) {
		Log.d(TAG, "Protocol version is obsolete (serverProtocolVersion=" + serverProtocolVersion + ", clientProtocolVersion=" +
						clientProtocolVersion + StaticData.SYMBOL_RIGHT_PAR);
		lccHelper.onObsoleteProtocolVersion();
	}

	@Override
	public void onLagInfoReceived(User user, Long aLong) {
		// todo: UPDATELCC
	}

	@Override
	public void onKicked(User user, String reason, String message, Long period) {
		Log.d(TAG, "The client kicked: " + user.getUsername() + ", reason=" + reason +
				", message=" + message + ", period=" + period);

		lccHelper.setConnected(false);
		lccHelper.processConnectionFailure(reason, message);
	}
}
