package com.chess.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.ui.views.LogoBackgroundDrawable;
import com.chess.utilities.AppUtils;
import com.facebook.android.Facebook;
import com.flurry.android.FlurryAgent;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 15:21
 */
public class SignInFragment extends ProfileSetupsFragment implements TextView.OnEditorActionListener, View.OnTouchListener {

	private boolean forceFlag;
	private EditText loginUsernameEdt;
	private EditText loginPasswordEdt;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_sign_in_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		loginUsernameEdt = (EditText) view.findViewById(R.id.usernameEdt);
		loginPasswordEdt = (EditText) view.findViewById(R.id.passwordEdt);
		loginPasswordEdt.setOnEditorActionListener(this);
		loginPasswordEdt.setOnTouchListener(this);

		setLoginFields(loginUsernameEdt, loginPasswordEdt);

		view.findViewById(R.id.signin).setOnClickListener(this);
		view.findViewById(R.id.signup).setOnClickListener(this);
		view.findViewById(R.id.guestplay).setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		loginUsernameEdt.setText(AppData.getUserName(getActivity()));
		loginPasswordEdt.setText(AppData.getPassword(getActivity()));
	}



	@Override
	public void onPause() {
		super.onPause();
		dismissProgressDialog();
	}

//	@Override
//	protected void afterLogin() {
//		FlurryAgent.logEvent(FlurryData.LOGGED_IN); // duplicate logic -> moved to CommonLogicFragment class
////		if (AppData.isNotificationsEnabled(this)){
////			checkMove();
////		}
//
//		backToHomeFragment();
//	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.signin) {
			if (!AppUtils.isNetworkAvailable(getActivity())){ // check only if live   // TODO restore
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
			} else{
				signInUser();
			}
		} else if (view.getId() == R.id.signup) {
			getActivityFace().openFragment(new SignUpFragment());

		} else if (view.getId() == R.id.guestplay) {
			getActivityFace().openFragment(new CreateProfileFragment());
//			DataHolder.reset();
//			TacticsDataHolder.reset();
//			AppData.setLiveChessMode(this, false);
//			AppData.setGuest(this, true);
//
//			Intent intent = new Intent(this, HomeScreenActivity.class);
//			startActivity(intent);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK ){
			if(requestCode == Facebook.DEFAULT_AUTH_ACTIVITY_CODE){
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

	@Override
	public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
		if(actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.FLAG_EDITOR_ACTION
				|| keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER ){
			if(!AppUtils.isNetworkAvailable(getActivity())){ // check only if live
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
			}else{
				signInUser();
			}
		}
		return false;
	}
}
