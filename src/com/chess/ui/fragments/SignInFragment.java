package com.chess.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.chess.R;
import com.chess.ui.activities.SignUpScreenActivity;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 15:21
 */
public class SignInFragment extends BaseFragment implements View.OnClickListener {

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
//		loginPasswordEdt.setOnEditorActionListener(this); // TODO restore
//		loginPasswordEdt.setOnTouchListener(this);

//		setLoginFields(loginUsernameEdt, loginPasswordEdt);

		view.findViewById(R.id.signin).setOnClickListener(this);
		view.findViewById(R.id.signup).setOnClickListener(this);
		view.findViewById(R.id.guestplay).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.signin) {
//			if (!AppUtils.isNetworkAvailable(getActivity())){ // check only if live   // TODO restore
//				popupItem.setPositiveBtnId(R.string.wireless_settings);
//				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
//			} else{
//				signInUser();
//			}
		} else if (view.getId() == R.id.signup) {
			getActivityFace().openFragment(new SignUpFragment());

//			startActivity(new Intent(this, SignUpScreenActivity.class));
		} else if (view.getId() == R.id.guestplay) {
			getActivityFace().openFragment(new HomeTabsFragment());
//			DataHolder.reset();
//			TacticsDataHolder.reset();
//			AppData.setLiveChessMode(this, false);
//			AppData.setGuest(this, true);
//
//			Intent intent = new Intent(this, HomeScreenActivity.class);
//			startActivity(intent);
		}
	}
}
