package com.chess.backend;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import com.chess.backend.entity.AppData;
import com.chess.backend.tasks.UpdateStatusTask;
import com.chess.ui.core.AppConstants;

/**
 * YourMoveUpdateService class
 *
 * @author alien_roger
 * @created at: 21.04.12 7:06
 */
public class YourMoveUpdateService extends Service {

	private static final long UPDATE_TIMEOUT = 120000; // 2 minutes

	private Handler handler;
	private String savedUserToken;


	@Override
	public void onCreate() {
		super.onCreate();
		handler = new Handler();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("YourMoveUpdateService", "starting service");
		checkStatusUpdate(AppData.getUserToken(this));
		return START_REDELIVER_INTENT;
	}

	private void checkStatusUpdate(String userToken){
		if(!userToken.equals(savedUserToken)){
			this.savedUserToken = userToken;
			if(!userToken.equals(AppConstants.SYMBOL_EMPTY)){
				new UpdateStatusTask(this).execute(userToken);
				handler.postDelayed(updateRunnable, UPDATE_TIMEOUT);
			}else {
				handler.removeCallbacks(updateRunnable);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		Log.d("YourMoveUpdateService", "killing service");
		handler.removeCallbacks(updateRunnable);
		super.onDestroy();
	}

	private Runnable updateRunnable = new  Runnable() {

		@Override
		public void run() {

			new UpdateStatusTask(YourMoveUpdateService.this).execute(savedUserToken);
			handler.removeCallbacks(this);
			handler.postDelayed(this, UPDATE_TIMEOUT);
		}
	};
}
