package com.chess.core;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.chess.R;
import com.chess.activities.Singin;
import com.chess.utilities.Notifications;

public class StartActivity extends CoreActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);

		//defaults
		mainApp.LoadBoard(mainApp.res_boards[mainApp.getSharedData().getInt(mainApp.getSharedData().getString("username", "") + "board", 8)]);
		mainApp.LoadPieces(mainApp.res_pieces[mainApp.getSharedData().getInt(mainApp.getSharedData().getString("username", "") + "pieces", 0)]);
		mainApp.loadCapturedPieces();

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(1);

//		Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

		LoadNext(0);
	}

	@Override
	public void LoadPrev(int code) {
		finish();
	}

	@Override
	public void LoadNext(int code) {
		if (mainApp.getSharedData().getString("user_token", "").equals("")) {
			startActivity(new Intent(this, Singin.class));
			mainApp.guest = true;
		} else {
			if (mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString("username", "") + "notifE", true))
				startService(new Intent(this, Notifications.class));

			boolean fromnotif = false;
			if (extras != null && extras.getBoolean("fromnotif")) {
				fromnotif = true;
				Notifications.resetCounter();
			}

			startActivity(new Intent(this, Tabs.class).putExtra("fromnotif", fromnotif));
			mainApp.guest = false;
		}
		finish();
	}

	@Override
	public void Update(int code) {
	}

//	public class TopExceptionHandler implements Thread.UncaughtExceptionHandler {
//		Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;
//
//		public TopExceptionHandler() {
//			defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
//		}
//
//		public void uncaughtException(Thread t, Throwable e) {
//			defaultUncaughtExceptionHandler.uncaughtException(t, e);
//			MobclixHelper.getAdTimer().cancel();
//			enableScreenLock();
//		}
//	}
}
