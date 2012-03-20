package com.chess.utilities;

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
import com.chess.core.AppConstants;
import com.chess.core.MainApp;
import com.chess.core.SplashActivity;
import com.chess.lcc.android.LccHolder;

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

		timer.scheduleAtFixedRate(
				new TimerTask() {
					private String response = ""
							,
							notification_message = ""
							,
							timestamp = "";

					public void run() {
						if (mainApp == null || mainApp.getSharedData() == null) {
							return;
						}
						if (/*(counter <= 5 || (counter%15 == 0 && counter <= 60) || counter%60 == 0) && */!mainApp.guest && !mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "").equals("")) {
							response = Web.Request("http://www." + LccHolder.HOST + "/api/get_move_status?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, ""), "GET", null, null);
							if (response.trim().contains("Success+1")) {
								/*String[] tmp = response.trim().split(":");
														notification_message = tmp[1];
														if(!tmp[2].equals(timestamp)){
															timestamp = tmp[2];*/
								handler.sendEmptyMessage(0);
								//resetCounter();
								//}
							} else {
								clear.sendEmptyMessage(0);
								//resetCounter();
							}
						}
						//counter++;
					}

					private Handler handler = new Handler() {
						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
							NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
							Notification notification = new Notification(R.drawable.ccpawn32x32, "ChessCom", System.currentTimeMillis());

							//int Notif = mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")+"notif", 0);

							/*switch (Notif) {
						  case 0:
							  break;
						  case 1:
							  notification.defaults |= Notification.DEFAULT_VIBRATE;
							  break;
						  case 2:
							  notification.defaults |= Notification.DEFAULT_SOUND;
							  break;
						  case 3:
							  notification.ledARGB = 0xff00ff00;
							  notification.ledOnMS = 300;
							  notification.ledOffMS = 3000;
							  notification.flags |= Notification.FLAG_SHOW_LIGHTS;
						  break;

						  default: break;
					  }*/
//							PendingIntent contentIntent = PendingIntent.getActivity(Notifications.this, 0, new Intent(Notifications.this, SplashActivity.class).putExtra(AppConstants.ENTER_FROM_NOTIFICATION, true), 0);
							Intent intent = new Intent(Notifications.this, SplashActivity.class);
							intent.putExtra(AppConstants.ENTER_FROM_NOTIFICATION, true);
							PendingIntent contentIntent = PendingIntent.getActivity(Notifications.this, 0, intent, 0);
							notification.setLatestEventInfo(getApplicationContext(), "ChessCom", notification_message, contentIntent);
							mNotificationManager.notify(1, notification);
						}
					};
					private Handler clear = new Handler() {
						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
							NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
							mNotificationManager.cancel(1);
						}
					};

				}, 0, 30000);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (timer != null) timer.cancel();
	}

	public static void resetCounter() {
		//counter = 0;

	}
}
