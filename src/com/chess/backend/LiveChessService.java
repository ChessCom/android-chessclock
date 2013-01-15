package com.chess.backend;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.chess.R;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.ConnectLiveChessTask;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.LiveChessClient;
import com.chess.ui.activities.LiveScreenActivity;

public class LiveChessService extends Service {

	private static final String TAG = "LCCLOG-LiveChessService";

	boolean liveConnected; // it is better to keep this state inside service instead of preferences appdata

	private ServiceBinder serviceBinder = new ServiceBinder();

	public class ServiceBinder extends Binder {
		public LiveChessService getService(){
			return LiveChessService.this;
		}
	}

	public IBinder onBind(Intent intent) {
		Log.d(TAG, "SERVICE: onBind");
		return serviceBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "SERVICE: onStartCommand");

		checkAndConnect();

		//return START_STICKY;
		return START_CONTINUATION_MASK;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "SERVICE: onDestroy");
		stopForeground(true);
	}

	public void checkAndConnect() {
		Log.d(TAG, "AppData.isLiveChess(getContext()) " + AppData.isLiveChess(getContext()));
		Log.d(TAG, "AppData.isLiveConnected(getContext()) " + liveConnected);
		Log.d(TAG, "AppData.isLiveConnected(getContext()) " + LccHolder.getInstance(getContext()).getClient());

		if (AppData.isLiveChess(getContext()) && !liveConnected
				/*&& LccHolder.getInstance(getContext()).getClient() == null*/) {
			runConnectTask();
		}
	}

	public void runConnectTask() {
		Log.d(TAG, "SERVICE: runConnectTask");
		new ConnectLiveChessTask(new LccConnectUpdateListener()).executeTask();
	}

	public void runConnectTask(boolean forceReenterCred) {
		Log.d(TAG, "SERVICE: runConnectTask");
		new ConnectLiveChessTask(new LccConnectUpdateListener(), forceReenterCred).executeTask();
	}

	public class LccConnectUpdateListener extends AbstractUpdateListener<LiveChessClient> {
		public LccConnectUpdateListener() {
			super(getContext());
		}

		@Override
		public void updateData(LiveChessClient returnedObj) {
			Log.d(TAG, "LiveChessClient initialized " + returnedObj);

			// todo: tune notification
			Notification notification = new Notification(R.drawable.player_indicator_online, // just test. change it
					"Chess.com Live",
					System.currentTimeMillis());

			Intent intent = new Intent(getContext(), LiveScreenActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

			PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);

			notification.setLatestEventInfo(getContext(), "Chess.com", "Live Online", pendingIntent);
			notification.flags |= Notification.FLAG_NO_CLEAR;

			startForeground(2048, notification);
		}
	}

	private Context getContext(){
		return this;
	}

	public boolean isLiveConnected() {
		return liveConnected;
	}

	public void setLiveConnected(boolean liveConnected) {
		this.liveConnected = liveConnected;
	}
}
