package com.chess.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.TacticsDataHolder;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.tasks.CheckUpdateTask;
import com.chess.utilities.AppUtils;
import com.facebook.android.Facebook;
import com.flurry.android.FlurryAgent;

/**
 * LoginScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 6:23
 */
public class LoginScreenActivity extends CommonLogicActivity implements View.OnClickListener, TextView.OnEditorActionListener, View.OnTouchListener {

	private boolean forceFlag;
	private EditText loginUsernameEdt;
	private EditText loginPasswordEdt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			findViewById(R.id.mainView).setBackground(backgroundChessDrawable);
		} else {
			findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);
		}

		loginUsernameEdt = (EditText) findViewById(R.id.usernameEdt);
		loginPasswordEdt = (EditText) findViewById(R.id.passwordEdt);
		loginPasswordEdt.setOnEditorActionListener(this);
		loginPasswordEdt.setOnTouchListener(this);

		setLoginFields(loginUsernameEdt, loginPasswordEdt);

		findViewById(R.id.signin).setOnClickListener(this);
		findViewById(R.id.signup).setOnClickListener(this);
		findViewById(R.id.guestplay).setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.signin) {
			if(!AppUtils.isNetworkAvailable(this)){ // check only if live
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
			}else{
				signInUser();
			}
		} else if (view.getId() == R.id.signup) {
			startActivity(new Intent(this, SignUpScreenActivity.class));
		} else if (view.getId() == R.id.guestplay) {
			DataHolder.reset();
			TacticsDataHolder.reset();
			AppData.setLiveChessMode(this, false);
			AppData.setGuest(this, true);

			Intent intent = new Intent(this, HomeScreenActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
		if(actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.FLAG_EDITOR_ACTION
				|| keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER ){
			if(!AppUtils.isNetworkAvailable(this)){ // check only if live
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
			}else{
				signInUser();
			}
		}
		return false;
	}



	@Override
	protected void onResume() {
		super.onResume();

		AppData.setLiveChessMode(this, true);

		loginUsernameEdt.setText(AppData.getUserName(this));
		loginPasswordEdt.setText(AppData.getPassword(this));

		long startDay = preferences.getLong(AppConstants.START_DAY, 0);
		if (startDay == 0 || !DateUtils.isToday(startDay)) {
			checkUpdate();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		dismissProgressDialog();
	}

	@Override
	protected void afterLogin() {
		FlurryAgent.logEvent(FlurryData.LOGGED_IN);
		if (AppData.isNotificationsEnabled(this)){
			checkMove();
		}

		backToHomeActivity();
	}

	private void checkUpdate() {
		new CheckUpdateTask(new CheckUpdateListener()).executeTask(RestHelper.GET_ANDROID_VERSION);
	}

	private class CheckUpdateListener extends AbstractUpdateListener<Boolean> {
		public CheckUpdateListener() {
			super(getContext());
		}

		@Override
		public void updateData(Boolean returnedObj) {
			forceFlag = returnedObj;
			if (isPaused)
				return;

			showPopupDialog(R.string.update_check, R.string.update_available_please_update, CHECK_UPDATE_TAG);
			getLastPopupFragment().setButtons(1);
		}
	}



	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);

		if(fragment.getTag().equals(CHECK_UPDATE_TAG)){
			if (forceFlag) {
				// drop start day
				preferencesEditor.putLong(AppConstants.START_DAY, 0);
				preferencesEditor.commit();

			}
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.GOOGLE_PLAY_URI)));
		}else if (fragment.getTag().equals(CHESS_NO_ACCOUNT_TAG)){
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.REGISTER_HTML)));
		}else if (fragment.getTag().equals(NETWORK_CHECK_TAG)){
			startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), NETWORK_REQUEST);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK ){
			if(requestCode == Facebook.DEFAULT_AUTH_ACTIVITY_CODE){
				Log.d("TEST", "LoginScreen onActivityResult -> facebook authorize");
//				facebook.authorizeCallback(requestCode, resultCode, data);
				handler.postDelayed(new DelayedCallback(data, requestCode, resultCode), FACEBOOK_DELAY);
			}else if(requestCode == NETWORK_REQUEST){
				signInUser();
			}
		}
	}



	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if(view.getId() == R.id.usernameEdt){
			loginUsernameEdt.setSelection(loginUsernameEdt.getText().length());
		} else if(view.getId() == R.id.passwordEdt){
			loginPasswordEdt.setError(null);
		}
		return false;
	}

}