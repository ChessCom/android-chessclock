package com.chess.backend;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.MainApp;
import com.chess.ui.core.SplashActivity;

import java.util.Timer;
import java.util.TimerTask;

public class Notifications extends Service {

	public MainApp mainApp;
	public Timer timer = new Timer();

	//private static int counter = 0;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mainApp = (MainApp) getApplication();

		timer.scheduleAtFixedRate(notificationTimerTask, 0, 30000);
	}

	private TimerTask notificationTimerTask = new TimerTask() {
		private String response = AppConstants.SYMBOL_EMPTY;
		private String notification_message = AppConstants.SYMBOL_EMPTY;
		private String timestamp = AppConstants.SYMBOL_EMPTY;

		public void run() {
			if (mainApp == null || mainApp.getSharedData() == null) {
				return;
			}
			if (/*(counter <= 5 || (counter%15 == 0 && counter <= 60) || counter%60 == 0) && */
					!mainApp.guest && !mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY).equals(AppConstants.SYMBOL_EMPTY)) {
				response = Web.Request("http://www." + LccHolder.HOST
                        + "/api/get_move_status?id="
                        + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY), "GET", null, null);
				if (response.trim().contains("Success+1")) {

					handler.sendEmptyMessage(0);
				} else {
					clear.sendEmptyMessage(0);
				}
			}
		}

		private Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				Notification notification = new Notification(R.drawable.ic_stat_chess, "ChessCom", System.currentTimeMillis());

				Intent intent = new Intent(Notifications.this, SplashActivity.class);
				intent.putExtra(AppConstants.ENTER_FROM_NOTIFICATION, true);
				PendingIntent contentIntent = PendingIntent.getActivity(Notifications.this, 0, intent, 0);
				notification.setLatestEventInfo(getApplicationContext(), "ChessCom", notification_message, contentIntent);
				mNotificationManager.notify(R.id.notification_message, notification);
			}
		};
		private Handler clear = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.cancel(R.id.notification_message);
			}
		};

	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (timer != null)
			timer.cancel();
	}

}
