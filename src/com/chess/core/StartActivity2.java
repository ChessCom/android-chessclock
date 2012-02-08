package com.chess.core;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.chess.R;
import com.chess.activities.HomeScreenActivity;
import com.chess.activities.LoginScreenActivity;
import com.chess.utilities.Notifications;

public class StartActivity2 extends CoreActivity2 {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);

		//defaults
		mainApp.LoadBoard(mainApp.res_boards[mainApp.getSharedData()
				.getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")
						+ AppConstants.PREF_BOARD_TYPE, 8)]);
		mainApp.LoadPieces(mainApp.res_pieces[mainApp.getSharedData()
				.getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")
						+ AppConstants.PREF_PIECES_SET, 0)]);
		mainApp.loadCapturedPieces();

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(1);

//		Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

		if (mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "").equals("")) {
			startActivity(new Intent(this, LoginScreenActivity.class));
			mainApp.guest = true;
		} else {
			if (mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_NOTIFICATION, true))
				startService(new Intent(this, Notifications.class));

			boolean fromnotif = false;
			if (extras != null && extras.getBoolean(AppConstants.ENTER_FROM_NOTIFICATION)) {
				fromnotif = true;
				Notifications.resetCounter();
			}

			startActivity(new Intent(this, HomeScreenActivity.class).putExtra(AppConstants.ENTER_FROM_NOTIFICATION, fromnotif));
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
