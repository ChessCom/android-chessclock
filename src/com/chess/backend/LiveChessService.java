package com.chess.backend;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.chess.R;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
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
import com.chess.model.MessageItem;
import com.chess.ui.activities.LiveScreenActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveChessService extends Service {

	private static final String TAG = "LCCLOG-LiveChessService";

	private ServiceBinder serviceBinder = new ServiceBinder();

	// or move holder code to Service itself.
	// but in this case we should have ability to reset holder data when it is necessary, for instance logout
	private LccHelper lccHelper;
	private LccConnectionUpdateFace connectionUpdateFace;
	private LccChallengeTaskRunner challengeTaskRunner;
	private LccGameTaskRunner gameTaskRunner;

	public class ServiceBinder extends Binder {
		public LiveChessService getService(){
			Log.d(TAG, "SERVICE: getService called");
			return LiveChessService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		//registerReceiver(networkChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "SERVICE: onBind");
		Log.d(TAG, "lccHelper instance before check = " + lccHelper);
		if (lccHelper == null) {
			lccHelper = new LccHelper(getContext(), this, new LccConnectUpdateListener());
			Log.d(TAG, "SERVICE: helper created");
		} else {
			Log.d(TAG, "SERVICE: helper exist!");
		}
		Log.d(TAG, "lccHelper instance after check = " + lccHelper);
		return serviceBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "SERVICE: onUnbind , this service have no binders anymore");

		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "SERVICE: onStartCommand");

		return START_STICKY_COMPATIBILITY;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "SERVICE: onDestroy");
		if (lccHelper != null) {
			lccHelper.logout();
			lccHelper = null;
		}
		stopForeground(true);
		//unregisterReceiver(networkChangeReceiver);
	}

	public void setConnectionUpdateFace(LccConnectionUpdateFace connectionUpdateFace) {
		this.connectionUpdateFace = connectionUpdateFace;
	}

	public void checkAndConnect(LccConnectionUpdateFace connectionUpdateFace) {
		setConnectionUpdateFace(connectionUpdateFace);
		Log.d(TAG, "AppData.isLiveChess(getContext()) " + AppData.isLiveChess(getContext()));
		Log.d(TAG, "lccHelper instance in checkAndConnect = " + lccHelper);
		Log.d(TAG, "lccClient instance in checkAndConnect = " +  lccHelper.getClient());

		Log.d(TAG, "lccHelper.getClient() " + lccHelper.getClient());

		if (AppData.isLiveChess(getContext()) && !lccHelper.isConnected()
				&& lccHelper.getClient() == null) { // prevent creating several instances when user navigates between activities in "reconnecting" mode
			lccHelper.runConnectTask();
			Log.d("lccClient", "no lccClient running connection task");
		} else if (lccHelper.isConnected()) {
			Log.d("lccClient", "connected case");
			onLiveConnected();
		} else {
			// we get here when network connection changes and we get different ip address
			//lccHelper.performConnect(true);  // probably need to be changed to create new instance of live client and perform connect

			// vm: lets avoid any manual connects here, LCC is in charge on that.

			Log.d("lccClient", "else case");
		}
	}

	public void onLiveConnected() {
		Log.d(TAG, "onLiveConnected, connectionUpdateFace = " + connectionUpdateFace);
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
			Log.d(TAG, "LiveChessClient initialized " + returnedObj);

			// todo: tune notification
			Notification notification = new Notification(R.drawable.ic_stat_live, getString(R.string.chess_com_live),
					System.currentTimeMillis());

			Intent intent = new Intent(getContext(), LiveScreenActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

			PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);

			notification.setLatestEventInfo(getContext(), getString(R.string.ches_com), getString(R.string.live), pendingIntent);
			notification.flags |= Notification.FLAG_NO_CLEAR;

			startForeground(R.drawable.ic_stat_live, notification);

//			onLiveConnected();
		}
	}

	private Context getContext(){
		return this;
	}

	private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {

			// todo: improve and refactor, just used old code.
			// OtherClientEntered problem is here

			if (!AppData.isLiveChess(context)) {
				return;
			}

			//LccHelper lccHolder = LccHelper.getInstance(context);

			boolean failover = intent.getBooleanExtra("FAILOVER_CONNECTION", false);
			Log.d(TAG, "NetworkChangeReceiver failover=" + failover);

			final ConnectivityManager connectivityManager = (ConnectivityManager)
					context.getSystemService(Context.CONNECTIVITY_SERVICE);

			final android.net.NetworkInfo wifi =
					connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			final android.net.NetworkInfo mobile =
					connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			Log.d(TAG, "NetworkChangeReceiver failover wifi=" + wifi.isFailover() + ", mobile=" + mobile.isFailover());

			NetworkInfo[] networkInfo
					= connectivityManager.getAllNetworkInfo();

			for (int i = 0; i < networkInfo.length; i++) {
				if (networkInfo[i].isConnected()) {
					Log.d(TAG,  "NetworkChangeReceiver isConnected " + networkInfo[i].getTypeName());

					// todo: check NPE
					if (lccHelper.getNetworkTypeName() != null && !networkInfo[i].getTypeName().equals(lccHelper.getNetworkTypeName())) {

						/*((LiveChessClientImpl) lccHelper.getClient()).leave();
						lccHelper.runConnectTask();*/

						//setNetworkChangedNotification(true);
						lccHelper.getContext().sendBroadcast(new Intent("com.chess.lcc.android-network-change"));
					} else {
						lccHelper.setNetworkTypeName(networkInfo[i].getTypeName());
					}
				}
			}
		}
	};

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

	public void runSendChallengeTask(Challenge challenge) {
		challengeTaskRunner.runSendChallengeTask(challenge);
	}

	public void setGameTaskListener(ActionBarUpdateListener<Game> gameTaskListener) {
		gameTaskRunner = new LccGameTaskRunner(gameTaskListener, lccHelper);
	}

	public void checkTestMove() {
		lccHelper.checkTestMove();
	}

	public void runMakeDrawTask() {
		gameTaskRunner.runMakeDrawTask();
	}

	public void runMakeResignTask() {
		gameTaskRunner.runMakeResignTask();
	}

	public void runAbortGameTask() {
		gameTaskRunner.runAbortGameTask();
	}

	public void runRejectDrawTask() {
		gameTaskRunner.runRejectDrawTask();
	}


	// ------------------- Lcc Holder wrapping --------------------------
	public User getUser() {
		return lccHelper.getUser();
	}

	public void logout() {
		lccHelper.logout();
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

	public boolean currentGameExist() {
		return lccHelper.currentGameExist();
	}

	public void setLccEventListener(LccEventListener eventListener) {
		lccHelper.setLccEventListener(eventListener);

	}

	public List<MessageItem> getMessagesList() {
		return lccHelper.getMessagesList();
	}

	public Long getCurrentGameId() {
		return lccHelper.getCurrentGameId();
	}

	public void checkAndProcessFullGame() {
		lccHelper.checkAndProcessFullGame();

	}

	public int getOwnSeeksCount() {
		return lccHelper.getOwnSeeksCount();
	}

	public String[] getOnlineFriends() {
		return lccHelper.getOnlineFriends();
	}

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

	public void executePausedActivityGameEvents() {
		lccHelper.executePausedActivityGameEvents();
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

	public boolean isAbortableBySeq() {
		return lccHelper.isAbortableBySeq();
	}

	public void rematch() {
		lccHelper.rematch();
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
}
