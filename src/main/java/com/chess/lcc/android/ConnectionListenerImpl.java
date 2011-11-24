package com.chess.lcc.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.chess.R;
import com.chess.live.client.*;

import android.util.Log;

/**
 * Created by IntelliJ IDEA. User: Vova Date: 28.02.2010 Time: 15:50:16 To change this template use File | Settings |
 * File Templates.
 */
public class ConnectionListenerImpl implements ConnectionListener
{
  private LccHolder lccHolder;

  public ConnectionListenerImpl(LccHolder lccHolder)
  {
    this.lccHolder = lccHolder;
  }

  public void onOtherClientEntered(User user)
  {
    Log.d("", "LCCLOG CONNECTION: Another client entered: user=" + user.getUsername());
    lccHolder.getAndroid().processOtherClientEntered();
  }

  public void onConnectionEstablished(User user, UserSettings settings, ServerStats stats)
  {
    lccHolder.setUser(user);
    lccHolder.setConnected(true);
    lccHolder.setConnectingInProgress(false);
    lccHolder.setSettings(settings);
    lccHolder.setServerStats(stats);
    lccHolder.setFriends(settings.getFriends());
    lccHolder.storeBlockedUsers(settings.getBlockedUsers(), settings.getBlockingUsers());
    lccHolder.getClient().subscribeToChallengeEvents(lccHolder.getChallengeListener());
    lccHolder.getClient().subscribeToGameEvents(lccHolder.getGameListener());
    lccHolder.getClient().subscribeToChatEvents(lccHolder.getChatListener());
    //lccHolder.getClient().subscribeToChatEvents(lccHolder.getChatListener());
    /*lccHolder.getClient()
      .subscribeToSeekList(LiveChessClient.SeekListOrderBy.Default, 1, lccHolder.getSeekListListener());*/

    lccHolder.getClient().subscribeToFriendStatusEvents(lccHolder.getFriendStatusListener());

    //lccHolder.getAndroid().sendConnectionBroadcastIntent(true, 0);
    /*lccHolder.getAndroid().getSharedDataEditor().putString("premium_status", "" + user.getMembershipLevel());
    lccHolder.getAndroid().getSharedDataEditor().commit();*/
    lccHolder.getAndroid().closeLoggingInIndicator();

    final ConnectivityManager connectivityManager = (ConnectivityManager)
    lccHolder.getAndroid().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    lccHolder.setNetworkTypeName(activeNetworkInfo.getTypeName());

    Log.d("", "LCCLOG CONNECTION: User has been connected: _user=" + user.getUsername() + ", authKey=" + user.getAuthKey());
  }

  public void onSettingsChanged(User user, UserSettings settings)
  {
    LccHolder.LOG.info("CONNECTION: onSettingsChanged");
    lccHolder.setFriends(settings.getFriends());
    lccHolder.storeBlockedUsers(settings.getBlockedUsers(), settings.getBlockingUsers());
  }

  public void onConnectionFailure(User user, String message, FailureDetails details, Throwable throwable)
  {
    Log.d("", "LCCLOG CONNECTION: User connection failure:" + message + ", details=" + details);
    lccHolder.setConnected(false);
    lccHolder.setConnectingInProgress(false);
    lccHolder.getAndroid().closeLoggingInIndicator();
    String detailsMessage = "";
    if (details != null)
    {
      switch(details)
      {
        case USER_KICKED:
        {
          detailsMessage = lccHolder.getAndroid().getContext().getString(R.string.lccFailedKicked);
          break;
        }
        case ACCOUNT_FAILED:
        {
          detailsMessage = lccHolder.getAndroid().getContext().getString(R.string.lccFailedUnavailable);
          break;
        }
        case SERVER_STOPPED:
        {
          detailsMessage = lccHolder.getAndroid().getContext().getString(R.string.lccFailedUnavailable);
          break;
        }
        default:
        {
          detailsMessage = message;
        }
      }
    }
    else
    {
      detailsMessage = "Connection/login error";
    }
    lccHolder.getAndroid().informAndExit("", detailsMessage);
    //lccHolder.getAndroid().sendConnectionBroadcastIntent(false, 0, detailsMessage);
  }

  public void onConnectionLost(User arg0, String arg1, FailureDetails arg2,
                               Throwable arg3)
  {
    LccHolder.LOG.info("LCCLOG CONNECTION: Connection Lost");
    lccHolder.setConnected(false);
    lccHolder.setConnectingInProgress(true);
    lccHolder.getAndroid().showReconnectingIndicator();
    lccHolder.getAndroid().closeLoggingInIndicator();
  }

  public void onConnectionReestablished(User arg0)
  {
    LccHolder.LOG.info("LCCLOG CONNECTION: onConnectionReestablished");
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
    lccHolder.getAndroid().closeReconnectingIndicator();
    lccHolder.getAndroid().closeLoggingInIndicator();
  }

  public void onPublishFailed(User user, Throwable th)
  {
    LccHolder.LOG.info("LCCLOG CONNECTION: onPublishFailed");
  }

  public void onConnectionRestored(User arg0)
  {
    LccHolder.LOG.info("LCCLOG CONNECTION: Connection Restored");
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
    lccHolder.getAndroid().closeReconnectingIndicator();
    lccHolder.getAndroid().closeLoggingInIndicator();
  }

  public void onObsoleteProtocolVersion(User user, String serverProtocolVersion, String clientProtocolVersion)
  {
    LccHolder.LOG.error(
        "Protocol version is obsolete (serverProtocolVersion=" + serverProtocolVersion + ", clientProtocolVersion=" +
        clientProtocolVersion + ")");
    lccHolder.getAndroid().processObsoleteProtocolVersion();
  }

  public void onKicked(User user, String reason, String message) {
    LccHolder.LOG.info("LCCLOG CONNECTION: user kicked");
    lccHolder.getAndroid().informAndExit(reason, "You have been kicked/banned");
    lccHolder.setNetworkTypeName(null);
  }

}
