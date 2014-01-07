package com.chess.ui.fragments.welcome;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.RegisterItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.statics.AppConstants;
import com.chess.statics.FlurryData;
import com.chess.statics.Symbol;
import com.chess.ui.fragments.CommonLogicFragment;
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

	private String username;
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
		view.findViewById(R.id.loginLinkTxt).setOnClickListener(this);

		userNameEdt.addTextChangedListener(new FieldChangeWatcher(userNameEdt));
		emailEdt.addTextChangedListener(new FieldChangeWatcher(emailEdt));
		passwordEdt.addTextChangedListener(new FieldChangeWatcher(passwordEdt));
		passwordRetypeEdt.addTextChangedListener(new FieldChangeWatcher(passwordRetypeEdt));

		setLoginFields(userNameEdt, passwordEdt);

		{ // Terms link handle
			TextView termsLinkTxt = (TextView) view.findViewById(R.id.termsLinkTxt);
			termsLinkTxt.setClickable(true);
			String termsText = getString(R.string.new_by_signing_up_accept_mobile) + Symbol.NEW_STR + Symbol.NEW_STR
					+ getString(R.string.new_by_signing_up_accept_mobile1) ;
			termsLinkTxt.setText(Html.fromHtml(termsText));
			Linkify.addLinks(termsLinkTxt, Linkify.WEB_URLS);
			termsLinkTxt.setMovementMethod(LinkMovementMethod.getInstance());
			termsLinkTxt.setLinkTextColor(Color.WHITE);
		}

		registerUpdateListener = new RegisterUpdateListener();
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.completeSignUpBtn) {
			if (!checkRegisterInfo()){
				return;
			}

			if (!isNetworkAvailable()){ // check only if live
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
				return;
			}

			submitRegisterInfo();
		} else if (view.getId() == R.id.loginLinkTxt) {
			getActivityFace().openFragment(new SignInFragment());
		}
	}

	private boolean checkRegisterInfo() {
		username = getTextFromField(userNameEdt);
		email = getTextFromField(emailEdt);
		password = getTextFromField(passwordEdt);

		if (username.length() < 3) {
			userNameEdt.setError(getString(R.string.too_short));
			userNameEdt.requestFocus();
			return false;
		}

		if (!emailPattern.matcher(getTextFromField(emailEdt)).matches()) {
			emailEdt.setError(getString(R.string.invalidEmail));
			emailEdt.requestFocus();
			return false;
		}

		if (email.equals(Symbol.EMPTY)) {
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
		LoadItem loadItem = LoadHelper.postUsers(username, password, email, getDeviceId());
		new RequestJsonTask<RegisterItem>(registerUpdateListener).executeTask(loadItem);
	}

	private class RegisterUpdateListener extends ChessUpdateListener<RegisterItem> {

		public RegisterUpdateListener() {
			super(RegisterItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			if (show) {
				showPopupHardProgressDialog();
			} else {
				if (isPaused)
					return;

				dismissProgressDialog();
			}
		}

		@Override
		public void updateData(RegisterItem returnedObj) {
			FlurryAgent.logEvent(FlurryData.NEW_ACCOUNT_CREATED);
			showToast(R.string.challenge_created);

			preferencesEditor.putString(AppConstants.USERNAME, getTextFromField(userNameEdt));
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
			if(requestCode == NETWORK_REQUEST){
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
