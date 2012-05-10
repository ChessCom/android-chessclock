package com.chess.backend;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import com.chess.backend.entity.AppData;
import com.chess.backend.tasks.UpdateStatusTask;

/**
 * YourMoveUpdateService class
 *
 * @author alien_roger
 * @created at: 21.04.12 7:06
 */
public class YourMoveUpdateService extends Service {

	private static final long UPDATE_TIMEOUT = 120000; // 2 minutes

	private Handler handler;

	@Override
	public void onCreate() {
		super.onCreate();
		handler = new Handler();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("YourMoveUpdateService", "starting service");
		checkStatusUpdate();
		return START_REDELIVER_INTENT;
	}

	private void checkStatusUpdate(){
		new UpdateStatusTask(this).execute(AppData.getUserToken(this));
		handler.removeCallbacks(updateRunnable);
		handler.postDelayed(updateRunnable, UPDATE_TIMEOUT);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		handler.removeCallbacks(updateRunnable);
		super.onDestroy();
	}

	private Runnable updateRunnable = new  Runnable() {

		@Override
		public void run() {
			new UpdateStatusTask(YourMoveUpdateService.this).execute(AppData.getUserToken(YourMoveUpdateService.this));
			handler.removeCallbacks(this);
			handler.postDelayed(this, UPDATE_TIMEOUT);
		}
	};
}
