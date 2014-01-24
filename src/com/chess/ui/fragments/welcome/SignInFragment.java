package com.chess.ui.fragments.welcome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.chess.R;
import com.chess.statics.AppConstants;
import com.chess.ui.fragments.CommonLogicFragment;


/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 15:21
 */
public class SignInFragment extends CommonLogicFragment implements TextView.OnEditorActionListener, View.OnTouchListener {

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

		enableSlideMenus(false);

		loginUsernameEdt = (EditText) view.findViewById(R.id.usernameEdt);
		loginPasswordEdt = (EditText) view.findViewById(R.id.passwordEdt);
		loginPasswordEdt.setOnEditorActionListener(this);
		loginUsernameEdt.setOnTouchListener(this);
		loginPasswordEdt.setOnTouchListener(this);

		setLoginFields(loginUsernameEdt, loginPasswordEdt);

		view.findViewById(R.id.signinBtn).setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		String username = getAppData().getUsername();
		if (!username.equals(AppConstants.GUEST_NAME)) {
			loginUsernameEdt.setText(username);
			loginPasswordEdt.setText(getAppData().getPassword());
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		dismissProgressDialog();
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.signinBtn) {
			if (!isNetworkAvailable()) {
				popupItem.setPositiveBtnId(R.string.check_connection);
				showPopupDialog(R.string.no_network, NETWORK_CHECK_TAG);
			} else {
				signInUser();
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == NETWORK_REQUEST) {
				signInUser();
			}
		}
	}

	@Override
	protected void afterLogin() {
		super.afterLogin();
		backToHomeFragment();
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (view.getId() == R.id.usernameEdt) {
			loginUsernameEdt.setError(null);
			loginUsernameEdt.setSelection(loginUsernameEdt.getText().length());
		} else if (view.getId() == R.id.passwordEdt) {
			loginPasswordEdt.setError(null);
		}
		return false;
	}

	@Override
	public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
		if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.FLAG_EDITOR_ACTION
				|| keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
			if (!isNetworkAvailable()) {
				popupItem.setPositiveBtnId(R.string.check_connection);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
			} else {
				signInUser();
			}
		}
		return false;
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(NETWORK_CHECK_TAG)) {
			startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), NETWORK_REQUEST);
			return;
		}

		super.onPositiveBtnClick(fragment);
	}
}
