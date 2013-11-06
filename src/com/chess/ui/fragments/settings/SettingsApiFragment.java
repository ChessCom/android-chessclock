package com.chess.ui.fragments.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.chess.R;
import com.chess.widgets.SwitchButton;
import com.chess.backend.RestHelper;
import com.chess.ui.fragments.LiveBaseFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.09.13
 * Time: 18:06
 */
public class SettingsApiFragment extends LiveBaseFragment implements SwitchButton.SwitchChangeListener {

	private SwitchButton apiSwitch;
	private EditText testUsernameEdt;
	private EditText testPasswordEdt;
	private EditText prodUsernameEdt;
	private EditText prodPasswordEdt;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_api_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.apiSwitchView).setOnClickListener(this);
		view.findViewById(R.id.reloginBtn).setOnClickListener(this);
		apiSwitch = (SwitchButton) view.findViewById(R.id.apiSwitch);
		apiSwitch.setChecked(RestHelper.getInstance().HOST.equals(RestHelper.HOST_PRODUCTION));
		apiSwitch.setSwitchChangeListener(this);

		testUsernameEdt = (EditText) view.findViewById(R.id.testUsernameEdt);
		testPasswordEdt = (EditText) view.findViewById(R.id.testPasswordEdt);
		prodUsernameEdt = (EditText) view.findViewById(R.id.prodUsernameEdt);
		prodPasswordEdt = (EditText) view.findViewById(R.id.prodPasswordEdt);

		testUsernameEdt.setText(getAppData().getTestUsername());
		testPasswordEdt.setText(getAppData().getTestPassword());
		if (!TextUtils.isEmpty(getAppData().getProdUsername())) {
			prodUsernameEdt.setText(getAppData().getProdUsername());
		} else {
			prodUsernameEdt.setText(getUsername());
		}
		if (!TextUtils.isEmpty(getAppData().getProdPassword())) {
			prodPasswordEdt.setText(getAppData().getProdPassword());
		} else {
			prodPasswordEdt.setText(getAppData().getPassword());
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.reloginBtn) {
			if (!TextUtils.isEmpty(prodUsernameEdt.getText())) {
				String username = prodUsernameEdt.getText().toString();
				getAppData().setProdUsername(username);
			}

			if (!TextUtils.isEmpty(prodPasswordEdt.getText())) {
				String password = prodPasswordEdt.getText().toString();
				getAppData().setProdPassword(password);
			}

			if (!TextUtils.isEmpty(testUsernameEdt.getText())) {
				String username = testUsernameEdt.getText().toString();
				getAppData().setTestUsername(username);
			}

			if (!TextUtils.isEmpty(testPasswordEdt.getText())) {
				String password = testPasswordEdt.getText().toString();
				getAppData().setTestPassword(password);
			}

			if (apiSwitch.isChecked()) {

				if (TextUtils.isEmpty(prodUsernameEdt.getText())) {
					prodUsernameEdt.setError(getString(R.string.validateUsername));
					prodUsernameEdt.requestFocus();
					return;
				}

				if (TextUtils.isEmpty(prodPasswordEdt.getText())) {
					prodPasswordEdt.setError(getString(R.string.password_cant_be_empty));
					prodPasswordEdt.requestFocus();
					return;
				}

				logoutFromLive();
				clearTempData();
				RestHelper.HOST = RestHelper.HOST_PRODUCTION;

				signInUser(prodUsernameEdt, prodPasswordEdt);
			} else {
				if (TextUtils.isEmpty(testUsernameEdt.getText())) {
					testUsernameEdt.setError(getString(R.string.validateUsername));
					testUsernameEdt.requestFocus();
					return;
				}

				if (TextUtils.isEmpty(testPasswordEdt.getText())) {
					testPasswordEdt.setError(getString(R.string.password_cant_be_empty));
					testPasswordEdt.requestFocus();
					return;
				}

				logoutFromLive();
				clearTempData();
				RestHelper.HOST = RestHelper.HOST_TEST;

				signInUser(testUsernameEdt, testPasswordEdt);
			}
		} else if (view.getId() == R.id.apiSwitchView) {
			apiSwitch.toggle();
		}
	}

	@Override
	public void onSwitchChanged(SwitchButton switchButton, boolean checked) {
		if (checked) {
			RestHelper.HOST = RestHelper.HOST_PRODUCTION;
		} else {
			RestHelper.HOST = RestHelper.HOST_TEST;
		}

		getAppData().setApiRoute(RestHelper.HOST);
	}
}
