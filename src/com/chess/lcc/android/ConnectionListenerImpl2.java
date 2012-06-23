package com.chess.lcc.android;

import android.util.Log;
import com.chess.backend.statics.StaticData;
import com.chess.live.client.*;

/**
 * Created by IntelliJ IDEA. User: Vova Date: 28.02.2010 Time: 15:50:16 To change this template use File | Settings |
 * File Templates.
 */
public class ConnectionListenerImpl2 implements ConnectionListener {
	public static final String CONNECTION = "CONNECTION";
	private LccHolder2 lccHolder;

    public ConnectionListenerImpl2(LccHolder2 lccHolder) {
		this.lccHolder = lccHolder;

    }

	public void onOtherClientEntered(User user) {
		Log.d(CONNECTION, "Another client entered: user=" + user.getUsername());
//		lccHolder.getAndroidStuff().processOtherClientEntered();
		lccHolder.onAnotherLoginDetected();
	}

	public void onConnectionEstablished(User user, UserSettings settings, ServerStats stats) {
        lccHolder.setUser(user);
		lccHolder.setConnected(true);
		Log.d("TEST", "onConnectionEstablished, setConnected(true)");
//		lccHolder.setConnectingInProgress(false);
		lccHolder.setFriends(settings.getFriends());
		Log.d("TEST", "Friends:");
		for(User friend:  settings.getFriends()){
			Log.d("TEST", "friend = " + friend);
		}

		lccHolder.storeBlockedUsers(settings.getBlockedUsers(), settings.getBlockingUsers());

		Log.d(CONNECTION, "User has been connected: _user=" + user.getUsername() + ", authKey=" + user.getAuthKey());
	}

	@Override
	public void onSettingsChanged(User user, UserSettings settings) {
		Log.d("TEST", "onSettingsChanged  settings:" + settings);
		Log.d(CONNECTION, "onSettingsChanged");
		lccHolder.setFriends(settings.getFriends());
		lccHolder.storeBlockedUsers(settings.getBlockedUsers(), settings.getBlockingUsers());
	}

/*
 public void execute(final Task task)
  {
    try
    {
      task.prepare();
      final TimerTask runnable = new TimerTask()
      {
        public void run()
        {
          try
          {
            if(LOG.isDebugEnabled())
              LOG.debug("Asynchronous execution of the task: " + task.getName());
            task.execute();
          }
          catch(Exception x)
          {
            task.handleException(x);  // proceed to call  listener.onConnectionFailure
          }
        }
      };


       public void handleException(Exception x)
    {
      super.handleException(x);
      listener.onConnectionFailure(loggedInUser, x.getMessage(), x instanceof LiveChessClientException ? ((LiveChessClientException)x).getFailureDetails() : null, x);
      loggedInUser = null;
    }

	 */

	@Override
	public void onConnectionFailure(User user, String message, FailureDetails details, Throwable throwable) {
		Log.d(CONNECTION, "User connection failure:" + message + ", details=" + details);
		Log.d("TEST", "User connection failure:" + message + ", details = " + details);

		if (details != null) {
			lccHolder.processConnectionFailure(details, message);
		} else {
			Log.d(CONNECTION, "User connection failure: IGNORING");
		}
	}

	@Override
	public void onConnectionLost(User user, String message, FailureDetails details, Throwable throwable) {
		Log.d("TEST", "Connection Lost, with message = " + message
                + " n\\Details: id = " + details.getFailureId() + "comments = " +details.getComments());
		Log.d(CONNECTION, "Connection Lost, with message = " + message
                + " n\\Details: id = " + details.getFailureId() + "comments = " +details.getComments());
		lccHolder.setConnected(false);
        lccHolder.performConnect();
	}

	@Override
	public void onConnectionReestablished(User arg0) {
		Log.d(CONNECTION, "onConnectionReestablished");
		Log.d("TEST", " onConnectionReestablished" );
		//lccHolder.clearGames();
		lccHolder.clearChallenges();
		lccHolder.clearOwnChallenges();
		lccHolder.clearSeeks();
		lccHolder.setConnected(true);

	}

	@Override
	public void onPublishFailed(User user, Throwable th) {
		Log.d(CONNECTION, "onPublishFailed");
		Log.d("TEST", "onPublishFailed");
	}

	@Override
	public void onConnectionRestored(User arg0) {
		Log.d("TEST", "Connection Restored");
		Log.d(CONNECTION, "Connection Restored");
		lccHolder.setConnected(true);

	}

	public void onObsoleteProtocolVersion(User user, String serverProtocolVersion, String clientProtocolVersion) {
		Log.d("TEST",
				"Protocol version is obsolete (serverProtocolVersion=" + serverProtocolVersion + ", clientProtocolVersion=" +
						clientProtocolVersion + StaticData.SYMBOL_RIGHT_PAR);
		Log.d(CONNECTION,
				"Protocol version is obsolete (serverProtocolVersion=" + serverProtocolVersion + ", clientProtocolVersion=" +
						clientProtocolVersion + StaticData.SYMBOL_RIGHT_PAR);
//		lccHolder.getAndroidStuff().processObsoleteProtocolVersion();
		lccHolder.onObsoleteProtocolVersion();
	}

	public void onKicked(User user, String reason, String message) { // TODO change when server change
		Log.d("TEST", "user kicked");
		Log.d(CONNECTION, "user kicked");
//		lccHolder.getAndroidStuff().informAndExit(reason, "You have been kicked/banned");
		lccHolder.setNetworkTypeName(null);  // TODO why it is not set in others methods?

//		lccHolder.processConnectionFailure(details);
		lccHolder.processConnectionFailure(reason, message);
	}

}
