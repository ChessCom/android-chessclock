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
	private LiveConnectionHelper liveConnectionHelper;
	private LccHelper lccHelper;

	public LccConnectionListener(LiveConnectionHelper liveConnectionHelper) {
		this.liveConnectionHelper = liveConnectionHelper;
		lccHelper = liveConnectionHelper.getLccHelper();
	}

	@Override
	public void onOtherClientEntered(User user) {
		LogMe.dl(TAG, "Another client entered: user=" + user.getUsername() + " client=" + liveConnectionHelper.getClientId());
		String message = lccHelper.getContext().getString(R.string.account_error) + " " +
				lccHelper.getContext().getString(R.string.another_login_detected);

		liveConnectionHelper.onOtherClientEntered(message);
	}

	@Override
	public void onConnectionEstablished(User user, UserSettings settings, ServerStats stats) {
		LogMe.dl(TAG, "onConnectionEstablished: client=" + liveConnectionHelper.getClientId());
		lccHelper.setUser(user);
		liveConnectionHelper.stopPingLiveTimer();
		liveConnectionHelper.setConnected(true);
		liveConnectionHelper.setConnecting(false);
		liveConnectionHelper.clearPausedEvents();
		lccHelper.setFriends(settings.getFriends());
		lccHelper.storeBlockedUsers(settings.getBlockedUsers(), settings.getBlockingUsers());

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
		LogMe.dl(TAG, "User connection failure: " + message + ", details=" + details + ", client=" + liveConnectionHelper.getClientId());

		liveConnectionHelper.pingLive();
		//lccHelper.stopPingLiveTimer();

		liveConnectionHelper.processConnectionFailure(details);
	}

	@Override
	public void onConnectionLost(User user, String message, Throwable throwable) {
		LogMe.dl(TAG, "Connection Lost, with message = " + message + ", client=" + liveConnectionHelper.getClientId());
		//LogMe.dl(TAG, "Connection Lost: isNetworkAvailable=" + AppUtils.isNetworkAvailable(lccHelper.getContext()));

		liveConnectionHelper.pingLive();
		//lccHelper.stopPingLiveTimer();

		liveConnectionHelper.setConnected(false);
		liveConnectionHelper.setConnecting(true);
	}

	@Override
	public void onConnectionReestablished(User user, UserSettings userSettings, ServerStats serverStats) {
		LogMe.dl(TAG, "onConnectionReestablished:" + " lccClient=" + liveConnectionHelper.getClientId());

		synchronized (LccHelper.GAME_SYNC_LOCK) {
			lccHelper.setUser(user);
			lccHelper.clearChallenges();
			lccHelper.clearOwnChallenges();
			lccHelper.clearSeeks();
			lccHelper.clearGames();
			lccHelper.setCurrentGameId(null);
			liveConnectionHelper.stopPingLiveTimer();
			liveConnectionHelper.setConnected(true);
			liveConnectionHelper.setConnecting(false);

			lccHelper.setFriends(userSettings.getFriends());
			lccHelper.storeBlockedUsers(userSettings.getBlockedUsers(), userSettings.getBlockingUsers());

			liveConnectionHelper.clearPausedEvents();
		}
	}

	@Override
	public void onPublishFailed(User user, Throwable th) {
		LogMe.dl(TAG, "onPublishFailed");
	}

	@Override
	public void onConnectionRestored(User arg0) {
		LogMe.dl(TAG, "Connection Restored:" + " lccClient=" + liveConnectionHelper.getClientId());
		liveConnectionHelper.stopPingLiveTimer();
		liveConnectionHelper.setConnected(true);
		liveConnectionHelper.setConnecting(false);
	}

	@Override
	public void onObsoleteProtocolVersion(User user, String serverProtocolVersion, String clientProtocolVersion) {
		LogMe.dl(TAG, "Protocol version is obsolete (serverProtocolVersion=" + serverProtocolVersion + ", clientProtocolVersion=" +
				clientProtocolVersion + ")");
		liveConnectionHelper.onObsoleteProtocolVersion();
	}

	@Override
	public void onLagInfoReceived(User user, Long aLong) {
		// todo: UPDATELCC
	}

	@Override
	public void onKicked(User user, String reason, String message, Long period) {
		LogMe.dl(TAG, "The client kicked: " + user.getUsername() + ", reason=" + reason +
				", message=" + message + ", period=" + period);
		liveConnectionHelper.stopPingLiveTimer();

		liveConnectionHelper.processKicked();
	}
}