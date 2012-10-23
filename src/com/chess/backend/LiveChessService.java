package com.chess.backend;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.tasks.ConnectLiveChessTask;
import com.chess.live.client.LiveChessClient;

/**
 * @author Alexey Schekin (schekin@azoft.com)
 * @created 23.10.12
 * @modified 23.10.12
 */
public class LiveChessService extends Service {

	private static final String TAG = "LiveChessService";

	private ServiceBinder serviceBinder = new ServiceBinder();


	public class ServiceBinder extends Binder {
		public LiveChessService getService(){
			return LiveChessService.this;
		}
	}

	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return START_CONTINUATION_MASK;
	}

	public void checkAndConnect() {
		// TODO instead of singleton storage we will use sharedPreferences to store boolean value when we are connected.
		// It's more reliable but we should update it properly

//		runConnectTask(); // TODO use this to start connection
//		if(DataHolder.getInstance().isLiveChess() && !connected && lccClient == null){
//			LccHolder.getInstance(context).runConnectTask();
//		}

		//TODO change body of created methods use File | Settings | File Templates.
	}


	public void runConnectTask() {
		new ConnectLiveChessTask(new LccConnectUpdateListener()).executeTask();
	}

	public class LccConnectUpdateListener extends AbstractUpdateListener<LiveChessClient> {
		public LccConnectUpdateListener() {
			super(getContext());
		}

		@Override
		public void updateData(LiveChessClient returnedObj) {
			Log.d(TAG, "LiveChessClient initialized");
//			lccClient = returnedObj;
		}
	}

	private Context getContext(){
		return this;
	}
}

