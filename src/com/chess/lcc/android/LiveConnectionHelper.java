package com.chess.lcc.android;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.chess.R;
import com.chess.backend.LiveChessService;
import com.chess.backend.RestHelper;
import com.chess.backend.image_load.bitmapfun.AsyncTask;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.lcc.android.interfaces.LccConnectionUpdateFace;
import com.chess.lcc.android.interfaces.LiveChessClientEventListener;
import com.chess.live.client.FailureDetails;
import com.chess.live.client.LiveChessClient;
import com.chess.statics.AppConstants;
import com.chess.statics.AppData;
import com.chess.statics.Symbol;
import com.chess.utilities.LogMe;

import java.util.HashMap;
import java.util.Map;

public class LiveConnectionHelper {

	private static final String TAG = "LCCLOG-LiveConnectionHelper";

	private static final long SHUTDOWN_TIMEOUT_DELAY = 30 * 1000; // 30 sec, shutdown after user leave app
	private static final int CONNECTION_FAILURE_DELAY = 2000;
	public static final Object CLIENT_SYNC_LOCK = new Object();
	public static final boolean RESET_LCC_LISTENERS = true;
	private final LiveChessService liveService;

	boolean liveConnected;
	private boolean connectionFailure;
	private boolean liveConnecting;
	private final LccHelper lccHelper;
	private final Context context;
	private AppData appData;
	private LiveChessClient lccClient;
	private LiveChessClientEventListener liveChessClientEventListener;
	private final LccConnectionListener connectionListener;
	private final LccSubscriptionListener subscriptionListener;
	private Map<LiveEvent.Event, LiveEvent> pausedActivityLiveEvents = new HashMap<LiveEvent.Event, LiveEvent>();
	private LccConnectionUpdateFace connectionUpdateFace;
	private Handler handler;


	public LiveConnectionHelper(LiveChessService liveService) {

		this.lccHelper = new LccHelper(liveService);

		this.context = liveService;
		this.liveService = liveService;

		appData = new AppData(context);

		connectionListener = new LccConnectionListener(this);
		subscriptionListener = new LccSubscriptionListener();
		handler = new Handler();
	}

	public void checkAndConnectLiveClient() {

		if (appData.isLiveChess() && !isConnected()) {
			if (!isLccConnecting() && (lccClient == null || isConnectionFailure())) { // prevent creating several instances when user navigates between activities in "reconnecting" mode
				LogMe.dl(TAG, "start Connection Task");
				runConnectTask();
			} else { // when client is connecting, but device screen was rotated for example
				LogMe.dl(TAG, "client is CONNECTING");
			}
		} else if (isConnected()) {
			LogMe.dl(TAG, "connected case");
			onLiveConnected();
		} else {
			// we get here when network connection changes and we get different ip address
			// lccHelper.performConnect(true);  // probably need to be changed to create new instance of live client and perform connect

			// vm: lets avoid any manual connects here, LCC is in charge on that.

			LogMe.dl(TAG, "else case");
		}
	}

	/**
	 * Connect live chess client
	 */
	public void performConnect() {
		AppData appData = new AppData(context);
		String username = appData.getUsername();
		String pass = appData.getPassword();

		// here we check if sessionId is not expired(ttl = 60min)
		long sessionIdSaveTime = appData.getLiveSessionIdSaveTime();
		long currentTime = System.currentTimeMillis();
		String sessionId = appData.getLiveSessionId();
		LogMe.dl(TAG, "sessionIdSaveTime = " + sessionIdSaveTime + ", currentTime = " + currentTime + ", sessionId = " + sessionId);

		boolean useSessionId = currentTime - sessionIdSaveTime <= AppConstants.LIVE_SESSION_EXPIRE_TIME
				&& !TextUtils.isEmpty(sessionId);

		boolean emptyPassword = pass.equals(Symbol.EMPTY);

		if (useSessionId) {
			if (emptyPassword || RestHelper.getInstance().IS_TEST_SERVER_MODE) {
				connectBySessionId(sessionId);
			} else {
				connectByCreds(username, pass);
			}
		} else {
			if (!emptyPassword && !RestHelper.getInstance().IS_TEST_SERVER_MODE) {
				connectByCreds(username, pass);
			} else {
				connectionFailure = true; // we need this flag to able to re-connect from live chess
				liveChessClientEventListener.onSessionExpired();
				//String message = context.getString(R.string.account_error);
				//liveChessClientEventListener.onConnectionFailure(message);
			}
		}
	}

	public void connectByCreds(String username, String pass) {
//		LogMe.dl(TAG, "connectByCreds : user = " + username + " pass = " + pass); // do not post in prod
		LogMe.dl(TAG, "connectByCreds : hidden"); // do not post in pod
		lccClient.connect(username, pass, connectionListener, subscriptionListener);
	}

	public void connectBySessionId(String sessionId) {
		LogMe.dl(TAG, "connectBySessionId : sessionId = " + sessionId);
		lccClient.connect(sessionId, connectionListener, subscriptionListener);
	}

	/**
	 * This method should be invoked only when live chess service bounded to activity.
	 * Thus we can use it to check if activity is bounded
	 * @param liveChessClientEventListener listener for events
	 */
	public void setLiveChessClientEventListener(LiveChessClientEventListener liveChessClientEventListener) {
		this.liveChessClientEventListener = liveChessClientEventListener;
	}

	public void onOtherClientEntered(String message) {
		connectionFailure = true;
		logout();
		liveChessClientEventListener.onConnectionFailure(message);
	}

	public void processKicked() {

		connectionFailure = true;
		logout();

		String kickMessage = context.getString(R.string.live_chess_server_upgrading);
		liveChessClientEventListener.onConnectionFailure(kickMessage);
	}

	public void processConnectionFailure(FailureDetails details) {

		/*if (details == null && !AppUtils.isNetworkAvailable(context)) {
			// handle null-case when user tries to connect when device connection is off, just ignore
			LogMe.dl(TAG, "processConnectionFailure: no active connection, wait for LCC reconnect");
			return;
		}*/

		LogMe.dl(TAG, "processConnectionFailure: details=" + details);

		String detailsMessage;

		setConnected(false);

		if (details != null) { // LCC stops client, create new one manually

			connectionFailure = true;

			setConnecting(false);
			cleanupLiveInfo();
			cancelServiceNotification();

			switch (details) {
				case USER_KICKED: {
					detailsMessage = context.getString(R.string.live_chess_server_upgrading);
					break;
				}
				case ACCOUNT_FAILED: { // wrong authKey  // TODO we should use check for correct login BEFORE connection
					/*AppData appData = new AppData(context);
					if (appData.getLiveConnectAttempts(context) < LIVE_CONNECTION_ATTEMPTS_LIMIT) {
						appData.incrementLiveConnectAttempts(context);*/

					// first of all we need to invalidate sessionId key
					new AppData(context).setLiveSessionId(null);

					if (isPossibleToReconnect()) {
						try {
							Thread.sleep(CONNECTION_FAILURE_DELAY);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						runConnectTask();
					} else {
						liveChessClientEventListener.onConnectionFailure(context.getString(R.string.pleaseLoginAgain));
					}
					return;
				}
				case SERVER_STOPPED: {
					detailsMessage = context.getString(R.string.server_stopped)
							+ context.getString(R.string.live_chess_server_unavailable);
					break;
				}
				// when connect(login, pswd) and auth_url is unreachable, details="Authentication service failed"
				/*
				case AUTH_URL_FAILED: {
					return;
				}
				*/
				default:
					// todo: show login/password popup instead
					detailsMessage = context.getString(R.string.pleaseLoginAgain);
					break;
			}

		} else { // when connect(authKey) and Live server in unreachable, details=null

			setConnecting(true);
			return;
		}

		liveChessClientEventListener.onConnectionFailure(detailsMessage);
	}

	private boolean isPossibleToReconnect() {
		AppData appData = new AppData(context);
		String pass = appData.getPassword();

		// here we check if sessionId is not expired(ttl = 60min)
		long sessionIdSaveTime = appData.getLiveSessionIdSaveTime();
		long currentTime = System.currentTimeMillis();

		boolean useSessionId = currentTime - sessionIdSaveTime <= AppConstants.LIVE_SESSION_EXPIRE_TIME;

		boolean emptyPassword = pass.equals(Symbol.EMPTY);

		if (useSessionId) {
			if (emptyPassword || RestHelper.getInstance().IS_TEST_SERVER_MODE) {
				return true;
			} else {
				return true;
			}
		} else {
			return !emptyPassword && !RestHelper.getInstance().IS_TEST_SERVER_MODE;
		}
	}

	public boolean isConnected() {
		return lccClient != null && liveConnected;
	}

	public void setConnected(boolean connected) {
		liveConnected = connected;

		if (connected) {

			appData.resetLiveConnectAttempts();

			connectionFailure = false;
			//connectionFailureCounter = 0;

			onLiveConnected();
			//liveChessClientEventListener.onConnectionEstablished();
			lccHelper.subscribeToLccListeners();

			//lccClient.subscribeToDebugGameEvents(new StandardDebugGameListener(getUser()));

//			ConnectivityManager connectivityManager = (ConnectivityManager)
//					context.getSystemService(Context.CONNECTIVITY_SERVICE);
//			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//			updateNetworkType(activeNetworkInfo.getTypeName());
		}
		// when onDestroy of service invoked, we don't have listeners anymore
		if (liveChessClientEventListener != null) {
			liveChessClientEventListener.onConnectionBlocked(!connected);
		}
	}

	public void cleanupLiveInfo() {
		LogMe.dl(TAG, "cleanupLiveInfo");
		//new AppData(context).setLiveChessMode(false); // let UI set it
		lccHelper.setCurrentGameId(null);
		lccHelper.setCurrentObservedGameId(null);
		lccHelper.setUser(null);
		lccHelper.clearGames();
		lccHelper.clearChallenges();
		lccHelper.clearOwnChallenges();
		lccHelper.clearSeeks();
		lccHelper.clearOnlineFriends();
		clearPausedEvents();
	}

	public void logout() {
		LogMe.dl(TAG, "USER LOGOUT");
		setConnected(false);
		setConnecting(false);
		cleanupLiveInfo();
		runDisconnectTask();
		cancelServiceNotification();
		//stopConnectionTimer();
	}

	private void leave() {
		setConnected(false);
		setConnecting(false);
		cleanupLiveInfo();
		runLeaveTask();
		//stopConnectionTimer();
	}

	public void runConnectTask() {
		setConnecting(true);
		new ConnectLiveChessTask(new LccConnectUpdateListener(), this).executeTask();
	}

	public class LccConnectUpdateListener extends AbstractUpdateListener<LiveChessClient> {
		public LccConnectUpdateListener() {
			super(context);
		}

		@Override
		public void updateData(LiveChessClient returnedObj) {
			LogMe.dl(TAG, "LiveChessClient initialized " + returnedObj);
		}
	}


	public void runDisconnectTask() {
		new LiveDisconnectTask().execute();
	}

	private class LiveDisconnectTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... voids) {
			synchronized (CLIENT_SYNC_LOCK) {
				if (lccClient != null) {
					LogMe.dl(TAG, "LOGOUT: lccClient=" + getClientId());
					lccClient.disconnect(RESET_LCC_LISTENERS);
					resetClient();
				}
			}
			return null;
		}
	}

	public void runLeaveTask() {
		new LiveLeaveTask().execute();
	}

	private class LiveLeaveTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... voids) {
			synchronized (CLIENT_SYNC_LOCK) {
				if (lccClient != null) {
					LogMe.dl(TAG, "LEAVE: lccClient=" + getClientId());
					lccClient.leave(RESET_LCC_LISTENERS);
					resetClient();
				}
			}
			return null;
		}
	}

	public boolean isConnectionFailure() {
		return connectionFailure;
	}

	public void setConnecting(boolean liveConnecting) {
		this.liveConnecting = liveConnecting;
	}

	public boolean isLccConnecting() {
		return /*lccClient != null && */liveConnecting;
	}

	private void cancelServiceNotification() {
		liveService.stopForeground(true); // exit Foreground mode and remove Notification icon
	}

	public void setLiveChessClient(LiveChessClient liveChessClient) {
		lccClient = liveChessClient;
		lccHelper.setLiveChessClient(lccClient);
	}

	public LiveChessClient getClient() {
		return lccClient;
	}

	// set user if necessary
	/*public void setUser(User user) {
	}*/

	public void resetClient() {
		LogMe.dl(TAG, "reset LCC instance");
		lccClient = null;
		lccHelper.setLiveChessClient(null);
	}

	public boolean isLiveChessEventListenerSet() {
		return liveChessClientEventListener != null;
	}

	public void onObsoleteProtocolVersion() {
		liveChessClientEventListener.onObsoleteProtocolVersion();
	}

	public LccHelper getLccHelper() {
		return lccHelper;
	}

	public Long getClientId() {
		return lccClient == null ? null : lccClient.getId();
	}

	public void clearPausedEvents() {
		pausedActivityLiveEvents.clear();
	}

	public Map<LiveEvent.Event, LiveEvent> getPausedActivityLiveEvents() {
		return pausedActivityLiveEvents;
	}

	public void onLiveConnected() {
		LogMe.dl(TAG, "onLiveConnected");
		if (connectionUpdateFace != null) {
			connectionUpdateFace.onConnected();
		}
		liveService.onLiveConnected();
	}

	public void setConnectionUpdateFace(LccConnectionUpdateFace connectionUpdateFace) {
		this.connectionUpdateFace = connectionUpdateFace;
	}

	public void startIdleTimeOutCounter() {
		handler.postDelayed(shutDownRunnable, SHUTDOWN_TIMEOUT_DELAY);
	}

	public void stopIdleTimeOutCounter() {
		handler.removeCallbacks(shutDownRunnable);
	}

	private final Runnable shutDownRunnable = new Runnable() {
		@Override
		public void run() {
			leave();
			Log.d("TEST", "shutDownRunnable, performing leave, and stopping service, hide notification");

			liveService.stop();
		}
	};
}
