package com.chess.ui.fragments.welcome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.RegisterItem;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;
import com.flurry.android.FlurryAgent;

import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 20:07
 */
public class SignUpFragment extends CommonLogicFragment implements View.OnClickListener{

	protected Pattern emailPattern = Pattern.compile("[a-zA-Z0-9\\._%\\+\\-]+@[a-zA-Z0-9\\.\\-]+\\.[a-zA-Z]{2,4}");
	protected Pattern gMailPattern = Pattern.compile("[a-zA-Z0-9\\._%\\+\\-]+@[g]");   // TODO use for autoComplete

	private EditText userNameEdt;
	private EditText emailEdt;
	private EditText passwordEdt;
	private EditText passwordRetypeEdt;

	private String userName;
	private String email;
	private String password;
	private RegisterUpdateListener registerUpdateListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_sign_up_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		enableSlideMenus(false);

		userNameEdt = (EditText) view.findViewById(R.id.usernameEdt);
		emailEdt = (EditText) view.findViewById(R.id.emailEdt);
		passwordEdt = (EditText) view.findViewById(R.id.passwordEdt);
		passwordRetypeEdt = (EditText) view.findViewById(R.id.passwordRetypeEdt);
		view.findViewById(R.id.completeSignUpBtn).setOnClickListener(this);

		userNameEdt.addTextChangedListener(new FieldChangeWatcher(userNameEdt));
		emailEdt.addTextChangedListener(new FieldChangeWatcher(emailEdt));
		passwordEdt.addTextChangedListener(new FieldChangeWatcher(passwordEdt));
		passwordRetypeEdt.addTextChangedListener(new FieldChangeWatcher(passwordRetypeEdt));

		setLoginFields(userNameEdt, passwordEdt);

		registerUpdateListener = new RegisterUpdateListener();
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.completeSignUpBtn) {
			if (!checkRegisterInfo()){
				return;
			}

			if (!AppUtils.isNetworkAvailable(getActivity())){ // check only if live
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, BasePopupsFragment.NETWORK_CHECK_TAG);
				return;
			}

			submitRegisterInfo();
		}
	}

	private boolean checkRegisterInfo() {
		userName = getTextFromField(userNameEdt);
		email = getTextFromField(emailEdt);
		password = getTextFromField(passwordEdt);

		if (userName.length() < 3) {
			userNameEdt.setError(getString(R.string.too_short));
			userNameEdt.requestFocus();
			return false;
		}

		if (!emailPattern.matcher(getTextFromField(emailEdt)).matches()) {
			emailEdt.setError(getString(R.string.invalidEmail));
			emailEdt.requestFocus();
			return true;
		}

		if (email.equals(StaticData.SYMBOL_EMPTY)) {
			emailEdt.setError(getString(R.string.can_not_be_empty));
			emailEdt.requestFocus();
			return false;
		}

		if (password.length() < 6) {
			passwordEdt.setError(getString(R.string.too_short));
			passwordEdt.requestFocus();
			return false;
		}

		if (!password.equals(passwordRetypeEdt.getText().toString())) {
			passwordRetypeEdt.setError(getString(R.string.pass_dont_match));
			passwordRetypeEdt.requestFocus();
			return false;
		}

		return true;
	}

	private void submitRegisterInfo() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_USERS);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_USERNAME, userName);
		loadItem.addRequestParams(RestHelper.P_PASSWORD, password);
		loadItem.addRequestParams(RestHelper.P_EMAIL, email);
		loadItem.addRequestParams(RestHelper.P_APP_TYPE, RestHelper.V_ANDROID);
		loadItem.addRequestParams(RestHelper.P_DEVICE_ID, getDeviceId());

		new RequestJsonTask<RegisterItem>(registerUpdateListener).executeTask(loadItem);
	}

	private class RegisterUpdateListener extends ChessUpdateListener<RegisterItem> {

		public RegisterUpdateListener() {
			super(RegisterItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			if (show) {
				showPopupHardProgressDialog(R.string.processing_);
			} else {
				if (isPaused)
					return;

				dismissProgressDialog();
			}
		}

		@Override
		public void updateData(RegisterItem returnedObj) {
			FlurryAgent.logEvent(FlurryData.NEW_ACCOUNT_CREATED);
			showToast(R.string.congratulations);

			preferencesEditor.putString(AppConstants.USERNAME, userNameEdt.getText().toString().toLowerCase());
			preferencesEditor.putInt(AppConstants.USER_PREMIUM_STATUS, RestHelper.V_BASIC_MEMBER);
			processLogin(returnedObj.getData());
		}
	}


	@Override
	protected void afterLogin() {
		FlurryAgent.logEvent(FlurryData.LOGGED_IN);     // duplicate logic -> moved to CommonLogicFragment class
		getActivityFace().openFragment(new CreateProfileFragment());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {  // TODO restore
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK ){
			if(requestCode == BasePopupsFragment.NETWORK_REQUEST){
				submitRegisterInfo();
			}
		}
	}

	private class FieldChangeWatcher implements TextWatcher {
		private EditText editText;

		public FieldChangeWatcher(EditText editText) {
			this.editText = editText;
		}

		@Override
		public void onTextChanged(CharSequence str, int start, int before, int count) {
			if (str.length() > 1) {
				editText.setError(null);

//				if (gMailPattern.matcher(getTextFromField(emailEdt)).matches()){ // TODO use with autoComplete
//					emailEdt.setText(str + "mail.com");
//					emailEdt.requestFocus();
//				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	}

}
