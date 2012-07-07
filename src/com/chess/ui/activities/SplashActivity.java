package com.chess.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import com.chess.R;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.utilities.AppUtils;

public class SplashActivity extends BaseFragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);

		if (AppData.getUserToken(this).equals(StaticData.SYMBOL_EMPTY)) {
			startActivity(new Intent(this, LoginScreenActivity.class));
			DataHolder.getInstance().setGuest(true);
		} else {
			if (preferences.getBoolean(AppData.getUserName(this) + AppConstants.PREF_NOTIFICATION, true)) {
				AppUtils.startNotificationsUpdate(this);
			}

			startActivity(new Intent(this, HomeScreenActivity.class));
			DataHolder.getInstance().setGuest(false);
		}
		finish();
	}
}
