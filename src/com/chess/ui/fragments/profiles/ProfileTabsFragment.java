package com.chess.ui.fragments.profiles;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.UserItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.statics.Symbol;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.08.13
 * Time: 9:19
 */
public class ProfileTabsFragment extends CommonLogicFragment implements RadioGroup.OnCheckedChangeListener {


	private static final String USERNAME = "username";
	private int photoImageSize;
	private String username;
	private UserUpdateListener userUpdateListener;
	private ProgressImageView photoImg;
	private TextView usernameTxt;
	private TextView locationTxt;
	private ImageView countryImg;
	private ImageView premiumIconImg;
	private RadioGroup tabRadioGroup;
	private int previousCheckedId;
	private EnhancedImageDownloader imageLoader;
	private UserItem.Data userInfo;

	public ProfileTabsFragment() {}

	public static ProfileTabsFragment createInstance(String username) {
		ProfileTabsFragment fragment = new ProfileTabsFragment();
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

		photoImageSize = (int) (80 * getResources().getDisplayMetrics().density);
		imageLoader = new EnhancedImageDownloader(getActivity());
		userUpdateListener = new UserUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_profile_tabs_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(username);

		photoImg = (ProgressImageView) view.findViewById(R.id.photoImg);
		usernameTxt = (TextView) view.findViewById(R.id.usernameTxt);
		locationTxt = (TextView) view.findViewById(R.id.locationTxt);
		countryImg = (ImageView) view.findViewById(R.id.countryImg);
		premiumIconImg = (ImageView) view.findViewById(R.id.premiumIconImg);

//		((TextView)view.findViewById(R.id.leftTabBtn)).setText(R.string.activity);
		((TextView)view.findViewById(R.id.centerTabBtn)).setText(R.string.ratings);
		((TextView)view.findViewById(R.id.rightTabBtn)).setText(R.string.games);

		tabRadioGroup = (RadioGroup) view.findViewById(R.id.tabRadioGroup);
		tabRadioGroup.setOnCheckedChangeListener(this);
		tabRadioGroup.check(R.id.centerTabBtn);

		previousCheckedId = tabRadioGroup.getCheckedRadioButtonId();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (need2update) {
			LoadItem loadItem = LoadHelper.getUserInfo(getUserToken(), username);
			new RequestJsonTask<UserItem>(userUpdateListener).executeTask(loadItem);
		} else {
			updateUiData();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(USERNAME, username);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		updateTabs();
	}

	private void updateTabs() {
		int checkedButtonId = tabRadioGroup.getCheckedRadioButtonId();
		if (checkedButtonId != previousCheckedId) {
			previousCheckedId = checkedButtonId;
			switch (checkedButtonId) {
//				case R.id.leftTabBtn: // TODO will be implemented later
//					changeInternalFragment(new ProfileActivityFragment());
//					break;
				case R.id.centerTabBtn:
					changeInternalFragment(ProfileRatingsFragment.createInstance(username));
					break;
				case R.id.rightTabBtn:
					changeInternalFragment(ProfileGamesFragment.createInstance(username));
					break;
			}
		}
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.tab_content_frame, fragment, fragment.getClass().getSimpleName());
		transaction.addToBackStack(fragment.getClass().getSimpleName());
		transaction.commit();
	}

	private class UserUpdateListener extends ChessLoadUpdateListener<UserItem> {

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
		imageLoader.download(userInfo.getAvatar(), photoImg, photoImageSize);
		usernameTxt.setText(userInfo.getFirstName() + Symbol.SPACE + userInfo.getLastName());
		locationTxt.setText(userInfo.getLocation());
		countryImg.setImageDrawable(AppUtils.getCountryFlagScaled(getActivity(), userInfo.getCountryName()));
		premiumIconImg.setImageResource(AppUtils.getPremiumIcon(userInfo.getPremiumStatus()));
	}
}
