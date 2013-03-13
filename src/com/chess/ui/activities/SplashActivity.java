package com.chess.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import com.chess.R;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.utilities.AppUtils;

public class SplashActivity extends CommonLogicActivity {

	private static final long SPLASH_DELAY = 1500;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			findViewById(R.id.mainView).setBackground(backgroundChessDrawable);
		} else {
			findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);
		}

		AppUtils.stopNotificationsUpdate(this);

		if (AppData.getUserToken(this).equals(StaticData.SYMBOL_EMPTY)) {
			goToLoginScreen();
		} else {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					startActivity(new Intent(SplashActivity.this, HomeScreenActivity.class));
					AppData.setGuest(getContext(), false);
					AppData.setLiveChessMode(getContext(), false);
				}
			}, SPLASH_DELAY);
		}
	}

	private void goToLoginScreen(){
		startActivity(new Intent(getContext(), LoginScreenActivity.class));
	}
}
