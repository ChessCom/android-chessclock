package com.chess.ui.fragments.profiles;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.UserItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.fragments.CommonLogicFragment;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.11.13
 * Time: 13:22
 */
public class ProfileAboutFragmentTablet extends CommonLogicFragment {

	private TextView memberSinceTxt;
	private TextView lastLoginTxt;
	private TextView birthdayTxt;
	private TextView aboutTxt;
	private UserItem.Data userInfo;

	private SimpleDateFormat dateFormatter;
	private UserUpdateListener userUpdateListener;
	private String username;

	public static ProfileAboutFragmentTablet createInstance(String username) {
		ProfileAboutFragmentTablet fragment = new ProfileAboutFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, username);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			username = getArguments().getString(USERNAME);
		} else {
			username = savedInstanceState.getString(USERNAME);
		}

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.profile_users_about_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		memberSinceTxt = (TextView) view.findViewById(R.id.memberSinceTxt);
		lastLoginTxt = (TextView) view.findViewById(R.id.lastLoginTxt);
		birthdayTxt = (TextView) view.findViewById(R.id.birthdayTxt);
		aboutTxt = (TextView) view.findViewById(R.id.aboutTxt);

	}

	@Override
	public void onResume() {
		super.onResume();

		memberSinceTxt.setText(getString(R.string.member_since_, "--"));
		lastLoginTxt.setText(getString(R.string.last_login_, "--"));
		birthdayTxt.setText(getString(R.string.birthday_, "--"));

		LoadItem loadItem = LoadHelper.getUserInfo(getUserToken(), username);
		new RequestJsonTask<UserItem>(userUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(USERNAME, username);
	}

	private class UserUpdateListener extends ChessUpdateListener<UserItem> {

		public UserUpdateListener() {
			super(UserItem.class);
		}

		@Override
		public void updateData(UserItem returnedObj) {
			super.updateData(returnedObj);
			userInfo = returnedObj.getData();

			updateUiData();

			need2update = false;
		}
	}

	private void updateUiData() {
		Date memberDate = new Date(userInfo.getLastLoginDate() * 1000);

		memberSinceTxt.setText(getString(R.string.member_since_, dateFormatter.format(memberDate)));

		Date loginDate = new Date(userInfo.getLastLoginDate() * 1000);
		lastLoginTxt.setText(getString(R.string.last_login_, dateFormatter.format(loginDate)));

		Date birthDate = new Date(userInfo.getDateOfBirth() * 1000);
		birthdayTxt.setText(getString(R.string.birthday_, dateFormatter.format(birthDate)));

		aboutTxt.setText(Html.fromHtml(userInfo.getAbout()));
	}

	private void init() {
		dateFormatter = new SimpleDateFormat(getString(R.string.common_date_format));
		userUpdateListener = new UserUpdateListener();

	}

}
