package com.chess.backend;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import com.chess.R;
import com.chess.backend.entity.api.ChatItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.lcc.android.*;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.lcc.android.interfaces.LccConnectionUpdateFace;
import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.lcc.android.interfaces.LiveChessClientEventListener;
import com.chess.live.client.Challenge;
import com.chess.live.client.Game;
import com.chess.live.client.LiveChessClient;
import com.chess.live.client.User;
import com.chess.model.GameLiveItem;
import com.chess.statics.AppData;
import com.chess.statics.IntentConstants;
import com.chess.statics.StaticData;
import com.chess.ui.activities.MainFragmentFaceActivity;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.utilities.LogMe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveChessService extends Service {

	private static final String TAG = "LCCLOG-LiveChessService";
	private static final long SHUTDOWN_TIMEOUT_DELAY = 15 * 60 * 1000;

	private ServiceBinder serviceBinder = new ServiceBinder();

	private LccHelper lccHelper;
	private LccConnectionUpdateFace connectionUpdateFace;
	private LccChallengeTaskRunner challengeTaskRunner;
	private LccGameTaskRunner gameTaskRunner;
	private AppData appData;
	private Handler handler;

	public class ServiceBinder extends Binder {
		public LiveChessService getService(){
			LogMe.dl(TAG, "SERVICE: getService called");
			return LiveChessService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		//registerReceiver(networkChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		appData = new AppData(this);
		handler = new Handler();
	}

	@Override
	public IBinder onBind(Intent intent) {
//		LogMe.dl(TAG, "SERVICE: onBind");
//		LogMe.dl(TAG, "lccHelper instance before check = " + lccHelper);
		if (lccHelper == null) {
			lccHelper = new LccHelper(getContext(), this, new LccConnectUpdateListener());
//			LogMe.dl(TAG, "SERVICE: helper created");
		} else {
//			LogMe.dl(TAG, "SERVICE: helper exist!");
		}
//		LogMe.dl(TAG, "lccHelper instance after check = " + lccHelper);
		return serviceBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		LogMe.dl(TAG, "SERVICE: onUnbind, this service have no binders anymore");

		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogMe.dl(TAG, "SERVICE: onStartCommand");

		return START_STICKY_COMPATIBILITY;
	}

	@Override
	public void onDestroy() {
		LogMe.dl(TAG, "SERVICE: onDestroy");
		if (lccHelper != null) {
			lccHelper.logout();
			lccHelper = null;
		}
		stopForeground(true);
		//unregisterReceiver(networkChangeReceiver);
	}

	public void checkAndConnect(LccConnectionUpdateFace connectionUpdateFace) {
		this.connectionUpdateFace = connectionUpdateFace;
		LogMe.dl(TAG, "appData.isLiveChess(getContext()) " + appData.isLiveChess());
		LogMe.dl(TAG, "lccHelper instance in checkAndConnect = " + lccHelper);
		LogMe.dl(TAG, "lccClient instance in checkAndConnect = " + lccHelper.getClient());

		LogMe.dl(TAG, "lccHelper.getClient() " + lccHelper.getClient());

		if (appData.isLiveChess() && !lccHelper.isConnected()) {
			if (lccHelper.getClient() == null || lccHelper.isConnectionFailure()) { // prevent creating several instances when user navigates between activities in "reconnecting" mode
				lccHelper.runConnectTask(true);
				LogMe.dl(TAG, "no lccClient running connection task");
			} else { // when client is connecting, but device screen was rotated for example
				LogMe.dl(TAG, "client is CONNECTING");
				//onConnecting();
			}
		} else if (lccHelper.isConnected()) {
			LogMe.dl(TAG, "connected case");
			onLiveConnected();
		} else {
			// we get here when network connection changes and we get different ip address
			//lccHelper.performConnect(true);  // probably need to be changed to create new instance of live client and perform connect

			// vm: lets avoid any manual connects here, LCC is in charge on that.

			LogMe.dl(TAG, "else case");
		}
	}

	public void onLiveConnected() {
		LogMe.dl(TAG, "onLiveConnected, connectionUpdateFace = " + connectionUpdateFace);
		if (connectionUpdateFace != null) {
			connectionUpdateFace.onConnected();
		}
	}

	public class LccConnectUpdateListener extends AbstractUpdateListener<LiveChessClient> {
		public LccConnectUpdateListener() {
			super(getContext());
		}

		@Override
		public void updateData(LiveChessClient returnedObj) {
			LogMe.dl(TAG, "LiveChessClient initialized " + returnedObj);

			// todo: tune notification
			Notification notification = new Notification(R.drawable.ic_stat_live, getString(R.string.chess_com_live),
					System.currentTimeMillis());

			Intent intent = new Intent(getContext(), MainFragmentFaceActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.putExtra(IntentConstants.LIVE_CHESS, true);

			PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 11, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			notification.setLatestEventInfo(getContext(), getString(R.string.ches_com), getString(R.string.live), pendingIntent);
			notification.flags |= Notification.FLAG_NO_CLEAR;

			startForeground(R.drawable.ic_stat_live, notification);

//			onLiveConnected(); // useless here because doesn't have user at that moment
		}
	}

	private Context getContext(){
		return this;
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
			logout();
			stopSelf();
		}
	};

//	private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(final Context context, final Intent intent) {
//
//			// todo: improve and refactor, just used old code.
//			// OtherClientEntered problem is here
//
//			if (!appData.isLiveChess()) {
//				return;
//			}
//
//			//LccHelper lccHolder = LccHelper.getInstance(context);
//
//			boolean failOver = intent.getBooleanExtra("FAIL_OVER_CONNECTION", false);
//			LogMe.dl(TAG, "NetworkChangeReceiver failOver=" + failOver);
//
//			final ConnectivityManager connectivityManager = (ConnectivityManager)
//					context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//			final NetworkInfo wifi =
//					connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//
//			final NetworkInfo mobile =
//					connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//
//			if (wifi != null && mobile != null) {
//				LogMe.dl(TAG, "NetworkChangeReceiver failOver wifi=" + wifi.isFailover() + ", mobile=" + mobile.isFailover());
//			}
//
//			NetworkInfo[] networkInfo
//					= connectivityManager.getAllNetworkInfo();
//
//			if (networkInfo != null) {
//				for (NetworkInfo aNetworkInfo : networkInfo) {
//					if (aNetworkInfo.isConnected()) {
//						LogMe.dl(TAG, "NetworkChangeReceiver isConnected " + aNetworkInfo.getTypeName());
//
//						// todo: check NPE
//						if (lccHelper.getNetworkTypeName() != null && !aNetworkInfo.getTypeName().equals(lccHelper.getNetworkTypeName())) {
//
//						/*((LiveChessClientImpl) lccHelper.getClient()).leave();
//						lccHelper.runConnectTask();*/
//
//							//setNetworkChangedNotification(true);
//							lccHelper.getContext().sendBroadcast(new Intent("com.chess.lcc.android-network-change"));
//						} else {
//							lccHelper.setNetworkTypeName(aNetworkInfo.getTypeName());
//						}
//					}
//				}
//
//			}
//		}
//	};

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


	// ------------------- Lcc Holder wrapping --------------------------
	public LccHelper getLccHelper(){
		return lccHelper;
	}

	public boolean isUserConnected() {
		return lccHelper != null && lccHelper.getUser() != null && isConnected();
	}

	/*public boolean isGameAlreadyPresent() { // todo: we have to add Current Game button and open active game or show results of previous game
		return lccHelper != null && lccHelper.isGameAlreadyPresent();
	}*/

	public boolean isActiveGamePresent() {
		return lccHelper != null && lccHelper.isActiveGamePresent();
	}

	/*public boolean isValidToMakeMove() {
		return lccHelper != null && lccHelper.getUser() != null && isConnected() && lccHelper.isActiveGamePresent();
	}*/

	public User getUser() {
		return lccHelper.getUser();
	}

	public void logout() {
		if (lccHelper != null) {
			lccHelper.logout();
		}
	}

	public void checkTestMove() {
		lccHelper.checkTestMove();
	}

	public Map<LiveEvent.Event, LiveEvent> getPausedActivityLiveEvents() {
		return lccHelper.getPausedActivityLiveEvents();
	}

	public void setConnected(boolean connected) {
		lccHelper.setConnected(connected);
	}

	public HashMap<Long, Challenge> getChallenges() {
		return lccHelper.getChallenges();
	}

	public boolean isConnected() {
		return lccHelper.isConnected();
	}

	public LiveChessClient getClient() {
		return lccHelper.getClient();
	}

	public void setLiveChessClientEventListener(LiveChessClientEventListener clientEventListener) {
		lccHelper.setLiveChessClientEventListener(clientEventListener);
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

	/*public void processFullGame() {
		lccHelper.processFullGame();
	}

	public int getOwnSeeksCount() {
		return lccHelper.getOwnSeeksCount();
	}

	public String[] getOnlineFriends() {
		return lccHelper.getOnlineFriends();
	}*/

	public GameLiveItem getGameItem() {
		return lccHelper.getGameItem();
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

	public void setLatestMoveNumber(int latestMoveNumber) {
		lccHelper.setLatestMoveNumber(latestMoveNumber);
	}

	public Boolean isUserColorWhite() {
		return lccHelper.isUserColorWhite();
	}

	public boolean isFairPlayRestriction() {
		return lccHelper.isFairPlayRestriction();
	}

	public Game getLastGame() {
		return lccHelper.getLastGame();
	}

	public void makeMove(String move, String temporaryDebugInfo) {
		lccHelper.makeMove(move, gameTaskRunner, temporaryDebugInfo);
	}

	public void paintClocks() {
		lccHelper.paintClocks();
	}

	public void rematch() {
		lccHelper.rematch();
	}

	public void checkGameEvents() {
		lccHelper.checkGameEvents();
	}

	public void createChallenge(LiveGameConfig config) {
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
		lccHelper.setCurrentGameId(null);
		lccHelper.stopClock();
		lccHelper.unObserveCurrentObservingGame();
		lccHelper.setCurrentObservedGameId(null);
	}
}