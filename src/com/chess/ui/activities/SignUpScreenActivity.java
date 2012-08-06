package com.chess.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.ChessUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.backend.tasks.PostDataTask;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.utilities.AppUtils;
import com.facebook.android.Facebook;
import com.facebook.android.LoginButton;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.flurry.android.FlurryAgent;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * SignUpScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:34
 */
public class SignUpScreenActivity extends CoreActivityActionBar implements View.OnClickListener, AdapterView.OnItemSelectedListener {


	private EditText userNameEdt;
	private EditText emailEdt;
	private EditText passwordEdt;
	private EditText regRetypeEdt;
	private Button regSubmit;
	private int CID = -1;
	private Context context;
	private static String[] COUNTRIES;
	private static String[] COUNTRIES_ID;
	private String[] tmp2;

	private Facebook facebook;
	private LoginButton facebookLoginButton;
	private static int SIGNIN_CALLBACK_CODE = 16;
	private static int SIGNIN_FACEBOOK_CALLBACK_CODE = 128;
	private String userName;
	private String email;
	private String password;
	private RegisterUpdateListener registerUpdateListener;
	private SignUpUpdateListener signUpUpdateListener;
	private int loginReturnCode;
	private LoginUpdateListener loginUpdateListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_screen);

		context = this;

		COUNTRIES = getResources().getStringArray(R.array.countries);
		COUNTRIES_ID = getResources().getStringArray(R.array.countries_id);

		userNameEdt = (EditText) findViewById(R.id.RegUsername);
		emailEdt = (EditText) findViewById(R.id.RegEmail);
		passwordEdt = (EditText) findViewById(R.id.RegPassword);
		regRetypeEdt = (EditText) findViewById(R.id.RegRetype);
		regSubmit = (Button) findViewById(R.id.RegSubmitBtn);

		userNameEdt.addTextChangedListener(new FieldChangeWatcher(userNameEdt));
		emailEdt.addTextChangedListener(new FieldChangeWatcher(emailEdt));
		passwordEdt.addTextChangedListener(new FieldChangeWatcher(passwordEdt));
		regRetypeEdt.addTextChangedListener(new FieldChangeWatcher(regRetypeEdt));


		getCountryCode();
		Spinner countrySpinner = (Spinner) findViewById(R.id.country);
		countrySpinner.setAdapter(new ChessSpinnerAdapter(this, tmp2));
		countrySpinner.setOnItemSelectedListener(this);

		regSubmit.setOnClickListener(this);

		facebookLoginButton = (LoginButton) findViewById(R.id.fb_connect);

		facebook = new Facebook(AppConstants.FACEBOOK_APP_ID);
		SessionStore.restore(facebook, this);
		SessionEvents.addAuthListener(new SampleAuthListener());
		SessionEvents.addLogoutListener(new SampleLogoutListener());

		facebookLoginButton.init(this, facebook);

		signUpUpdateListener = new SignUpUpdateListener();
		registerUpdateListener = new RegisterUpdateListener();
		loginUpdateListener = new LoginUpdateListener();
	}

	private void getCountryCode() {
		String[] tmp = COUNTRIES.clone();
		java.util.Arrays.sort(tmp);
		int i = 0, k = 0;
		for (i = 0; i < tmp.length; i++) {
			if (tmp[i].equals(getString(R.string.united_states))) {
				k = i;
				break;
			}
		}
		tmp2 = new String[tmp.length];
		tmp2[0] = tmp[k];
		for (i = 0; i < tmp2.length; i++) {
			if (i < k) {
				tmp2[i + 1] = tmp[i];
			} else if (i > k) {
				tmp2[i] = tmp[i];
			}
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.RegSubmitBtn) {
			if (checkRegisterInfo())
				submitRegisterInfo();
		}
	}

	private boolean checkRegisterInfo() {
		userName = encodeField(userNameEdt);
		email = encodeField(emailEdt);
		password = encodeField(passwordEdt);

		if (userName.length() < 3) {
			userNameEdt.setError(getString(R.string.too_short));
			userNameEdt.requestFocus();
			return false;
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

		if (!password.equals(regRetypeEdt.getText().toString())) {
			regRetypeEdt.setError(getString(R.string.pass_dont_match));
			regRetypeEdt.requestFocus();
			return false;
		}

		if (CID == -1) {
			showToast(getString(R.string.wrongcountry));
			return false;
		}
		return true;
	}

	private void submitRegisterInfo() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.REGISTER);
		loadItem.addRequestParams(RestHelper.P_USER_NAME, userName);
		loadItem.addRequestParams(RestHelper.P_PASSWORD, password);
		loadItem.addRequestParams(RestHelper.P_EMAIL, email);
		loadItem.addRequestParams(RestHelper.P_COUNTRY_ID, String.valueOf(CID));
		loadItem.addRequestParams(RestHelper.P_APP_TYPE, RestHelper.V_ANDROID);

		new GetStringObjTask(registerUpdateListener).executeTask(loadItem);
	}

	private class RegisterUpdateListener extends ChessUpdateListener {
		public RegisterUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if(returnedObj.contains(RestHelper.R_SUCCESS)){
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.LOGIN);
				loadItem.addRequestParams(RestHelper.P_USER_NAME, userName);
				loadItem.addRequestParams(RestHelper.P_PASSWORD, password);

				new PostDataTask(signUpUpdateListener).executeTask(loadItem);
			}else if(returnedObj.contains(RestHelper.R_ERROR)){
				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
			}
		}
	}

	private class SignUpUpdateListener extends ChessUpdateListener {
		public SignUpUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if(returnedObj.contains(RestHelper.R_SUCCESS)){
				FlurryAgent.onEvent("New Account Created", null);  // TODO
				String[] result = returnedObj.split(":");


				preferencesEditor.putString(AppConstants.USERNAME, userNameEdt.getText().toString().toLowerCase());
				preferencesEditor.putString(AppConstants.PASSWORD, passwordEdt.getText().toString());
				preferencesEditor.putString(AppConstants.USER_PREMIUM_STATUS, result[0].split("[+]")[1]);
				preferencesEditor.putString(AppConstants.API_VERSION, result[1]);
				try {
					preferencesEditor.putString(AppConstants.USER_TOKEN, URLEncoder.encode(result[2], HTTP.UTF_8));
				} catch (UnsupportedEncodingException ignored) {
					showSinglePopupDialog(R.string.error, R.string.error_occurred_while_login);
					return;
				}
				preferencesEditor.commit();

				startActivity(new Intent(context, HomeScreenActivity.class));
				finish();

				showToast(R.string.congratulations);

				if (returnedObj.length() > 0) {
					String[] responseArray = returnedObj.split(":");
					if (responseArray.length >= 4) {
						if (loginReturnCode == SIGNIN_CALLBACK_CODE) {
							preferencesEditor.putString(AppConstants.USERNAME, userName.toLowerCase());
						} else if (loginReturnCode == SIGNIN_FACEBOOK_CALLBACK_CODE && responseArray.length >= 5) {
							FlurryAgent.onEvent(FlurryData.FB_LOGIN, null);

							preferencesEditor.putString(AppConstants.USERNAME, responseArray[4].trim().toLowerCase());
						}
						doUpdate(responseArray);
					}
				}
			}else if(returnedObj.contains(RestHelper.R_ERROR)){
				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
			}
		}
	}

	private void doUpdate(String[] response) {

		preferencesEditor.putString(AppConstants.PASSWORD, passwordEdt.getText().toString().trim());
		preferencesEditor.putString(AppConstants.USER_PREMIUM_STATUS, response[0].split("[+]")[1]);
		preferencesEditor.putString(AppConstants.API_VERSION, response[1]);
		try {
			preferencesEditor.putString(AppConstants.USER_TOKEN, URLEncoder.encode(response[2], HTTP.UTF_8));
		} catch (UnsupportedEncodingException ignored) {
		}
		preferencesEditor.commit();

		FlurryAgent.onEvent(FlurryData.LOGGED_IN);
		if (preferences.getBoolean(AppData.getUserName(this) + AppConstants.PREF_NOTIFICATION, true)) {
			AppUtils.startNotificationsUpdate(this);
		}
		DataHolder.getInstance().setGuest(false);


		backToHomeActivity();
	}


	public class SampleAuthListener implements SessionEvents.AuthListener {
		@Override
		public void onAuthSucceed() {

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.LOGIN);
			loadItem.addRequestParams(RestHelper.P_FACEBOOK_ACCESS_TOKEN, facebook.getAccessToken());
			loadItem.addRequestParams(RestHelper.P_RETURN, RestHelper.V_USERNAME);

			new GetStringObjTask(loginUpdateListener).executeTask(loadItem);

			loginReturnCode = SIGNIN_FACEBOOK_CALLBACK_CODE;
		}

		@Override
		public void onAuthFail(String error) {
			showToast(getString(R.string.login_failed) + StaticData.SYMBOL_SPACE + error);
		}
	}

	private class LoginUpdateListener extends ChessUpdateListener {
		public LoginUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				if (returnedObj.length() > 0) {
					final String[] responseArray = returnedObj.split(":");
					if (responseArray.length >= 4) {

						if (loginReturnCode == SIGNIN_CALLBACK_CODE) {
							preferencesEditor.putString(AppConstants.USERNAME, userName.toLowerCase());
							doUpdate(responseArray);
						} else if (loginReturnCode == SIGNIN_FACEBOOK_CALLBACK_CODE && responseArray.length >= 5) {
							FlurryAgent.onEvent(FlurryData.FB_LOGIN, null);
							preferencesEditor.putString(AppConstants.USERNAME, responseArray[4].trim().toLowerCase());
							doUpdate(responseArray);
						}
					}
				}
			} else if (returnedObj.contains(RestHelper.R_FB_USER_HAS_NO_ACCOUNT)) {
				showToast(R.string.no_chess_account_signup_please);
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.REGISTER_HTML)));
			}
		}
	}

	public class SampleLogoutListener implements SessionEvents.LogoutListener {
		@Override
		public void onLogoutBegin() {
			showToast(R.string.loggin_out);
		}

		@Override
		public void onLogoutFinish() {
			showToast(R.string.you_logged_out);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	private String encodeField(EditText editText) {
		String value = "";
		try {
			value = URLEncoder.encode(getTextFromField(editText), HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			editText.setError(getString(R.string.encoding_unsupported));
		}
		return value;
	}

	private class FieldChangeWatcher implements TextWatcher {
		private EditText editText;

		public FieldChangeWatcher(EditText editText) {
			this.editText = editText;
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.length() > 1) {
				editText.setError(null);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
		int i = 0;
		while (i < COUNTRIES.length) {   // TODO use predefined strings
			if (COUNTRIES[i].equals(tmp2[pos])) {
				break;
			}
			i++;
		}
		CID = Integer.parseInt(COUNTRIES_ID[i]);
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {

	}

}