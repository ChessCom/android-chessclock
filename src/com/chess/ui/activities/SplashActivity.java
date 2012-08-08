package com.chess.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.utilities.AppUtils;

public class SplashActivity extends BaseFragmentActivity {

	private View splashProgress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);

		splashProgress = findViewById(R.id.splashProgress);
		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);

		if (AppData.getUserToken(this).equals(StaticData.SYMBOL_EMPTY)) {
			startActivity(new Intent(this, LoginScreenActivity.class));
			DataHolder.getInstance().setGuest(true);
		} else { // validate token
//			if(RestHelper.IS_TEST_SERVER_MODE){
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.VALIDATE_TOKEN);
				loadItem.addRequestParams(RestHelper.P_AUTH_TOKEN, AppData.getUserToken(this));

				new GetStringObjTask(new TokenValidationListener()).execute(loadItem);

//			}else{
//				if (preferences.getBoolean(AppData.getUserName(getContext()) + AppConstants.PREF_NOTIFICATION, true)) {
//					AppUtils.startNotificationsUpdate(getContext());
//				}
//
//				startActivity(new Intent(SplashActivity.this, HomeScreenActivity.class));
//				DataHolder.getInstance().setGuest(false);
//			}
		}

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
			if(returnedObj.contains(RestHelper.R_SUCCESS)){
				if (preferences.getBoolean(AppData.getUserName(getContext()) + AppConstants.PREF_NOTIFICATION, true)) {
					AppUtils.startNotificationsUpdate(getContext());
				}

				startActivity(new Intent(SplashActivity.this, HomeScreenActivity.class));
				DataHolder.getInstance().setGuest(false);

			}else{
				startActivity(new Intent(getContext(), LoginScreenActivity.class));
				DataHolder.getInstance().setGuest(true);
				showToast(returnedObj.substring(RestHelper.R_ERROR.length()));
			}
			finish();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			startActivity(new Intent(getContext(), LoginScreenActivity.class));
			DataHolder.getInstance().setGuest(true);
			finish();
		}
	}

}
