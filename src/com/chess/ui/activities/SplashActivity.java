package com.chess.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import com.chess.R;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.utilities.AppUtils;

public class SplashActivity extends CommonLogicActivity {

	private View splashProgress;
	private static final long SPLASH_DELAY = 1500;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);

		splashProgress = findViewById(R.id.splashProgress);
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
					//AppData.setLiveConnected(getContext(), false);
				}
			}, SPLASH_DELAY);
		}

		/*else { // validate token
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.VALIDATE_TOKEN);
			loadItem.addRequestParams(RestHelper.P_AUTH_TOKEN, AppData.getUserToken(this));

			new GetStringObjTask(new TokenValidationListener()).execute(loadItem);
		}*/
	}

	private class TokenValidationListener extends AbstractUpdateListener<String>{

		public TokenValidationListener() {
			super(getContext());
		}

		@Override
		public void showProgress(boolean show) {
			splashProgress.setVisibility(show? View.VISIBLE: View.INVISIBLE);
		}

		@Override
		public void updateData(String returnedObj) {
			if (AppData.isNotificationsEnabled(getContext())) {
				checkMove();
			}

			startActivity(new Intent(SplashActivity.this, HomeScreenActivity.class));
			AppData.setGuest(getContext(), false);
			AppData.setLiveChessMode(getContext(), false);
			//AppData.setLiveConnected(getContext(), false);
		}

		@Override
		public void errorHandle(String resultMessage) {
			goToLoginScreen();
			showToast(resultMessage);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			goToLoginScreen();
		}
	}

	private void goToLoginScreen(){
		startActivity(new Intent(getContext(), LoginScreenActivity.class));
	}
}
