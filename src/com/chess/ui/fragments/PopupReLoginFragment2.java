package com.chess.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.backend.tasks.PostDataTask;
import com.chess.model.PopupItem;
import com.chess.utilities.AppUtils;
import com.facebook.android.Facebook;
import com.facebook.android.LoginButton;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.flurry.android.FlurryAgent;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * @author alien_roger
 * @created at: 07.04.12 7:13
 */
public class PopupReLoginFragment2 extends PopupCustomViewFragment {

	private static int SIGNIN_CALLBACK_CODE = 16;
	private static int SIGNIN_FACEBOOK_CALLBACK_CODE = 128;
	private static final int MIN_USERNAME_LENGTH = 3;
	private static final int MAX_USERNAME_LENGTH = 20;


	private EditText usernameEdt;
	private EditText passwordEdt;
	private PopupItem popupProgressItem;
	private PopupProgressFragment popupProgressDialogFragment;
	private LoginUpdateListener loginUpdateListener;
	private static final String PROGRESS_TAG = "progress popup";

	private int loginReturnCode;
	private SharedPreferences.Editor preferencesEditor;
	private SharedPreferences preferences;
	private Facebook facebook;

	public static PopupReLoginFragment2 newInstance(PopupItem popupItem) {
		PopupReLoginFragment2 frag = new PopupReLoginFragment2();
		Bundle arguments = new Bundle();
		arguments.putSerializable(POPUP_ITEM, popupItem);
		frag.setArguments(arguments);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		popupProgressItem = new PopupItem();
		popupProgressDialogFragment = PopupProgressFragment.newInstance(popupProgressItem);

		loginUpdateListener = new LoginUpdateListener();
		preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		preferencesEditor = preferences.edit();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);


		LoginButton facebookLoginButton = (LoginButton) view.findViewById(R.id.re_fb_connect);

		usernameEdt = (EditText) view.findViewById(R.id.usernameEdt);
		passwordEdt = (EditText) view.findViewById(R.id.passwordEdt);
		view.findViewById(R.id.re_signin).setOnClickListener(this);

		facebook = new Facebook(AppConstants.FACEBOOK_APP_ID);
		SessionStore.restore(facebook, getActivity());

		SessionEvents.dropAuthListeners();
		SessionEvents.addAuthListener(new SampleAuthListener());
		SessionEvents.dropLogoutListeners();
		SessionEvents.addLogoutListener(new SampleLogoutListener());
		facebookLoginButton.init(getActivity(), facebook);

		facebookLoginButton.logout();
	}

	@Override
	public void onResume() {
		super.onResume();
		usernameEdt.setText(AppData.getUserName(getActivity()));
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if(view.getId() == R.id.re_signin){
			String userName = getTextFromField(usernameEdt);
			if (userName.length() < MIN_USERNAME_LENGTH || userName.length() > MAX_USERNAME_LENGTH) {
				usernameEdt.setError(getString(R.string.validateUsername));
				usernameEdt.requestFocus();
				return;
			}

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.LOGIN);
			loadItem.addRequestParams(RestHelper.P_USER_NAME, userName);
			loadItem.addRequestParams(RestHelper.P_PASSWORD, getTextFromField(passwordEdt));

			new PostDataTask(loginUpdateListener).executeTask(loadItem);

			loginReturnCode = SIGNIN_CALLBACK_CODE;
		}
	}

	private class LoginUpdateListener extends AbstractUpdateListener<String> {
		public LoginUpdateListener() {
			super(getActivity());
		}

		@Override
		public void showProgress(boolean show) {
			if(show){
				popupProgressItem.setTitle(R.string.signingin);
				popupProgressItem.setMessage(StaticData.SYMBOL_EMPTY);
				popupProgressDialogFragment.setNotCancelable();
				popupProgressDialogFragment.updatePopupItem(popupProgressItem);
				popupProgressDialogFragment.show(getFragmentManager(), PROGRESS_TAG);

			} else {
				popupProgressDialogFragment.dismiss();
			}
		}

		@Override
		public void updateData(String returnedObj) {
			if(isPaused)
				return;

			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				if (returnedObj.length() > 0) {
					final String[] responseArray = returnedObj.split(":");
					if (responseArray.length >= 4) {
						if (loginReturnCode == SIGNIN_CALLBACK_CODE) {
							preferencesEditor.putString(AppConstants.USERNAME, usernameEdt.getText().toString().trim().toLowerCase());
							processLogin(responseArray);
						} else if (loginReturnCode == SIGNIN_FACEBOOK_CALLBACK_CODE && responseArray.length >= 5) {
							FlurryAgent.onEvent(FlurryData.FB_LOGIN, null);
							preferencesEditor.putString(AppConstants.USERNAME, responseArray[4].trim().toLowerCase());
							processLogin(responseArray);
						}
					}
				}
			} else if (returnedObj.contains(RestHelper.R_FB_USER_HAS_NO_ACCOUNT)) {
				showToast(R.string.no_chess_account_signup_please);

			} else if(returnedObj.contains(RestHelper.R_ERROR)){
				String message = returnedObj.substring(RestHelper.R_ERROR.length());
				if(message.equals("Invalid password.")){
					passwordEdt.setError(getResources().getString(R.string.invalid_password));
					passwordEdt.requestFocus();
				}else{
					showToast(message);
				}
			}
		}
	}

	private void processLogin(String[] response) {
		preferencesEditor.putString(AppConstants.PASSWORD, passwordEdt.getText().toString().trim());
		preferencesEditor.putString(AppConstants.USER_PREMIUM_STATUS, response[0].split("[+]")[1]);
		preferencesEditor.putString(AppConstants.API_VERSION, response[1]);
		try {
			preferencesEditor.putString(AppConstants.USER_TOKEN, URLEncoder.encode(response[2], AppConstants.UTF_8));
		} catch (UnsupportedEncodingException ignored) { // TODO handle more proper way
			preferencesEditor.putString(AppConstants.USER_TOKEN, response[2]);
		}
		preferencesEditor.putString(AppConstants.USER_SESSION_ID, response[3]);
		preferencesEditor.commit();

		FlurryAgent.onEvent(FlurryData.LOGGED_IN);
		if (preferences.getBoolean(AppData.getUserName(getActivity()) + AppConstants.PREF_NOTIFICATION, true)){
			AppUtils.startNotificationsUpdate(getActivity());
		}
	}

	private class SampleAuthListener implements SessionEvents.AuthListener {
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
		}
	}

	private class SampleLogoutListener implements SessionEvents.LogoutListener {
		@Override
		public void onLogoutBegin() {
		}

		@Override
		public void onLogoutFinish() {
		}
	}




}
