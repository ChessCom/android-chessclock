package com.chess.ui.fragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.RegisterItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;

import static com.chess.statics.AppConstants.PASSWORD;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.10.13
 * Time: 11:35
 */
public class SettingsPasswordFragment extends CommonLogicFragment {

	private static final int MIN_PASSWORD_LENGTH = 6;
	private EditText oldPasswordEdt;
	private EditText newPasswordEdt;
	private EditText retypePasswordEdt;
	private String oldPassword;
	private String newPassword;
	private PasswordUpdateListener passwordUpdateListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		passwordUpdateListener = new PasswordUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_password_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.password);

		oldPasswordEdt = (EditText) view.findViewById(R.id.oldPasswordEdt);
		newPasswordEdt = (EditText) view.findViewById(R.id.newPasswordEdt);
		retypePasswordEdt = (EditText) view.findViewById(R.id.retypePasswordEdt);
		view.findViewById(R.id.changePasswordBtn).setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.changePasswordBtn) {
			if (!checkPasswords()){
				return;
			}

			if (!AppUtils.isNetworkAvailable(getActivity())){ // check only if live
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
				return;
			}

			updateData();
		}
	}

	private boolean checkPasswords() {
		oldPassword = getTextFromField(oldPasswordEdt);
		newPassword = getTextFromField(newPasswordEdt);
		String retypePassword = getTextFromField(retypePasswordEdt);

		if (!oldPassword.equals(getAppData().getPassword())) {
			oldPasswordEdt.setError(getString(R.string.pass_dont_match));
			oldPasswordEdt.requestFocus();
			return false;
		}

		if (newPassword.length() < MIN_PASSWORD_LENGTH) {
			newPasswordEdt.setError(getString(R.string.too_short));
			newPasswordEdt.requestFocus();
			return false;
		}

		if (!newPassword.equals(retypePassword)) {
			retypePasswordEdt.setError(getString(R.string.pass_dont_match));
			retypePasswordEdt.requestFocus();
			return false;
		}

		return true;
	}

	private void updateData() {
		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(RestHelper.PUT);
		loadItem.setLoadPath(RestHelper.getInstance().CMD_PASSWORD);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_OLD_PASS, oldPassword);
		loadItem.addRequestParams(RestHelper.P_NEW_PASS, newPassword);

		new RequestJsonTask<RegisterItem>(passwordUpdateListener).executeTask(loadItem);
	}

	private class PasswordUpdateListener extends ChessUpdateListener<RegisterItem> {

		public PasswordUpdateListener() {
			super(RegisterItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			if (show) {
				showPopupHardProgressDialog(R.string.processing_);
			} else {
				if (isPaused) {
					return;
				}

				dismissProgressDialog();
			}
		}

		@Override
		public void updateData(RegisterItem returnedObj) {
			showToast(R.string.password_changed);
			preferencesEditor.putString(PASSWORD, newPassword).commit();
		}
	}
}
