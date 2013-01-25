package com.chess.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCode;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.SoundPlayer;
import com.chess.backend.entity.TacticsDataHolder;
import com.chess.backend.entity.new_api.LoginItem;
import com.chess.backend.entity.new_api.RegisterItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.activities.CoreActivityActionBar;
import com.chess.ui.interfaces.ActiveFragmentInterface;
import com.facebook.android.Facebook;
import com.facebook.android.LoginButton;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.flurry.android.FlurryAgent;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.01.13
 * Time: 10:18
 */
public abstract class CommonLogicFragment extends BasePopupsFragment implements View.OnClickListener {

	private static final int SIGNIN_FACEBOOK_CALLBACK_CODE = 128;
	private static final int SIGNIN_CALLBACK_CODE = 16;
	protected static final long FACEBOOK_DELAY = 200;
	private static final int MIN_USERNAME_LENGTH = 3;
	private static final int MAX_USERNAME_LENGTH = 20;

	protected static final int REQUEST_REGISTER = 11;
	private static final int REQUEST_UNREGISTER = 22;

	//	private LoginUpdateListener loginUpdateListener;
	private LoginUpdateListenerNew loginUpdateListener;

	private int loginReturnCode;
	private ActiveFragmentInterface activityFace;
	protected static Facebook facebook;
	protected static Handler handler;
	private EditText loginUsernameEdt;
	private EditText passwordEdt;

	protected SharedPreferences preferences;
	protected SharedPreferences.Editor preferencesEditor;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activityFace = (ActiveFragmentInterface) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		preferences = AppData.getPreferences(getActivity()); // TODO rework shared pref usage to unique get method
		preferencesEditor = preferences.edit();

		handler = new Handler();
//		setHasOptionsMenu(true);
	}

	protected void setTitle(int titleId) {
		getActivity().setTitle(titleId);
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		facebookInit((LoginButton) getView().findViewById(R.id.fb_connect));
	}

	protected void showActionBar(boolean show) {
		getActivityFace().getActionBarActivity().provideActionBarHelper().showActionBar(show);
	}

	protected void facebookInit(LoginButton fbLoginBtn) {
		if (fbLoginBtn != null) {
			facebook = new Facebook(AppConstants.FACEBOOK_APP_ID);
			SessionStore.restore(facebook, getActivity());

			SessionEvents.dropAuthListeners();
			SessionEvents.addAuthListener(new SampleAuthListener());
			SessionEvents.dropLogoutListeners();
			SessionEvents.addLogoutListener(new SampleLogoutListener());
			fbLoginBtn.init(getActivity(), facebook);

			handler = new Handler();

//			loginUpdateListener = new LoginUpdateListener();
			loginUpdateListener = new LoginUpdateListenerNew();
		}
	}

	protected void setLoginFields(EditText passedUsernameEdt, EditText passedPasswordEdt) {
		this.loginUsernameEdt = passedUsernameEdt;
		this.passwordEdt = passedPasswordEdt;
	}

	protected CoreActivityActionBar getInstance() {
		return activityFace.getActionBarActivity();
	}

	protected ActiveFragmentInterface getActivityFace (){
		return activityFace;
	}

	protected void registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
		getActivity().registerReceiver(broadcastReceiver, intentFilter);
	}

	protected void unRegisterMyReceiver(BroadcastReceiver broadcastReceiver) {
		if (broadcastReceiver != null) {
			getActivity().unregisterReceiver(broadcastReceiver);
		}
	}

	protected void backToHomeFragment() {
		getActivityFace().switchFragment(new HomeTabsFragment());
	}

	protected void backToLoginFragment() {
		getActivityFace().switchFragment(new SignInFragment());
	}

	public SoundPlayer getSoundPlayer() {
		return SoundPlayer.getInstance(getActivity());
	}

	protected void signInUser(){
		String userName = getTextFromField(loginUsernameEdt);
		if (userName.length() < MIN_USERNAME_LENGTH || userName.length() > MAX_USERNAME_LENGTH) {
			loginUsernameEdt.setError(getString(R.string.validateUsername));
			loginUsernameEdt.requestFocus();
			return;
		}

		String pass = getTextFromField(passwordEdt);
		if (pass.length() == 0) {
			passwordEdt.setError(getString(R.string.invalid_password));
			passwordEdt.requestFocus();
			return;
		}

		LoadItem loadItem = new LoadItem();
//		loadItem.setLoadPath(RestHelper.LOGIN);
		loadItem.setLoadPath(RestHelper.CMD_LOGIN);
		loadItem.setRequestMethod(RestHelper.POST);
//		loadItem.addRequestParams(RestHelper.P_USER_NAME, userName);
		loadItem.addRequestParams(RestHelper.P_USER_NAME_OR_MAIL, userName);
		loadItem.addRequestParams(RestHelper.P_PASSWORD, getTextFromField(passwordEdt));
		loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.P_USER_NAME);

//		new PostDataTask(loginUpdateListener).executeTask(loadItem);
		new RequestJsonTask<LoginItem>(loginUpdateListener).executeTask(loadItem);

		loginReturnCode = SIGNIN_CALLBACK_CODE;
	}

	@Override
	public void onClick(View v) {

	}

	private class LoginUpdateListenerNew extends AbstractUpdateListener<LoginItem> {
		public LoginUpdateListenerNew() {
			super(getContext(), LoginItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			if (show){
				showPopupHardProgressDialog(R.string.signing_in_);
			} else {
				if(isPaused)
					return;

				dismissProgressDialog();
			}
		}

		@Override
		public void updateData(LoginItem returnedObj) {
			if (loginReturnCode == SIGNIN_FACEBOOK_CALLBACK_CODE) {
				FlurryAgent.logEvent(FlurryData.FB_LOGIN);
			}
			preferencesEditor.putString(AppConstants.USERNAME, returnedObj.getData().getUsername().trim().toLowerCase());
			preferencesEditor.putInt(AppConstants.USER_PREMIUM_STATUS, returnedObj.getData().getPremium_status());
			processLogin(returnedObj.getData());
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				// get server code
				int serverCode = RestHelper.decodeServerCode(resultCode);
				switch (serverCode){
					case ServerErrorCode.INVALID_USERNAME_PASSWORD:
						passwordEdt.setError(getResources().getString(R.string.invalid_password));
						passwordEdt.requestFocus();
						break;
					case ServerErrorCode.FACEBOOK_USER_NO_ACCOUNT:
						popupItem.setPositiveBtnId(R.string.sing_up);
						showPopupDialog(R.string.no_chess_account_signup_please, CHESS_NO_ACCOUNT_TAG);
						break;
					default:
//						String serverMessage = ServerErrorCode.getUserFriendlyMessage(); // TODO restore
//						showToast(serverMessage);

						break;
				}
			}
		}

		@Override
		public void errorHandle(String resultMessage) {
			if (resultMessage.contains(RestHelper.R_FB_USER_HAS_NO_ACCOUNT)) {
				popupItem.setPositiveBtnId(R.string.sing_up);
				showPopupDialog(R.string.no_chess_account_signup_please, CHESS_NO_ACCOUNT_TAG);
			} else {
			}
		}
	}

	protected void processLogin(RegisterItem.Data returnedObj) {
		if (passwordEdt == null) { // if accidently return in wrong callback, when widgets are not initialized
			return;
		}

		preferencesEditor.putString(AppConstants.PASSWORD, passwordEdt.getText().toString().trim());

		try {
			preferencesEditor.putString(AppConstants.USER_TOKEN, URLEncoder.encode(returnedObj.getLogin_token(), HTTP.UTF_8));
		} catch (UnsupportedEncodingException ignored) {
			preferencesEditor.putString(AppConstants.USER_TOKEN, returnedObj.getLogin_token());
//			showSinglePopupDialog(R.string.error, R.string.error_occurred_while_login); // or use that logic?
//			return;
		}
// 		preferencesEditor.putString(AppConstants.USER_SESSION_ID, response[3]); // TODO used only for live, so should be separate connection to live
		preferencesEditor.commit();

		if (getActivity() == null) {
			return;
		}

		AppData.setGuest(getActivity(), false);
		AppData.setLiveChessMode(getActivity(), false);
		DataHolder.reset();
		TacticsDataHolder.reset();

		getActivityFace().registerGcm();

		afterLogin();
	}

	protected void afterLogin(){
		FlurryAgent.logEvent(FlurryData.LOGGED_IN);
//		if (AppData.isNotificationsEnabled(getActivity())){
//			checkMove();
//		}

		backToHomeFragment();}

	public class DelayedCallback implements Runnable {

		private Intent data;
		private int resultCode;
		private int requestCode;

		public DelayedCallback(Intent data, int requestCode, int resultCode) {
			this.data = data;
			this.requestCode = requestCode;
			this.resultCode = resultCode;
		}

		@Override
		public void run() {
			handler.removeCallbacks(this);
			facebook.authorizeCallback(requestCode, resultCode, data);
		}
	}

	public class SampleAuthListener implements SessionEvents.AuthListener {
		@Override
		public void onAuthSucceed() {
			LoadItem loadItem = new LoadItem();
//			loadItem.setLoadPath(RestHelper.LOGIN);
			loadItem.setLoadPath(RestHelper.CMD_LOGIN);
			loadItem.setRequestMethod(RestHelper.POST);
			loadItem.addRequestParams(RestHelper.P_FACEBOOK_ACCESS_TOKEN, facebook.getAccessToken());
//			loadItem.addRequestParams(RestHelper.P_RETURN, RestHelper.V_USERNAME);
			loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.V_USERNAME);

//			new GetStringObjTask(loginUpdateListener).executeTask(loadItem);
			new RequestJsonTask<LoginItem>(loginUpdateListener).executeTask(loadItem);
			loginReturnCode = SIGNIN_FACEBOOK_CALLBACK_CODE;
		}

		@Override
		public void onAuthFail(String error) {
			showToast(getString(R.string.login_failed)+ StaticData.SYMBOL_SPACE + error);
		}
	}
	public class SampleLogoutListener implements SessionEvents.LogoutListener {
		@Override
		public void onLogoutBegin() {
			showToast(R.string.login_out);
		}

		@Override
		public void onLogoutFinish() {
			showToast(R.string.you_logged_out);
		}
	}

	protected List<String> getItemsFromArray(String[] array){
		List<String> items = new ArrayList<String>();
		items.addAll(Arrays.asList(array));
		return items;
	}

//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		inflater.inflate(R.menu.sign_out, menu);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//			case android.R.id.home:
//				getActivityFace().toggleMenu(SlidingMenu.LEFT);
//				break;
//		}
//		return true;
//	}
}
