package com.chess.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCode;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.TacticsDataHolder;
import com.chess.backend.entity.new_api.LoginItem;
import com.chess.backend.entity.new_api.RegisterItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.views.drawables.LogoBackgroundDrawable;
import com.flurry.android.FlurryAgent;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SplashActivity extends CommonLogicActivity {

	private static final long SPLASH_DELAY = 1000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);

		LogoBackgroundDrawable logoBackgroundDrawable = new LogoBackgroundDrawable(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			findViewById(R.id.mainView).setBackground(logoBackgroundDrawable);
		} else {
			findViewById(R.id.mainView).setBackgroundDrawable(logoBackgroundDrawable);
		}

		if (AppData.getUserToken(this).equals(StaticData.SYMBOL_EMPTY)) {
			goToLoginScreen();
		} /*else {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					startActivity(new Intent(SplashActivity.this, NewLoginActivity.class));
					AppData.setGuest(getContext(), false);
					AppData.setLiveChessMode(getContext(), false);
				}
			}, SPLASH_DELAY);
		}*/

		else { // validate credentials
			LoadItem loadItem = new LoadItem();

			loadItem.setLoadPath(RestHelper.CMD_LOGIN);
			loadItem.setRequestMethod(RestHelper.POST);
			loadItem.addRequestParams(RestHelper.P_USER_NAME_OR_MAIL, AppData.getUserName(this));
			loadItem.addRequestParams(RestHelper.P_PASSWORD, AppData.getPassword(this));

			new RequestJsonTask<LoginItem>(new LoginUpdateListenerNew()).executeTask(loadItem);
		}
	}

	private class LoginUpdateListenerNew extends AbstractUpdateListener<LoginItem> {
		public LoginUpdateListenerNew() {
			super(getContext(), LoginItem.class);
		}

//		@Override
//		public void showProgress(boolean show) {
//			if (show){
//				showPopupHardProgressDialog(R.string.signing_in_);
//			} else {
//				if(isPaused)
//					return;
//
//				dismissProgressDialog();
//			}
//		}

		@Override
		public void updateData(LoginItem returnedObj) {
			processLogin(returnedObj.getData());
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				// get server code
				int serverCode = RestHelper.decodeServerCode(resultCode);

				String serverMessage = ServerErrorCode.getUserFriendlyMessage(getContext(), serverCode); // TODO restore
				showToast(serverMessage);
			} else {
				goToLoginScreen();
			}
		}
	}

	protected void processLogin(RegisterItem.Data returnedObj) {
		try {
			preferencesEditor.putString(AppConstants.USER_TOKEN, URLEncoder.encode(returnedObj.getLogin_token(), HTTP.UTF_8));
		} catch (UnsupportedEncodingException ignored) {
			preferencesEditor.putString(AppConstants.USER_TOKEN, returnedObj.getLogin_token());
		}
		preferencesEditor.commit();

		AppData.setGuest(this, false);
		AppData.setLiveChessMode(this, false);
		DataHolder.reset();
		TacticsDataHolder.reset();

		registerGcmService();

		goToLoginScreen();
	}

	private void goToLoginScreen(){
		startActivity(new Intent(getContext(), NewLoginActivity.class));
	}
}
