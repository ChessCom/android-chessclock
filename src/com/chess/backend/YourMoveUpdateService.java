package com.chess.backend;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
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

	private static final long UPDATE_TIMEOUT = 60000; // 1 minute

	private Handler handler;
	private String userToken;


	@Override
	public void onCreate() {
		super.onCreate();
		handler = new Handler();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		checkStatusUpdate(AppData.getInstance().getUserToken(this));
		return START_STICKY_COMPATIBILITY;
	}

	private void checkStatusUpdate(String userToken){
		this.userToken = userToken;
		if(!userToken.equals(AppConstants.SYMBOL_EMPTY)){
			new UpdateStatusTask(this).execute(userToken);
			handler.postDelayed(updateRunnable, UPDATE_TIMEOUT);
		}else
			 handler.removeCallbacks(updateRunnable);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	private Runnable updateRunnable = new  Runnable() {

		@Override
		public void run() {
			new UpdateStatusTask(YourMoveUpdateService.this).execute(userToken);
			handler.postDelayed(this, UPDATE_TIMEOUT);
		}
	};
}
