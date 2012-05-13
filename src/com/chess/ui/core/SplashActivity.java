package com.chess.ui.core;

import android.content.Intent;
import android.os.Bundle;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.ui.activities.HomeScreenActivity;
import com.chess.ui.activities.LoginScreenActivity;
import com.chess.utilities.AppUtils;

public class SplashActivity extends CoreActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);

		//defaults
		mainApp.loadBoard(mainApp.res_boards[mainApp.getSharedData().getInt(AppData.getUserName(getContext())
						+ AppConstants.PREF_BOARD_TYPE, 8)]);

		mainApp.loadPieces(mainApp.getSharedData().getInt(AppData.getUserName(getContext())
				+ AppConstants.PREF_PIECES_SET, 0));

		if (mainApp.getSharedData().getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY)
				.equals(StaticData.SYMBOL_EMPTY)) {
			startActivity(new Intent(this, LoginScreenActivity.class));
			mainApp.guest = true;
		} else {
			if (mainApp.getSharedData().getBoolean(AppData.getUserName(getContext()) + AppConstants.PREF_NOTIFICATION, true)) {
				AppUtils.startNotificationsUpdate(this);
			}

			startActivity(new Intent(this, HomeScreenActivity.class));
			mainApp.guest = false;
		}
		finish();
	}

	@Override
	public void update(int code) {
	}

}
