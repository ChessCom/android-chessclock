package com.chess.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.RegisterItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.views.NewBackgroundChessDrawable;
import com.chess.utilities.AppUtils;
import com.facebook.android.Facebook;
import com.flurry.android.FlurryAgent;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 20:07
 */
public class SignUpFragment extends CommonLogicFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener{

	protected Pattern emailPattern = Pattern.compile("[a-zA-Z0-9\\._%\\+\\-]+@[a-zA-Z0-9\\.\\-]+\\.[a-zA-Z]{2,4}");
	protected Pattern gMailPattern = Pattern.compile("[a-zA-Z0-9\\._%\\+\\-]+@[g]");   // TODO use for autoComplete

	private EditText userNameEdt;
	private EditText emailEdt;
	private EditText passwordEdt;
	private EditText passwordRetypeEdt;
	private Button regSubmit;
	private int CID = -1;
	private static String[] COUNTRIES;
	private static String[] COUNTRIES_ID;
	private String[] tmp2;

	private String userName;
	private String email;
	private String password;
	private RegisterUpdateListener registerUpdateListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		showActionBar(false);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_sign_up_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (HONEYCOMB_PLUS_API) {
			view.findViewById(R.id.mainFrame).setBackground(new NewBackgroundChessDrawable(getActivity()));
		} else {
			view.findViewById(R.id.mainFrame).setBackgroundDrawable(new NewBackgroundChessDrawable(getActivity()));
		}

		COUNTRIES = getResources().getStringArray(R.array.countries);
		COUNTRIES_ID = getResources().getStringArray(R.array.countries_id);

		userNameEdt = (EditText) view.findViewById(R.id.usernameEdt);
		emailEdt = (EditText) view.findViewById(R.id.emailEdt);
		passwordEdt = (EditText) view.findViewById(R.id.passwordEdt);
		passwordRetypeEdt = (EditText) view.findViewById(R.id.passwordRetypeEdt);
		regSubmit = (Button) view.findViewById(R.id.RegSubmitBtn);

		userNameEdt.addTextChangedListener(new FieldChangeWatcher(userNameEdt));
		emailEdt.addTextChangedListener(new FieldChangeWatcher(emailEdt));
		passwordEdt.addTextChangedListener(new FieldChangeWatcher(passwordEdt));
		passwordRetypeEdt.addTextChangedListener(new FieldChangeWatcher(passwordRetypeEdt));

		setLoginFields(userNameEdt, passwordEdt);

		getCountryCode();
//		Spinner countrySpinner = (Spinner) view.findViewById(R.id.country);  // TODO create in CreateProfileFragment
//		countrySpinner.setAdapter(new ChessSpinnerAdapter(getActivity(), getItemsFromArray(tmp2)));
//		countrySpinner.setOnItemSelectedListener(this);

		regSubmit.setOnClickListener(this);

		registerUpdateListener = new RegisterUpdateListener();
	}

	private void getCountryCode() {
		String[] tmp = COUNTRIES.clone();
		java.util.Arrays.sort(tmp);
		int i, k = 0;
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
			if (!checkRegisterInfo()){
				return;
			}

			if (!AppUtils.isNetworkAvailable(getActivity())){ // check only if live
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
				return;
			}

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

		if (CID == -1) {
			showToast(getString(R.string.wrongcountry));
			return false;
		}
		return true;
	}

	private void submitRegisterInfo() {
		LoadItem loadItem = new LoadItem();
//		loadItem.setLoadPath(RestHelper.REGISTER);
		loadItem.setLoadPath(RestHelper.CMD_REGISTER);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_USER_NAME, userName);
		loadItem.addRequestParams(RestHelper.P_PASSWORD, password);
		loadItem.addRequestParams(RestHelper.P_EMAIL, email);
		loadItem.addRequestParams(RestHelper.P_COUNTRY_ID, CID);
		loadItem.addRequestParams(RestHelper.P_COUNTRY_CODE, 0); // TODO
		loadItem.addRequestParams(RestHelper.P_APP_TYPE, RestHelper.V_ANDROID);

//		new GetStringObjTask(registerUpdateListener).executeTask(loadItem);
		new RequestJsonTask<RegisterItem>(registerUpdateListener).executeTask(loadItem);
	}

	private class RegisterUpdateListener extends ActionBarUpdateListener<RegisterItem> {

		public RegisterUpdateListener() {
			super(getInstance(), RegisterItem.class);
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
		FlurryAgent.logEvent(FlurryData.LOGGED_IN);
//		if (AppData.isNotificationsEnabled(getActivity())){
//			checkMove();
//		}

		backToHomeFragment();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {  // TODO restore
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK ){
			if(requestCode == Facebook.DEFAULT_AUTH_ACTIVITY_CODE){
				facebook.authorizeCallback(requestCode, resultCode, data);
			}else if(requestCode == NETWORK_REQUEST){
				submitRegisterInfo();
			}
		}
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
