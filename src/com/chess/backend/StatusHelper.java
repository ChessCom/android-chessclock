package com.chess.backend;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import com.chess.backend.entity.AppData;
import com.chess.backend.tasks.UpdateStatusTask;
import com.chess.ui.core.AppConstants;

/**
 * StatusHelper class
 *
 * @author alien_roger
 * @created at: 21.04.12 7:06
 */
public class StatusHelper extends Service {


	private static final long UPDATE_TIMEOUT = 60000; // 1 minute

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		checkStatusUpdate(AppData.getInstance().getUserToken(this));
		return START_STICKY_COMPATIBILITY;
	}

	private void checkStatusUpdate(/*MainApp mainApp, Context context*/String userToken){
//		String userToken = mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY);
		if(/*!mainApp.guest && */!userToken.equals(AppConstants.SYMBOL_EMPTY)){
			new UpdateStatusTask(this).execute(userToken);
			new Handler().postDelayed(new UpdateRunnable(this, userToken), UPDATE_TIMEOUT);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class UpdateRunnable implements Runnable {
		private Context context;
		private String userToken;

		private UpdateRunnable(Context context, String userToken) {
			this.context = context;
			this.userToken = userToken;
		}

		@Override
		public void run() {
			new UpdateStatusTask(context).execute(userToken);
		}
	}
}
