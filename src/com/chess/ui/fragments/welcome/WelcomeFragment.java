package com.chess.ui.fragments.welcome;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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
import com.chess.ui.interfaces.WelcomeTabsFace;
import com.chess.utilities.AppUtils;
import com.flurry.android.FlurryAgent;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.02.13
 * Time: 19:54
 */
public class WelcomeFragment extends CommonLogicFragment implements YouTubePlayer.OnInitializedListener,
		YouTubePlayer.OnFullscreenListener {

	private static final int PAGE_CNT = 4;
	public static final int SIGN_UP_PAGE = 3;

	public static final String YOUTUBE_DEMO_LINK = "AgTQJUhK2MY";
	private static final String YOUTUBE_FRAGMENT_TAG = "youtube fragment";
	private WelcomeTabsFace parentFace;


	private RadioGroup homePageRadioGroup;
	private LayoutInflater inflater;
	private ViewPager viewPager;
	private YouTubePlayerSupportFragment youTubePlayerFragment;
	private View youTubeFrameContainer1;
	private View youTubeFrameContainer2;
	private View youTubeFrameContainer3;
	private boolean youtubeFragmentGoFullScreen;

	// SignUp Part
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
	private YouTubePlayer youTubePlayer;

	public WelcomeFragment() {
	}

	public static WelcomeFragment createInstance(WelcomeTabsFace parentFace) {
		WelcomeFragment fragment = new WelcomeFragment();
		fragment.parentFace = parentFace;
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_welcome_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		enableSlideMenus(false);

		inflater = LayoutInflater.from(getActivity());

		viewPager = (ViewPager) view.findViewById(R.id.viewPager);
		WelcomePagerAdapter mainPageAdapter = new WelcomePagerAdapter();
		viewPager.setAdapter(mainPageAdapter);
		viewPager.setOnPageChangeListener(pageChangeListener);

		homePageRadioGroup = (RadioGroup) view.findViewById(R.id.pagerIndicatorGroup);
		for (int i = 0; i < PAGE_CNT; ++i) {
			inflater.inflate(R.layout.new_page_indicator_view, homePageRadioGroup, true);
		}

		((RadioButton) homePageRadioGroup.getChildAt(0)).setChecked(true);

		{// SignUp part
			registerUpdateListener = new RegisterUpdateListener();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (!youtubeFragmentGoFullScreen) {
			releaseYouTubeFragment();
		}
	}

	private final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			if (position == SIGN_UP_PAGE) {
				homePageRadioGroup.setVisibility(View.GONE);
			} else {
				hideKeyBoard();
				homePageRadioGroup.setVisibility(View.VISIBLE);
				((RadioButton) homePageRadioGroup.getChildAt(position)).setChecked(true);
			}

			parentFace.onPageSelected(position);
			if (position == SIGN_UP_PAGE) {
				releaseYouTubeFragment();
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int position) {
		}
	};

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.playBtn1) {
			initYoutubeFragment(R.id.youTubeFrameContainer1);
			youTubeFrameContainer1.setVisibility(View.VISIBLE);
		} else if (view.getId() == R.id.playBtn2) {
			initYoutubeFragment(R.id.youTubeFrameContainer2);
			youTubeFrameContainer2.setVisibility(View.VISIBLE);
		} else if (view.getId() == R.id.playBtn3) {
			initYoutubeFragment(R.id.youTubeFrameContainer3);
			youTubeFrameContainer3.setVisibility(View.VISIBLE);
		} else if (view.getId() == R.id.completeSignUpBtn) {
			if (!checkRegisterInfo()) {
				return;
			}

			if (!AppUtils.isNetworkAvailable(getActivity())) {
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, BasePopupsFragment.NETWORK_CHECK_TAG);
				return;
			}

			submitRegisterInfo();
		}
	}

	private void initYoutubeFragment(int containerId) {
		youTubePlayerFragment = new YouTubePlayerSupportFragment();
		getFragmentManager().beginTransaction()
				.replace(containerId/*R.id.youTubeFrameContainer*/, youTubePlayerFragment, YOUTUBE_FRAGMENT_TAG)
				.commit();
		youTubePlayerFragment.initialize(AppConstants.YOUTUBE_DEVELOPER_KEY, this);
	}

	private void releaseYouTubeFragment() {
		if (youTubeFrameContainer1 != null)
			youTubeFrameContainer1.setVisibility(View.GONE);
		if (youTubeFrameContainer2 != null)
			youTubeFrameContainer2.setVisibility(View.GONE);
		if (youTubeFrameContainer3 != null)
			youTubeFrameContainer3.setVisibility(View.GONE);
		if (youTubePlayerFragment != null && !youTubePlayerFragment.isDetached()) {
			getFragmentManager().beginTransaction()
					.detach(youTubePlayerFragment)
					.commit();
			youTubePlayerFragment = null;
		}
	}

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
		if (!wasRestored) {
			youTubePlayer.cueVideo(YOUTUBE_DEMO_LINK);
		}
		this.youTubePlayer = youTubePlayer;
		youTubePlayer.setOnFullscreenListener(this);
	}

	private static final int RECOVERY_DIALOG_REQUEST = 1;

	@Override
	public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
		if (errorReason.isUserRecoverableError()) {
			errorReason.getErrorDialog(getActivity(), RECOVERY_DIALOG_REQUEST).show();
		} else {
			String errorMessage = String.format("error_player", errorReason.toString());
			Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onFullscreen(boolean youtubeFragmentGoFullScreen) {
		this.youtubeFragmentGoFullScreen = youtubeFragmentGoFullScreen;
	}

	/**
	 *
	 * @return true player was previously initiated and fullscreen was turned off
	 */
	public boolean hideYoutubeFullScreen() {
		if (youTubePlayer != null) {
			youTubePlayer.setFullscreen(false);
			return true;
		} else {
			return false;
		}
	}

	public boolean swipeBackFromSignUp() {
		if (viewPager.getCurrentItem() == SIGN_UP_PAGE) {
			viewPager.setCurrentItem(SIGN_UP_PAGE - 1, true);
			return true;
		} else {
			return false;
		}
	}

	private class WelcomePagerAdapter extends PagerAdapter {

		private RelativeLayout firstView;
		private RelativeLayout secondView;
		private RelativeLayout thirdView;
		private RelativeLayout signUpView;
		private boolean initiatedFirst;
		private boolean initiatedSecond;
		private boolean initiatedThird;

		@Override
		public int getCount() {
			return PAGE_CNT;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = null;
			switch (position) {
				case 0:
					if (firstView == null) {
						firstView = (RelativeLayout) inflater.inflate(R.layout.new_welcome_frame_1, container, false);
					}
					view = firstView;

					// add youTubeView to control visibility
					youTubeFrameContainer1 = firstView.findViewById(R.id.youTubeFrameContainer1);
					youTubeFrameContainer1.setVisibility(View.GONE);

					int orientation = getResources().getConfiguration().orientation; // auto-init for fullscreen
					if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
						Fragment fragmentByTag = getFragmentManager().findFragmentByTag(YOUTUBE_FRAGMENT_TAG);
						youTubePlayerFragment = (YouTubePlayerSupportFragment) fragmentByTag;
						getFragmentManager().beginTransaction()
								.replace(R.id.youTubeFrameContainer1, youTubePlayerFragment, YOUTUBE_FRAGMENT_TAG)
								.commit();
						youTubePlayerFragment.initialize(AppConstants.YOUTUBE_DEVELOPER_KEY, WelcomeFragment.this);
					}

					if (!initiatedFirst) {
						// add ImageView back
						ImageView imageView = new ImageView(getContext());
						imageView.setAdjustViewBounds(true);
						imageView.setId(R.id.firstWelcomeImg);
						imageView.setScaleType(ImageView.ScaleType.FIT_XY);
						imageView.setImageResource(R.drawable.img_welcome_back);

						int screenWidth = getResources().getDisplayMetrics().widthPixels;
						int imageHeight = (int) (screenWidth / 1.3f);

						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
						params.addRule(RelativeLayout.CENTER_IN_PARENT);
						firstView.addView(imageView, 0, params);


						firstView.findViewById(R.id.playBtn1).setOnClickListener(WelcomeFragment.this);


						initiatedFirst = true;
					}
					break;
				case 1:
					if (secondView == null) {
						secondView = (RelativeLayout) inflater.inflate(R.layout.new_welcome_frame_2, container, false);
					}
					view = secondView;

					// add youTubeView to control visibility
					youTubeFrameContainer2 = secondView.findViewById(R.id.youTubeFrameContainer2);
					youTubeFrameContainer2.setVisibility(View.GONE);

					orientation = getResources().getConfiguration().orientation; // auto-init for fullscreen
					if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
						Fragment fragmentByTag = getFragmentManager().findFragmentByTag(YOUTUBE_FRAGMENT_TAG);
						youTubePlayerFragment = (YouTubePlayerSupportFragment) fragmentByTag;
						getFragmentManager().beginTransaction()
								.replace(R.id.youTubeFrameContainer2, youTubePlayerFragment, YOUTUBE_FRAGMENT_TAG)
								.commit();
						youTubePlayerFragment.initialize(AppConstants.YOUTUBE_DEVELOPER_KEY, WelcomeFragment.this);
					}

					if (!initiatedSecond) {
						ImageView imageView = new ImageView(getContext());
						imageView.setAdjustViewBounds(true);
						imageView.setId(R.id.secondWelcomeImg);
						imageView.setScaleType(ImageView.ScaleType.FIT_XY);
						imageView.setImageResource(R.drawable.img_welcome_two_back);

						int screenWidth = getResources().getDisplayMetrics().widthPixels;
						int imageHeight = (int) (screenWidth / 1.3f);

						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
						params.addRule(RelativeLayout.CENTER_IN_PARENT);
						secondView.addView(imageView, 0, params);

						secondView.findViewById(R.id.playBtn2).setOnClickListener(WelcomeFragment.this);

						initiatedSecond = true;
					}
					break;
				case 2:
					if (thirdView == null) {
						thirdView = (RelativeLayout) inflater.inflate(R.layout.new_welcome_frame_3, container, false);
					}
					view = thirdView;

					// add youTubeView to control visibility
					youTubeFrameContainer3 = thirdView.findViewById(R.id.youTubeFrameContainer3);
					youTubeFrameContainer3.setVisibility(View.GONE);

					orientation = getResources().getConfiguration().orientation; // auto-init for fullscreen
					if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
						Fragment fragmentByTag = getFragmentManager().findFragmentByTag(YOUTUBE_FRAGMENT_TAG);
						youTubePlayerFragment = (YouTubePlayerSupportFragment) fragmentByTag;
						getFragmentManager().beginTransaction()
								.replace(R.id.youTubeFrameContainer3, youTubePlayerFragment, YOUTUBE_FRAGMENT_TAG)
								.commit();
						youTubePlayerFragment.initialize(AppConstants.YOUTUBE_DEVELOPER_KEY, WelcomeFragment.this);
					}

					if (!initiatedThird) {
						ImageView imageView = new ImageView(getContext());
						imageView.setAdjustViewBounds(true);
						imageView.setId(R.id.thirdWelcomeImg);
						imageView.setScaleType(ImageView.ScaleType.FIT_XY);
						imageView.setImageResource(R.drawable.img_welcome_three_back);

						int screenWidth = getResources().getDisplayMetrics().widthPixels;
						int imageHeight = (int) (screenWidth / 1.3f);

						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
						params.addRule(RelativeLayout.CENTER_IN_PARENT);

						thirdView.addView(imageView, 0, params);

						thirdView.findViewById(R.id.playBtn3).setOnClickListener(WelcomeFragment.this);

						initiatedThird = true;
					}
					break;
				case 3:
					if (signUpView == null) {
						signUpView = (RelativeLayout) inflater.inflate(R.layout.new_welcome_sign_up_frame, container, false);
					}
					view = signUpView;

					userNameEdt = (EditText) view.findViewById(R.id.usernameEdt);
					emailEdt = (EditText) view.findViewById(R.id.emailEdt);
					passwordEdt = (EditText) view.findViewById(R.id.passwordEdt);
					passwordRetypeEdt = (EditText) view.findViewById(R.id.passwordRetypeEdt);
					view.findViewById(R.id.completeSignUpBtn).setOnClickListener(WelcomeFragment.this);

					userNameEdt.addTextChangedListener(new FieldChangeWatcher(userNameEdt));
					emailEdt.addTextChangedListener(new FieldChangeWatcher(emailEdt));
					passwordEdt.addTextChangedListener(new FieldChangeWatcher(passwordEdt));
					passwordRetypeEdt.addTextChangedListener(new FieldChangeWatcher(passwordRetypeEdt));

					setLoginFields(userNameEdt, passwordEdt);
					break;

				default:
					break;
			}

			container.addView(view);

			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object view) {
			container.removeView((View) view);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
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

		new RequestJsonTask<RegisterItem>(registerUpdateListener).executeTask(loadItem);
	}

	private class RegisterUpdateListener extends CommonLogicFragment.ChessUpdateListener<RegisterItem> {

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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {  // TODO restore
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == BasePopupsFragment.NETWORK_REQUEST) {
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
