package com.chess.lcc.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.bugsense.trace.BugSenseHandler;
import com.chess.R;
import com.chess.backend.LiveChessService;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.ChatItem;
import com.chess.backend.entity.api.LoginItem;
import com.chess.backend.image_load.bitmapfun.AsyncTask;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.interfaces.LoginUpdateListener;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.lcc.android.interfaces.LccConnectionUpdateFace;
import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.lcc.android.interfaces.LiveChessClientEventListener;
import com.chess.live.client.*;
import com.chess.live.util.ThreadMonitor;
import com.chess.model.DataHolder;
import com.chess.model.GameLiveItem;
import com.chess.statics.AppConstants;
import com.chess.statics.AppData;
import com.chess.statics.FlurryData;
import com.chess.statics.StaticData;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.interfaces.MakeMoveFace;
import com.chess.ui.interfaces.PopupShowFace;
import com.chess.utilities.AppUtils;
import com.chess.utilities.LogMe;
import com.chess.utilities.Ping;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveConnectionHelper {

	private static final String TAG = "LCCLOG-LiveConnectionHelper";
	private static final boolean PING_ENABLED = false;
//	private static final boolean PING_ENABLED = com.chess.BuildConfig.DEBUG;

	public static final boolean THREAD_MONITORING_ENABLED = true;
	private static ThreadMonitor TM;
	private static float THREAD_MONITOR_POLLING_INTERVAL = 10.0f;
	//private static CpuUsageLogger CPU_LOGGER;

	private static final long SHUTDOWN_TIMEOUT_DELAY = 30 * 1000; // 30 sec, shutdown after user leave app
	private static final long PLAYING_SHUTDOWN_TIMEOUT_DELAY = 2 * 60 * 1000;
	public static final Object CLIENT_SYNC_LOCK = new Object(); // todo: probably also move to UI thread
	public static final boolean RESET_LCC_LISTENERS = true;

	private AppData appData;
	private Handler handler;
	private final Context context;

	private final LiveChessService liveService;
	private LiveChessClient lccClient;
	private final LccHelper lccHelper;

	private boolean liveConnected;
	private boolean connectionFailure;
	private boolean liveConnecting;

	private LccConnectionListener connectionListener;
	private LccSubscriptionListener subscriptionListener;

	private LiveChessClientEventListener liveChessClientEventListener;
	private LccConnectionUpdateFace connectionUpdateFace;

	private Map<LiveEvent.Event, LiveEvent> pausedActivityLiveEvents = new HashMap<LiveEvent.Event, LiveEvent>();

	private LccChallengeTaskRunner challengeTaskRunner;
	private LccGameTaskRunner gameTaskRunner;
	private Ping testPing;
	private PopupShowFace popupShowFace;


	public LiveConnectionHelper(LiveChessService liveService) {

		this.context = liveService;
		this.liveService = liveService;
		this.lccHelper = new LccHelper(this);

		appData = new AppData(context);
		connectionListener = new LccConnectionListener(this);
		subscriptionListener = new LccSubscriptionListener();
		handler = new Handler();
		testPing = new Ping(context);

		if (THREAD_MONITORING_ENABLED) {
			TM = new ThreadMonitor(THREAD_MONITOR_POLLING_INTERVAL, true, false, true, new ThreadMonitorListener());
			//CPU_LOGGER = new CpuUsageLogger();
		}
	}

	public void checkAndConnect(LccConnectionUpdateFace connectionUpdateFace) {
//		LogMe.dl(TAG, "appData.isLiveChess(getContext()) " + appData.isLiveChess());
		LogMe.dl(TAG, "liveConnectionHelper instance in checkAndConnect = " + this);
		LogMe.dl(TAG, "lccClient instance in checkAndConnect = " + lccClient);

		setConnectionUpdateFace(connectionUpdateFace);
		checkAndConnectLiveClient();
	}

	public void checkAndConnectLiveClient() {

		if (lccClient != null) {
			LogMe.dl(TAG, "DEBUG: lccClient.isInvalid()=" + lccClient.isInvalid());
			LogMe.dl(TAG, "DEBUG: lccClient.isConnected()=" + lccClient.isConnected());
		}
		LogMe.dl(TAG, "DEBUG: isLccConnecting()=" + isLccConnecting());
		LogMe.dl(TAG, "DEBUG: isConnectionFailure()=" + isConnectionFailure());

		if (DataHolder.getInstance().isLiveChess() && !isConnected()) {
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
		/*
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

				onSessionExpired();
			}
		}
		*/

		// todo: @lcc - check SessionId for null or empty string?
		String sessionId = appData.getLiveSessionId();
		connectBySessionId(sessionId);
	}

	private void onSessionExpired() {
		performReloginForLive();
	}

	/*public void connectByCreds(String username, String pass) {
//		LogMe.dl(TAG, "connectByCreds : user = " + username + " pass = " + pass); // do not post in prod
		LogMe.dl(TAG, "connectByCreds : hidden"); // do not post in pod
		lccClient.connect(username, pass, connectionListener, subscriptionListener);
	}*/

	public void connectBySessionId(String sessionId) {
		LogMe.dl(TAG, "connectBySessionId : sessionId = " + sessionId);
		lccClient.connect(sessionId, connectionListener, subscriptionListener);
	}

	/**
	 * This method should be invoked only when live chess service bounded to activity.
	 * Thus we can use it to check if activity is bounded
	 *
	 * @param liveChessClientEventListener listener for events
	 */
	public void setLiveChessClientEventListener(LiveChessClientEventListener liveChessClientEventListener) {
		this.liveChessClientEventListener = liveChessClientEventListener;
	}

	public LiveChessClientEventListener getLiveChessClientEventListener() {
		return liveChessClientEventListener;
	}

	public void popupShowListener(PopupShowFace popupShowFace) {
		this.popupShowFace = popupShowFace;
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

		//cancelServiceNotification();
		setConnected(false);

		if (details != null) { // LCC stops client, create new one manually

			connectionFailure = true;

			setConnecting(false);
			cleanupLiveInfo();
			//cancelServiceNotification();

			switch (details) {
				case USER_KICKED: {
					detailsMessage = context.getString(R.string.live_chess_server_upgrading);
					break;
				}
				case ACCOUNT_FAILED: { // wrong authKey  // TODO we should use check for correct login BEFORE connection
					/*AppData appData = new AppData(context);
					if (appData.getLiveConnectAttempts(context) < LIVE_CONNECTION_ATTEMPTS_LIMIT) {
						appData.incrementLiveConnectAttempts(context);*/

					/*
					// first of all we need to invalidate sessionId key
					appData.setLiveSessionId(null);

					if (isPossibleToReconnect()) {
						try {
							Thread.sleep(CONNECTION_FAILURE_DELAY);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						runConnectTask();
					} else {
					*/
					onSessionExpired();
					//liveChessClientEventListener.onConnectionFailure(context.getString(R.string.pleaseLoginAgain));
					//}
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
			liveService.stop();

		} else { // when connect(authKey) and Live server in unreachable, details=null

			setConnecting(true);
			return;
		}

		liveChessClientEventListener.onConnectionFailure(detailsMessage);
	}

	/*
	private boolean isPossibleToReconnect() {
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
	*/

	public boolean isConnected() {
		return lccClient != null && liveConnected;
	}

	public void setConnected(boolean connected) {
		liveConnected = connected;

		if (connected) {

			connectionFailure = false;
			//connectionFailureCounter = 0;

			onLiveConnected();
			//liveChessClientEventListener.onConnectionEstablished();
			lccHelper.subscribeToLccListeners(); // todo: move to task

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

		synchronized (LccHelper.GAME_SYNC_LOCK) {
			lccHelper.setCurrentGameId(null);
			lccHelper.setCurrentObservedGameId(null);
			lccHelper.setUser(null);
			lccHelper.clearGames();
			lccHelper.clearChallengesData();
			lccHelper.clearOnlineFriends();
			clearPausedEvents();
		}
	}

	public void logout() {
		LogMe.dl(TAG, "USER LOGOUT");
		setConnected(false);
		setConnecting(false);
		cleanupLiveInfo();
		runDisconnectTask();
		//cancelServiceNotification();
	}

	public void leave() {
		setConnected(false);
		setConnecting(false);
		cleanupLiveInfo();
		runLeaveTask();
		liveService.stop();
	}

	private void runConnectTask() {
		setConnecting(true);
		new ConnectLiveChessTask(new LccConnectUpdateListener(), this).executeTask();
	}

	private class LccConnectUpdateListener extends AbstractUpdateListener<LiveChessClient> {
		public LccConnectUpdateListener() {
			super(context);
		}

		@Override
		public void updateData(LiveChessClient returnedObj) {
			LogMe.dl(TAG, "LiveChessClient initialized " + returnedObj);
		}
	}

	private void runDisconnectTask() {
		new LiveDisconnectTask().execute();
	}

	private class LiveDisconnectTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... voids) {
			synchronized (CLIENT_SYNC_LOCK) {
				if (lccClient != null) {
					LogMe.dl(TAG, "LOGOUT: lccClient=" + getClientId());
					lccClient.disconnect(RESET_LCC_LISTENERS);
					liveService.stop();
					resetClient();
				}
			}
			return null;
		}
	}

	private void runLeaveTask() {
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
		LogMe.dl(TAG, "START shutdown timer");
		handler.postDelayed(shutDownRunnable, getShutDownDelay());
	}

	public void stopIdleTimeOutCounter() {
		LogMe.dl(TAG, "STOP shutdown timer");
		handler.removeCallbacks(shutDownRunnable);
	}

	private final Runnable shutDownRunnable = new Runnable() {
		@Override
		public void run() {
			if (connectionUpdateFace != null) {
				connectionUpdateFace.onShutdown();
			}
			Log.d(TAG, "shutDownRunnable, performing leave, and stopping service, hide notification");
			leave();
		}
	};

	private long getShutDownDelay() {
		return lccHelper.isUserPlaying() ? PLAYING_SHUTDOWN_TIMEOUT_DELAY : SHUTDOWN_TIMEOUT_DELAY;
	}

	public void pingLive() {
		if (PING_ENABLED) {
			stopPingLiveTimer();
			//testPing.runPingLiveTask();
			//testPing.runTestRequestLiveServerTask();
			runPingLiveTimer();
		}
	}

	public void runPingLiveTimer() {
		testPing.runPingLiveTimer();
		testPing.runTestRequestsTimer();
	}

	public void stopPingLiveTimer() {
		if (PING_ENABLED) {
			testPing.stopPingLiveTimer();
			testPing.stopTestRequestsTimer();
		}
	}

	// ------------------- Task runners wrapping ------------------------

	public void setChallengeTaskListener(AbstractUpdateListener<Challenge> challengeTaskListener) {
		challengeTaskRunner = new LccChallengeTaskRunner(challengeTaskListener, lccHelper);
	}

	public void declineAllChallenges(Challenge currentChallenge) {
		challengeTaskRunner.declineAllChallenges(currentChallenge, lccHelper.getChallenges());
	}

	public void runAcceptChallengeTask(Challenge currentChallenge) {
		challengeTaskRunner.runAcceptChallengeTask(currentChallenge);
	}

	public void declineCurrentChallenge(Challenge currentChallenge) {
		challengeTaskRunner.declineCurrentChallenge(currentChallenge, lccHelper.getChallenges());
	}

	public void cancelAllOwnChallenges() {
		challengeTaskRunner.cancelAllOwnChallenges(lccHelper.getOwnChallenges());
	}

	public void runSendChallengeTask(Challenge challenge) {
		challengeTaskRunner.runSendChallengeTask(challenge);
	}

	public void setGameTaskListener(ActionBarUpdateListener<Game> gameTaskListener) {
		gameTaskRunner = new LccGameTaskRunner(gameTaskListener, lccHelper);
	}

	public void runMakeDrawTask() {
		gameTaskRunner.runMakeDrawTask();
	}

	public void runMakeResignTask() {
		gameTaskRunner.runMakeResignTask();
	}

	public void runMakeResignAndExitTask() {
		// todo: gameTaskRunner sets in onLiveClientConnected and onResume. Investigate why it is null here
		if (gameTaskRunner != null) {
			gameTaskRunner.runMakeResignAndExitTask();
		}
	}

	public void runRejectDrawTask() {
		gameTaskRunner.runRejectDrawTask();
	}

	// ------------------- LccHelper wrapping --------------------------

	public boolean isActiveGamePresent() {
		return lccHelper.isActiveGamePresent();
	}

	public boolean isUserPlaying() {
		return lccHelper.isUserPlaying();
	}

	/*public boolean isValidToMakeMove() {
		return lccHelper != null && lccHelper.getUser() != null && isConnected() && lccHelper.isActiveGamePresent();
	}*/

	public User getUser() {
		return lccHelper.getUser();
	}

	public void checkTestMove() {
		lccHelper.checkTestMove();
	}

	public HashMap<Long, Challenge> getChallenges() {
		return lccHelper.getChallenges();
	}

	public void setOuterChallengeListener(OuterChallengeListener outerChallengeListener) {
		lccHelper.setOuterChallengeListener(outerChallengeListener);
	}

	public void setLccChatMessageListener(LccChatMessageListener chatMessageListener) {
		lccHelper.setLccChatMessageListener(chatMessageListener);
	}

	public void setLccEventListener(LccEventListener eventListener) {
		lccHelper.setLccEventListener(eventListener);
	}

	public void setLccObserveEventListener(LccEventListener eventListener) {
		lccHelper.setLccObserveEventListener(eventListener);
	}

	public List<ChatItem> getMessagesList() {
		return lccHelper.getMessagesList();
	}

	public Long getCurrentGameId() {
		return lccHelper.getCurrentGameId();
	}

	public String[] getOnlineFriends() {
		return lccHelper.getOnlineFriends();
	}

	public GameLiveItem getGameItem() {
		return lccHelper.getGameItem();
	}

	public GameLiveItem getObservedGameItem() {
		return lccHelper.getObservedGameItem();
	}

	public Game getCurrentObservedGame() {
		return lccHelper.getCurrentObservedGame();
	}

	public int getResignTitle() {
		return lccHelper.getResignTitle();
	}

	public List<String> getPendingWarnings() {
		return lccHelper.getPendingWarnings();
	}

	public String getLastWarningMessage() {
		return lccHelper.getLastWarningMessage();
	}

	public void setGameActivityPausedMode(boolean gameActivityPausedMode) {
		lccHelper.setGameActivityPausedMode(gameActivityPausedMode);
	}

	public void checkAndReplayMoves() {
		lccHelper.checkAndReplayMoves();
	}

	public void checkFirstTestMove() {
		lccHelper.checkFirstTestMove();
	}

	public String getUsername() {
		return lccHelper.getUsername();
	}

	public Game getCurrentGame() {
		return lccHelper.getCurrentGame();
	}

	public Integer getGamesCount() {
		return lccHelper.getGamesCount();
	}

	public Integer getLatestMoveNumber() {
		return lccHelper.getLatestMoveNumber();
	}

	public Boolean isUserColorWhite() {
		return lccHelper.isUserColorWhite();
	}

	public boolean isFairPlayRestriction() {
		return lccHelper.isFairPlayRestriction();
	}

	public void makeMove(String move, String temporaryDebugInfo, MakeMoveFace makeMoveFace) {
		lccHelper.makeMove(move, gameTaskRunner, temporaryDebugInfo, makeMoveFace);
	}

	public void updatePlayersClock() {
		lccHelper.updatePlayersClock();
	}

	public void rematch() {
		lccHelper.rematch();
	}

	public void checkGameEvents() {
		lccHelper.checkGameEvents();
	}

	public void createChallenge(LiveGameConfig config) {
		Gson gson = new Gson();
		LogMe.dl("TEST", "live config = " + gson.toJson(config));
		lccHelper.createChallenge(config);
	}

	public void sendMessage(String message, TaskUpdateInterface<String> taskFace) {
		new SendLiveMessageTask(taskFace, message).executeTask(lccHelper.getCurrentGameId());
	}

	private class SendLiveMessageTask extends AbstractUpdateTask<String, Long> {

		private String message;

		public SendLiveMessageTask(TaskUpdateInterface<String> taskFace, String message) {
			super(taskFace);
			this.message = message;
		}

		@Override
		protected Integer doTheTask(Long... params) {
			lccHelper.sendChatMessage(params[0], message);
			return StaticData.RESULT_OK;
		}
	}

	public void runObserveTopGameTask(TaskUpdateInterface<Void> taskFace) {
		new ObserveTopGameTask(taskFace).execute();
	}

	private class ObserveTopGameTask extends AbstractUpdateTask<Void, Void> {

		public ObserveTopGameTask(TaskUpdateInterface<Void> taskFace) {
			super(taskFace);
		}

		@Override
		protected Integer doTheTask(Void... params) {
			lccHelper.observeTopGame();
			return StaticData.RESULT_OK;
		}
	}

	public void exitGameObserving() {
		LogMe.dl(TAG, "exitGameObserving");
		setLccObserveEventListener(null);
		//lccHelper.setCurrentGameId(null); // looks redundant here, lets try to avoid this
		lccHelper.stopClocks();
		lccHelper.unObserveCurrentObservingGame();
		lccHelper.setCurrentObservedGameId(null);
	}

	public void initClocks() {
		lccHelper.initClocks();
	}

	public void stopClocks() {
		lccHelper.stopClocks();
	}

	public boolean isCurrentGameObserved() {
		Game currentGame = lccHelper.getCurrentGame();
		boolean isCurrentGameObserved = currentGame != null && lccHelper.isObservedGame(currentGame);

		return isCurrentGameObserved;
	}

	public boolean isObservedGame(Game game) {
		return lccHelper.isObservedGame(game);
	}

	public Context getContext() {
		return context;
	}

	/*
	private void sessionIdCheck() {
		LoadItem loadItem = LoadHelper.getUserInfo(appData.getUserToken());
		loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.V_SESSION_ID);

		new RequestJsonTask<UserItem>(new SessionIdUpdateListener()).executeTask(loadItem);
	}

	private class SessionIdUpdateListener extends AbstractUpdateListener<UserItem> {

		public SessionIdUpdateListener() {
			super(context, UserItem.class);
		}

		@Override
		public void updateData(UserItem returnedObj) {
			super.updateData(returnedObj);

			if (TextUtils.isEmpty(returnedObj.getData().getSessionId())) { // if API was not updated to get a single sessionId field
				// we perform re-login
				performReloginForLive();
				//needReLoginToLive = true;
				return;
			} else {
				appData.setLiveSessionId(returnedObj.getData().getSessionId());
			}

			if (liveUiUpdateListener != null) {
				liveUiUpdateListener.performServiceConnection();
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			//LogMe.dl(TAG, "SessionIdUpdateListener errorHandle resultCode=" + resultCode);
		}
	}*/

	public void performReloginForLive() {
		Log.d(TAG, "performReloginForLive");

		/*
		logout();
		unBindAndStopLiveService();
		*/

		String password = appData.getPassword();
		if (!TextUtils.isEmpty(password)) {

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_LOGIN);
			loadItem.setRequestMethod(RestHelper.POST);
			loadItem.addRequestParams(RestHelper.P_DEVICE_ID, AppUtils.getDeviceId(context));
			loadItem.addRequestParams(RestHelper.P_USER_NAME_OR_MAIL, appData.getUsername());
			loadItem.addRequestParams(RestHelper.P_PASSWORD, password);
			loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.P_USERNAME);
			loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.P_TACTICS_RATING);

			new RequestJsonTask<LoginItem>(new CredentialsLoginUpdateListener()).executeTask(loadItem);

		} else if (!TextUtils.isEmpty(appData.getFacebookToken())) {

			String accessToken = appData.getFacebookToken();

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_LOGIN);
			loadItem.setRequestMethod(RestHelper.POST);
			loadItem.addRequestParams(RestHelper.P_FACEBOOK_ACCESS_TOKEN, accessToken);
			loadItem.addRequestParams(RestHelper.P_DEVICE_ID, AppUtils.getDeviceId(context));
			loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.V_USERNAME);
			loadItem.addRequestParams(RestHelper.P_FIELDS_, RestHelper.V_TACTICS_RATING);

			FacebookLoginUpdateListener facebookLoginUpdateListener =
					new FacebookLoginUpdateListener(context, accessToken);

			new RequestJsonTask<LoginItem>(facebookLoginUpdateListener).executeTask(loadItem);
		}

		//needReLoginToLive = true;
	}

	private class CredentialsLoginUpdateListener extends AbstractUpdateListener<LoginItem> {
		public CredentialsLoginUpdateListener() {
			super(context, LoginItem.class);
		}

		@Override
		public void showProgress(boolean show) { // DO not show progress as we already showing it while making first attempt to connect
		}

		@Override
		public void updateData(LoginItem returnedObj) {

			LoginItem.Data loginData = returnedObj.getData();

			SharedPreferences.Editor preferencesEditor = appData.getEditor();

			String username = loginData.getUsername();
			if (!TextUtils.isEmpty(username)) {
				preferencesEditor.putString(AppConstants.USERNAME, username);
			}
			preferencesEditor.putInt(AppConstants.USER_PREMIUM_STATUS, loginData.getPremiumStatus());
			preferencesEditor.putString(AppConstants.LIVE_SESSION_ID, loginData.getSessionId());
			preferencesEditor.putLong(AppConstants.LIVE_SESSION_ID_SAVE_TIME, System.currentTimeMillis());
			preferencesEditor.putLong(username + AppConstants.PREF_USER_ID, loginData.getUserId());
			preferencesEditor.putString(AppConstants.USER_TOKEN, loginData.getLoginToken());
			preferencesEditor.putLong(AppConstants.USER_TOKEN_SAVE_TIME, System.currentTimeMillis());
			preferencesEditor.commit();

			liveChessClientEventListener.registerGcm();
			DataHolder.getInstance().setLiveChessMode(true);
			Log.d(TAG, "LBA CredentialsLoginUpdateListener -> updateData");

			liveChessClientEventListener.performServiceConnection();
		}

		@Override
		public void errorHandle(Integer resultCode) {

			// show message only for re-login and app update
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.ACCESS_DENIED_CODE) { // handled in CommonLogicFragment
					if (popupShowFace == null) {
						String message = context.getString(R.string.version_is_obsolete_update);
						popupShowFace.safeShowSinglePopupDialog(R.string.error, message);
					}
					return;
				} else if (serverCode != ServerErrorCodes.INVALID_LOGIN_TOKEN_SUPPLIED) { // handled in CommonLogicFragment
					if (popupShowFace == null) {
						String serverMessage = ServerErrorCodes.getUserFriendlyMessage(context, serverCode); // TODO restore
						popupShowFace.safeShowSinglePopupDialog(R.string.error, serverMessage);
					}
					return;
				}
			}
			super.errorHandle(resultCode);
		}
	}

	private class FacebookLoginUpdateListener extends LoginUpdateListener {

		public FacebookLoginUpdateListener(Context context, String facebookToken) {
			super(context, facebookToken, popupShowFace);
		}

		@Override
		public void updateData(LoginItem returnedObj) {
			super.updateData(returnedObj);

			//if (needReLoginToLive) {
			DataHolder.getInstance().setLiveChessMode(true);
			liveChessClientEventListener.performServiceConnection();
			//}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
		}
	}

	private class ThreadMonitorListener implements ThreadMonitor.ThreadMonitorListener {

		private static final String TAG = "LCCLOG-ThreadMonitor";

		public void onStarted(String info) {
			LogMe.dl(TAG, "onStarted: " + info);
		}

		@Override
		public void onTerminated(String info) {
			LogMe.dl(TAG, "onTerminated: " + info);
		}

		@Override
		public void onLogged(String info, java.util.Collection<java.lang.Thread> blockedThreads) {
			LogMe.dl(TAG, "onLogged: " + info);

			MoveInfo latestMoveInfo = lccHelper.getLatestMoveInfo();

			if (blockedThreads != null && latestMoveInfo != null) {
				for (Thread thread : blockedThreads) {
					if (thread.getId() == latestMoveInfo.getMoveFirstThreadId() || thread.getId() == latestMoveInfo.getMoveSecondThreadId()) {

						HashMap<String, String> params = new HashMap<String, String>();
						params.put("ThreadMonitor", info);
						params.put("Move", latestMoveInfo.toString());

						FlurryAgent.logEvent(FlurryData.MOVE_BLOCKED_THREAD_DEBUG, params);

						Exception e = new Exception(FlurryData.MOVE_BLOCKED_THREAD_DEBUG);
						BugSenseHandler.sendExceptionMap(params, e);

						//throw new RuntimeException(FlurryData.MOVE_BLOCKED_THREAD_DEBUG);
					}
				}
			}
		}

		@Override
		public void onError(String info, Exception e) {
			LogMe.dl(TAG, "onError: " + info);
			if (e != null) {
				e.printStackTrace();
			}
		}
	}
}