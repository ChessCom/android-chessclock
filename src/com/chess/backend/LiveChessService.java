package com.chess.backend;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
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
import com.chess.statics.IntentConstants;
import com.chess.statics.StaticData;
import com.chess.ui.activities.MainFragmentFaceActivity;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.utilities.LogMe;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveChessService extends Service {

	private static final String TAG = "LCCLOG-LiveChessService";
	private static final int GO_TO_LIVE = 11;

	private ServiceBinder serviceBinder = new ServiceBinder();

	private LccChallengeTaskRunner challengeTaskRunner;
	private LccGameTaskRunner gameTaskRunner;
	LiveConnectionHelper liveConnectionHelper;

	public class ServiceBinder extends Binder {
		public LiveChessService getService() {
			LogMe.dl(TAG, "SERVICE: getService called");
			return LiveChessService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (liveConnectionHelper == null) {
			liveConnectionHelper = new LiveConnectionHelper(this);
		}
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

		if (liveConnectionHelper == null) {
			liveConnectionHelper = new LiveConnectionHelper(this);
		}
		if (liveConnectionHelper.isLiveChessEventListenerSet()) {
			liveConnectionHelper.checkAndConnectLiveClient();
		}

		return START_STICKY_COMPATIBILITY;
	}

	@Override
	public void onDestroy() {
		LogMe.dl(TAG, "SERVICE: onDestroy");
		if (liveConnectionHelper != null) {
			logout();
			liveConnectionHelper = null;
		}
		stopForeground(true);
		//unregisterReceiver(networkChangeReceiver);
	}

	public void stop() {
		stopSelf();
		stopForeground(true);
	}

	public void checkAndConnect(LccConnectionUpdateFace connectionUpdateFace) {
//		LogMe.dl(TAG, "appData.isLiveChess(getContext()) " + appData.isLiveChess());
		LogMe.dl(TAG, "liveConnectionHelper instance in checkAndConnect = " + liveConnectionHelper);
		LogMe.dl(TAG, "lccClient instance in checkAndConnect = " + liveConnectionHelper.getClient());

		liveConnectionHelper.setConnectionUpdateFace(connectionUpdateFace);
		liveConnectionHelper.checkAndConnectLiveClient();
	}

	public void onLiveConnected() {

		// Show status bar for ongoing notification
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

		Intent notifyIntent = new Intent(getContext(), MainFragmentFaceActivity.class);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notifyIntent.putExtra(IntentConstants.LIVE_CHESS, true);

		// Creates the PendingIntent
		PendingIntent pendingIntent = PendingIntent.getActivity(this, GO_TO_LIVE, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Bitmap bigImage = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_stat_chess)).getBitmap();
		String title = getString(R.string.live_chess_connection);
		String body = getString(R.string.live_chess_connected_description);

		notificationBuilder.setContentTitle(title)
				.setTicker(title)
				.setContentText(body)
				.setSmallIcon(R.drawable.ic_stat_live)
				.setLargeIcon(bigImage);

		// Puts the PendingIntent into the notification builder
		notificationBuilder.setContentIntent(pendingIntent);
		notificationBuilder.setOngoing(true);

		startForeground(R.drawable.ic_stat_live, notificationBuilder.build());
	}

	private Context getContext() {
		return this;
	}

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

	// ------------------- Task runners wrapping ------------------------

	public void setChallengeTaskListener(AbstractUpdateListener<Challenge> challengeTaskListener) {
		challengeTaskRunner = new LccChallengeTaskRunner(challengeTaskListener, getLccHelper());
	}

	public void declineAllChallenges(Challenge currentChallenge) {
		challengeTaskRunner.declineAllChallenges(currentChallenge, getLccHelper().getChallenges());
	}

	public void runAcceptChallengeTask(Challenge currentChallenge) {
		challengeTaskRunner.runAcceptChallengeTask(currentChallenge);
	}

	public void declineCurrentChallenge(Challenge currentChallenge) {
		challengeTaskRunner.declineCurrentChallenge(currentChallenge, getLccHelper().getChallenges());
	}

	public void cancelAllOwnChallenges() {
		challengeTaskRunner.cancelAllOwnChallenges(getLccHelper().getOwnChallenges());
	}

	public void runSendChallengeTask(Challenge challenge) {
		challengeTaskRunner.runSendChallengeTask(challenge);
	}

	public void setGameTaskListener(ActionBarUpdateListener<Game> gameTaskListener) {
		gameTaskRunner = new LccGameTaskRunner(gameTaskListener, getLccHelper());
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


	// ------------------- Helpers wrapping --------------------------
	public LccHelper getLccHelper() {
		return liveConnectionHelper != null ? liveConnectionHelper.getLccHelper() : null;
	}

	public boolean isUserConnected() {
		return getLccHelper() != null && getLccHelper() != null && isConnected();
	}

	public boolean isActiveGamePresent() {
		return getLccHelper() != null && getLccHelper().isActiveGamePresent();
	}

	/*public boolean isValidToMakeMove() {
		return lccHelper != null && lccHelper.getUser() != null && isConnected() && lccHelper.isActiveGamePresent();
	}*/

	public User getUser() {
		return getLccHelper().getUser();
	}

	public void logout() {
		if (liveConnectionHelper != null) {
			liveConnectionHelper.logout();
		}
	}

	public void checkTestMove() {
		getLccHelper().checkTestMove();
	}

	public Map<LiveEvent.Event, LiveEvent> getPausedActivityLiveEvents() {
		return liveConnectionHelper.getPausedActivityLiveEvents();
	}

	public void setConnected(boolean connected) {
		liveConnectionHelper.setConnected(connected);
	}

	public HashMap<Long, Challenge> getChallenges() {
		return getLccHelper().getChallenges();
	}

	public boolean isConnected() {
		return liveConnectionHelper.isConnected();
	}

	public LiveChessClient getClient() {
		return getLccHelper().getClient();
	}

	public void setLiveChessClientEventListener(LiveChessClientEventListener clientEventListener) {
		liveConnectionHelper.setLiveChessClientEventListener(clientEventListener);
	}

	public void setOuterChallengeListener(OuterChallengeListener outerChallengeListener) {
		getLccHelper().setOuterChallengeListener(outerChallengeListener);
	}

	public void setLccChatMessageListener(LccChatMessageListener chatMessageListener) {
		getLccHelper().setLccChatMessageListener(chatMessageListener);
	}

	public void setLccEventListener(LccEventListener eventListener) {
		getLccHelper().setLccEventListener(eventListener);
	}

	public void setLccObserveEventListener(LccEventListener eventListener) {
		getLccHelper().setLccObserveEventListener(eventListener);
	}

	public List<ChatItem> getMessagesList() {
		return getLccHelper().getMessagesList();
	}

	public Long getCurrentGameId() {
		return getLccHelper().getCurrentGameId();
	}

	public String[] getOnlineFriends() {
		return getLccHelper().getOnlineFriends();
	}

	public GameLiveItem getGameItem() {
		return getLccHelper().getGameItem();
	}

	public int getResignTitle() {
		return getLccHelper().getResignTitle();
	}

	public List<String> getPendingWarnings() {
		return getLccHelper().getPendingWarnings();
	}

	public String getLastWarningMessage() {
		return getLccHelper().getLastWarningMessage();
	}

	public void setGameActivityPausedMode(boolean gameActivityPausedMode) {
		getLccHelper().setGameActivityPausedMode(gameActivityPausedMode);
	}

	public void checkAndReplayMoves() {
		getLccHelper().checkAndReplayMoves();
	}

	public void checkFirstTestMove() {
		getLccHelper().checkFirstTestMove();
	}

	public String getUsername() {
		return getLccHelper().getUsername();
	}

	public Game getCurrentGame() {
		return getLccHelper().getCurrentGame();
	}

	public Integer getGamesCount() {
		return getLccHelper().getGamesCount();
	}

	public Integer getLatestMoveNumber() {
		return getLccHelper().getLatestMoveNumber();
	}

	public Boolean isUserColorWhite() {
		return getLccHelper().isUserColorWhite();
	}

	public boolean isFairPlayRestriction() {
		return getLccHelper().isFairPlayRestriction();
	}

	public void makeMove(String move, String temporaryDebugInfo) {
		getLccHelper().makeMove(move, gameTaskRunner, temporaryDebugInfo);
	}

	public void updatePlayersClock() {
		getLccHelper().updatePlayersClock();
	}

	public void rematch() {
		getLccHelper().rematch();
	}

	public void checkGameEvents() {
		getLccHelper().checkGameEvents();
	}

	public void createChallenge(LiveGameConfig config) {
		Gson gson = new Gson();
		LogMe.dl("TEST", "live config = " + gson.toJson(config));
		getLccHelper().createChallenge(config);
	}

	public void sendMessage(String message, TaskUpdateInterface<String> taskFace) {
		new SendLiveMessageTask(taskFace, message).executeTask(getLccHelper().getCurrentGameId());
	}

private class SendLiveMessageTask extends AbstractUpdateTask<String, Long> {

	private String message;

	public SendLiveMessageTask(TaskUpdateInterface<String> taskFace, String message) {
		super(taskFace);
		this.message = message;
	}

	@Override
	protected Integer doTheTask(Long... params) {
		getLccHelper().sendChatMessage(params[0], message);
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
		getLccHelper().observeTopGame();
		return StaticData.RESULT_OK;
	}
}
	public void exitGameObserving() {
		LogMe.dl(TAG, "exitGameObserving");
		setLccObserveEventListener(null);
		getLccHelper().setCurrentGameId(null);
		getLccHelper().stopClocks();
		getLccHelper().unObserveCurrentObservingGame();
		getLccHelper().setCurrentObservedGameId(null);
	}

	public void initClocks() {
		getLccHelper().initClocks();
	}

	public void stopClocks() {
		getLccHelper().stopClocks();
	}

	public boolean isCurrentGameObserved() {
		Game currentGame = getLccHelper().getCurrentGame();
		boolean isCurrentGameObserved = currentGame != null && getLccHelper().isObservedGame(currentGame);

		return isCurrentGameObserved;
	}

	public Long getCurrentObservedGameId() {
		return getLccHelper().getCurrentObservedGameId();
	}

	public void startIdleTimeOutCounter() {
		liveConnectionHelper.startIdleTimeOutCounter();
	}

	public void stopIdleTimeOutCounter() {
		liveConnectionHelper.stopIdleTimeOutCounter();
	}

}