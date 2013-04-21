package com.chess.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import com.chess.R;
import com.chess.backend.statics.AppData;
import com.chess.ui.views.drawables.LogoBackgroundDrawable;

public class SplashActivity extends CommonLogicActivity {

	private static final long SPLASH_DELAY = 100;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_screen);

		LogoBackgroundDrawable logoBackgroundDrawable = new LogoBackgroundDrawable(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			findViewById(R.id.mainView).setBackground(logoBackgroundDrawable);
		} else {
			findViewById(R.id.mainView).setBackgroundDrawable(logoBackgroundDrawable);
		}

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				startActivity(new Intent(SplashActivity.this, MainFragmentFaceActivity.class));
				AppData.setLiveChessMode(getContext(), false);
			}
		}, SPLASH_DELAY);
	}

}
