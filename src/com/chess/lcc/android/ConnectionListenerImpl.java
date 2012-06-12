package com.chess.lcc.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.live.client.*;

/**
 * Created by IntelliJ IDEA. User: Vova Date: 28.02.2010 Time: 15:50:16 To change this template use File | Settings |
 * File Templates.
 */
public class ConnectionListenerImpl implements ConnectionListener {
	private LccHolder lccHolder;

	public ConnectionListenerImpl(LccHolder lccHolder) {
		this.lccHolder = lccHolder;
	}

	public void onOtherClientEntered(User user) {
		Log.d("CONNECTION", "Another client entered: user=" + user.getUsername());
		lccHolder.getAndroidStuff().processOtherClientEntered();
	}

	public void onConnectionEstablished(User user, UserSettings settings, ServerStats stats) {
		lccHolder.setUser(user);
		lccHolder.setConnected(true);
		Log.d("TEST", "onConnectionEstablished, setConnected(true)");

		lccHolder.setConnectingInProgress(false);
		lccHolder.setFriends(settings.getFriends());
		lccHolder.storeBlockedUsers(settings.getBlockedUsers(), settings.getBlockingUsers());
		lccHolder.getClient().subscribeToChallengeEvents(lccHolder.getChallengeListener());
		lccHolder.getClient().subscribeToGameEvents(lccHolder.getGameListener());
		lccHolder.getClient().subscribeToChatEvents(lccHolder.getChatListener());
		//lccHolder.getClient().subscribeToChatEvents(lccHolder.getChatListener());
		/*lccHolder.getClient()
			  .subscribeToSeekList(LiveChessClient.SeekListOrderBy.Default, 1, lccHolder.getSeekListListener());*/

		lccHolder.getClient().subscribeToFriendStatusEvents(lccHolder.getFriendStatusListener());

		//lccHolder.getAndroidStuff().sendConnectionBroadcastIntent(true, 0);
		/*lccHolder.getAndroidStuff().getSharedDataEditor().putString("premium_status", StaticData.SYMBOL_EMPTY + user.getMembershipLevel());
			lccHolder.getAndroidStuff().getSharedDataEditor().commit();*/
		lccHolder.getAndroidStuff().closeLoggingInIndicator();

		final ConnectivityManager connectivityManager = (ConnectivityManager)
				lccHolder.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		lccHolder.setNetworkTypeName(activeNetworkInfo.getTypeName());

		Log.d("CONNECTION", "User has been connected: _user=" + user.getUsername() + ", authKey=" + user.getAuthKey());
	}

	@Override
	public void onSettingsChanged(User user, UserSettings settings) {
		Log.d("CONNECTION", "onSettingsChanged");
		lccHolder.setFriends(settings.getFriends());
		lccHolder.storeBlockedUsers(settings.getBlockedUsers(), settings.getBlockingUsers());
	}

	@Override
	public void onConnectionFailure(User user, String message, FailureDetails details, Throwable throwable) {
		Log.d("CONNECTION", "User connection failure:" + message + ", details=" + details);

		String detailsMessage = StaticData.SYMBOL_EMPTY;
		if (details != null) {
			lccHolder.setConnected(false);
			lccHolder.setConnectingInProgress(false);
			lccHolder.getAndroidStuff().closeLoggingInIndicator();

			switch (details) {
				case USER_KICKED: {
					detailsMessage = lccHolder.getContext().getString(R.string.lccFailedUpgrading);
					break;
				}
				case ACCOUNT_FAILED: {
					detailsMessage = "Account error. " + lccHolder.getContext().getString(R.string.lccFailedUnavailable);
					break;
				}
				case SERVER_STOPPED: {
					detailsMessage = "Server stopped. " + lccHolder.getContext().getString(R.string.lccFailedUnavailable);
					break;
				}
				default: {
					detailsMessage = message;
				}
			}
			lccHolder.getAndroidStuff().informAndExit(StaticData.SYMBOL_EMPTY, detailsMessage);
		} else {
			Log.d("CONNECTION", "User connection failure: IGNORING");
		}
	}

	@Override
	public void onConnectionLost(User arg0, String arg1, FailureDetails arg2,
								 Throwable arg3) {
		Log.d("CONNECTION", "Connection Lost");
		lccHolder.setConnected(false);
		lccHolder.setConnectingInProgress(true);
		lccHolder.getAndroidStuff().showReconnectingIndicator();
		lccHolder.getAndroidStuff().closeLoggingInIndicator();
	}

	@Override
	public void onConnectionReestablished(User arg0) {
		Log.d("CONNECTION", "onConnectionReestablished");
		//lccHolder.clearGames();
		lccHolder.clearChallenges();
		lccHolder.clearOwnChallenges();
		lccHolder.clearSeeks();
		lccHolder.setConnected(true);
		lccHolder.setConnectingInProgress(false);
		lccHolder.getClient().subscribeToChallengeEvents(lccHolder.getChallengeListener());
		lccHolder.getClient().subscribeToGameEvents(lccHolder.getGameListener());
		lccHolder.getClient().subscribeToChatEvents(lccHolder.getChatListener());
		//lccHolder.getClient().subscribeToChatEvents(lccHolder.getChatListener());
		/*lccHolder.getClient()
			  .subscribeToSeekList(LiveChessClient.SeekListOrderBy.Default, 1, lccHolder.getSeekListListener());*/
		lccHolder.getClient().subscribeToFriendStatusEvents(lccHolder.getFriendStatusListener());
		lccHolder.getAndroidStuff().closeReconnectingIndicator();
		lccHolder.getAndroidStuff().closeLoggingInIndicator();
	}

	@Override
	public void onPublishFailed(User user, Throwable th) {
		Log.d("CONNECTION", "onPublishFailed");
	}

	@Override
	public void onConnectionRestored(User arg0) {
		Log.d("CONNECTION", "Connection Restored");
		lccHolder.setConnected(true);
		lccHolder.setConnectingInProgress(false);
		lccHolder.getClient().subscribeToChallengeEvents(lccHolder.getChallengeListener());
		lccHolder.getClient().subscribeToGameEvents(lccHolder.getGameListener());
		lccHolder.getClient().subscribeToChatEvents(lccHolder.getChatListener());
		//lccHolder.clearGames();
		//lccHolder.getClient().subscribeToChatEvents(lccHolder.getChatListener());
		/*lccHolder.getClient()
			  .subscribeToSeekList(LiveChessClient.SeekListOrderBy.Default, 1, lccHolder.getSeekListListener());*/
		lccHolder.getClient().subscribeToFriendStatusEvents(lccHolder.getFriendStatusListener());
		lccHolder.getAndroidStuff().closeReconnectingIndicator();
		lccHolder.getAndroidStuff().closeLoggingInIndicator();
	}

	public void onObsoleteProtocolVersion(User user, String serverProtocolVersion, String clientProtocolVersion) {
		Log.d("CONNECTION",
				"Protocol version is obsolete (serverProtocolVersion=" + serverProtocolVersion + ", clientProtocolVersion=" +
						clientProtocolVersion + ")");
		lccHolder.getAndroidStuff().processObsoleteProtocolVersion();
	}

	public void onKicked(User user, String reason, String message) {
		Log.d("CONNECTION", "user kicked");
		lccHolder.getAndroidStuff().informAndExit(reason, "You have been kicked/banned");
		lccHolder.setNetworkTypeName(null);
	}

}
