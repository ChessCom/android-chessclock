package com.chess.lcc.android;

import com.chess.R;
import com.chess.live.client.*;

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
    System.out.println("Another client entered: user=" + user.getUsername());
    lccHolder.getAndroid().processOtherClientEntered();
  }

  public void onConnectionEstablished(User user, UserSettings settings, ServerStats stats)
  {
    lccHolder.setUser(user);
    lccHolder.setConnected(true);
    //lccHolder.setConnectingInProgress(false);
    lccHolder.setSettings(settings);
    lccHolder.setServerStats(stats);
    lccHolder.setFriends(settings.getFriends());
    lccHolder.storeBlockedUsers(settings.getBlockedUsers(), settings.getBlockingUsers());
    lccHolder.getClient().subscribeToChallengeEvents(lccHolder.getChallengeListener());
    lccHolder.getClient().subscribeToGameEvents(lccHolder.getGameListener());
    //lccHolder.getClient().subscribeToChatEvents(lccHolder.getChatListener());
    /*lccHolder.getClient()
      .subscribeToSeekList(LiveChessClient.SeekListOrderBy.Default, 1, lccHolder.getSeekListListener());*/
    lccHolder.getClient().subscribeToFriendStatusEvents(lccHolder.getFriendStatusListener());

    //lccHolder.getAndroid().sendConnectionBroadcastIntent(true, 0);
    lccHolder.getAndroid().getSharedDataEditor().putString("premium_status", "" + user.getMembershipLevel());

    System.out.println("User has been connected: _user=" + user.getUsername() + ", authKey=" + user.getAuthKey());
  }

  public void onSettingsChanged(User user, UserSettings settings)
  {
    LccHolder.LOG.info("CONNECTION: onSettingsChanged");
    lccHolder.setFriends(settings.getFriends());
    lccHolder.storeBlockedUsers(settings.getBlockedUsers(), settings.getBlockingUsers());
  }

  public void onConnectionFailure(User user, String message, FailureDetails details, Throwable throwable)
  {
    System.out.println("CONNECTION: User connection failure:" + message + ", details=" + details);
    lccHolder.setConnected(false);
    //lccHolder.setConnectingInProgress(false);
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
    //lccHolder.getAndroid().sendConnectionBroadcastIntent(false, 0, detailsMessage);
    //lccHolder.getAndroid().showReconnectingIndicator();
  }

  public void onConnectionLost(User arg0, String arg1, FailureDetails arg2,
                               Throwable arg3)
  {
    LccHolder.LOG.info("CONNECTION: Connection Lost");
    lccHolder.setConnected(false);
    //lccHolder.setConnectingInProgress(false);
    lccHolder.getAndroid().showReconnectingIndicator();
  }

  public void onConnectionReestablished(User arg0)
  {
    LccHolder.LOG.info("CONNECTION: onConnectionReestablished");
    lccHolder.clearGames();
    lccHolder.clearChallenges();
    lccHolder.clearOwnChallenges();
    lccHolder.clearSeeks();
    lccHolder.setConnected(true);
    //lccHolder.setConnectingInProgress(false);
    lccHolder.getClient().subscribeToChallengeEvents(lccHolder.getChallengeListener());
    lccHolder.getClient().subscribeToGameEvents(lccHolder.getGameListener());
    lccHolder.getClient().subscribeToChatEvents(lccHolder.getChatListener());
    /*lccHolder.getClient()
      .subscribeToSeekList(LiveChessClient.SeekListOrderBy.Default, 1, lccHolder.getSeekListListener());*/
    lccHolder.getClient().subscribeToFriendStatusEvents(lccHolder.getFriendStatusListener());
  }

  public void onConnectionRestored(User arg0)
  {
    LccHolder.LOG.info("CONNECTION: Connection Restored");
    lccHolder.setConnected(true);
    //lccHolder.setConnectingInProgress(false);
    lccHolder.getClient().subscribeToChallengeEvents(lccHolder.getChallengeListener());
    lccHolder.getClient().subscribeToGameEvents(lccHolder.getGameListener());
    lccHolder.getClient().subscribeToChatEvents(lccHolder.getChatListener());
    /*lccHolder.getClient()
      .subscribeToSeekList(LiveChessClient.SeekListOrderBy.Default, 1, lccHolder.getSeekListListener());*/
    lccHolder.getClient().subscribeToFriendStatusEvents(lccHolder.getFriendStatusListener());
    lccHolder.getAndroid().closeReconnectingIndicator();
  }

  public void onObsoleteProtocolVersion(User user, String serverProtocolVersion, String clientProtocolVersion)
  {
    LccHolder.LOG.error(
        "Protocol version is obsolete (serverProtocolVersion=" + serverProtocolVersion + ", clientProtocolVersion=" +
        clientProtocolVersion + ")");
    lccHolder.getAndroid().processObsoleteProtocolVersion();
  }

  @Override
  public void onKicked(User arg0, String arg1, String arg2) {
  // TODO Auto-generated method stub
  }

}
