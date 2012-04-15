package com.chess.ui.core;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.chess.R;
import com.chess.ui.activities.HomeScreenActivity;
import com.chess.ui.activities.LoginScreenActivity;
import com.chess.utilities.Notifications;

public class SplashActivity extends CoreActivity {

	private View progressView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);

		progressView = findViewById(R.id.progressView);
		//defaults
		mainApp.loadBoard(mainApp.res_boards[mainApp.getSharedData().getInt(mainApp.getUserName()
						+ AppConstants.PREF_BOARD_TYPE, 8)], progressView);

//		mainApp.loadPieces(mainApp.res_pieces[mainApp.getSharedData().getInt(mainApp.getUserName()
//				+ AppConstants.PREF_PIECES_SET, 0)], progressView);
		mainApp.loadPieces(mainApp.getSharedData().getInt(mainApp.getUserName()
				+ AppConstants.PREF_PIECES_SET, 0), progressView);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationManager.cancel(1);

		if (mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY).equals(AppConstants.SYMBOL_EMPTY)) {
			startActivity(new Intent(this, LoginScreenActivity.class));
			mainApp.guest = true;
		} else {
			if (mainApp.getSharedData().getBoolean(mainApp.getUserName() + AppConstants.PREF_NOTIFICATION, true))
				startService(new Intent(this, Notifications.class));

			boolean fromNotification = false;
			if (extras != null && extras.getBoolean(AppConstants.ENTER_FROM_NOTIFICATION)) {
				fromNotification = true;
				Notifications.resetCounter();
			}

			startActivity(new Intent(this, HomeScreenActivity.class)
					.putExtra(AppConstants.ENTER_FROM_NOTIFICATION, fromNotification));
			mainApp.guest = false;
		}
		finish();
	}

	@Override
	public void update(int code) {
	}

}
