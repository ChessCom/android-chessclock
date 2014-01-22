package com.chess.lcc.android;

import com.chess.R;
import com.chess.live.client.*;
import com.chess.utilities.LogMe;

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
		LogMe.dl(TAG, "Another client entered: user=" + user.getUsername());
		String message = lccHelper.getContext().getString(R.string.account_error) + " " +
				lccHelper.getContext().getString(R.string.another_login_detected);

		lccHelper.onOtherClientEntered(message);
	}

	@Override
	public void onConnectionEstablished(User user, UserSettings settings, ServerStats stats) {
		lccHelper.setUser(user);
		LogMe.dl(TAG, "onConnectionEstablished: client=" + lccHelper.getClientId());
		lccHelper.setConnected(true);
		lccHelper.setFriends(settings.getFriends());
		lccHelper.storeBlockedUsers(settings.getBlockedUsers(), settings.getBlockingUsers());
		lccHelper.clearPausedEvents();
		lccHelper.stopConnectionTimer();

		LogMe.dl(TAG, "User has been connected: name=" + user.getUsername() + ", authKey=" + user.getAuthKey() + ", user=" + user);
	}

	@Override
	public void onSettingsChanged(User user, UserSettings settings) {
		LogMe.dl(TAG, "onSettingsChanged");
		lccHelper.setFriends(settings.getFriends());
		lccHelper.storeBlockedUsers(settings.getBlockedUsers(), settings.getBlockingUsers());
	}

	@Override
	public void onConnectionFailure(User user, String message, FailureDetails details, Throwable throwable) {
		LogMe.dl(TAG, "User connection failure: " + message + ", details=" + details);

		lccHelper.processConnectionFailure(details);
	}

	@Override
	public void onConnectionLost(User user, String message, Throwable throwable) {
		LogMe.dl(TAG, "Connection Lost, with message = " + message + ", client=" + lccHelper.getClientId());
		//LogMe.dl(TAG, "Connection Lost: isNetworkAvailable=" + AppUtils.isNetworkAvailable(lccHelper.getContext()));
		lccHelper.setConnected(false);
	}

	@Override
	public void onConnectionReestablished(User user, UserSettings userSettings, ServerStats serverStats) {
		LogMe.dl(TAG, "onConnectionReestablished:" + " lccClient=" + lccHelper.getClientId());
		lccHelper.clearChallenges();
		lccHelper.clearOwnChallenges();
		lccHelper.clearSeeks();
		lccHelper.clearGames();
		lccHelper.setCurrentGameId(null);
		lccHelper.setConnected(true);

		lccHelper.setFriends(userSettings.getFriends());
		lccHelper.storeBlockedUsers(userSettings.getBlockedUsers(), userSettings.getBlockingUsers());

		lccHelper.clearPausedEvents();

		lccHelper.stopConnectionTimer();
	}

	@Override
	public void onPublishFailed(User user, Throwable th) {
		LogMe.dl(TAG, "onPublishFailed");
	}

	@Override
	public void onConnectionRestored(User arg0) {
		LogMe.dl(TAG, "Connection Restored:" + " lccClient=" + lccHelper.getClientId());
		lccHelper.setConnected(true);
	}

	@Override
	public void onObsoleteProtocolVersion(User user, String serverProtocolVersion, String clientProtocolVersion) {
		LogMe.dl(TAG, "Protocol version is obsolete (serverProtocolVersion=" + serverProtocolVersion + ", clientProtocolVersion=" +
				clientProtocolVersion + ")");
		lccHelper.onObsoleteProtocolVersion();
	}

	@Override
	public void onLagInfoReceived(User user, Long aLong) {
		// todo: UPDATELCC
	}

	@Override
	public void onKicked(User user, String reason, String message, Long period) {
		LogMe.dl(TAG, "The client kicked: " + user.getUsername() + ", reason=" + reason +
				", message=" + message + ", period=" + period);

		lccHelper.setConnected(false);
		lccHelper.processKicked();
	}
}