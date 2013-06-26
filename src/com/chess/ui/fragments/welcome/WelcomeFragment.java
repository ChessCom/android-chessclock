package com.chess.ui.fragments.welcome;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.RegisterItem;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.FragmentTabsFace;
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
public class WelcomeFragment extends CommonLogicFragment implements YouTubePlayer.OnFullscreenListener{

	private static final int PAGE_CNT = 4;
	public static final int SIGN_UP_PAGE = 3;

	public static final String YOUTUBE_DEMO_LINK1 = "AgTQJUhK2MY";
	public static final String YOUTUBE_DEMO_LINK2 = "sXZ9fAO7YWg";
	public static final String YOUTUBE_DEMO_LINK3 = "iHXCZAShyIY";
	private static final String YOUTUBE_FRAGMENT_TAG = "youtube fragment";

	private FragmentTabsFace parentFace;


	private RadioGroup homePageRadioGroup;
	private LayoutInflater inflater;
	private ViewPager viewPager;
	private YouTubePlayerSupportFragment youTubePlayerFragment1;
	private YouTubePlayerSupportFragment youTubePlayerFragment2;
	private YouTubePlayerSupportFragment youTubePlayerFragment3;
	private FrameLayout youTubeFrameContainer1;
	private FrameLayout youTubeFrameContainer2;
	private FrameLayout youTubeFrameContainer3;
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
	private YouTubePlayer youTubePlayer1;
	private YouTubePlayer youTubePlayer2;
	private YouTubePlayer youTubePlayer3;
	private RoboButton closeYouTubeBtn1;
	private RoboButton closeYouTubeBtn2;
	private RoboButton closeYouTubeBtn3;

	public WelcomeFragment() {
	}

	public static WelcomeFragment createInstance(FragmentTabsFace parentFace) {
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
			releaseYouTubeFragment(youTubeFrameContainer1, youTubePlayerFragment1);
			releaseYouTubeFragment(youTubeFrameContainer2, youTubePlayerFragment2);
			releaseYouTubeFragment(youTubeFrameContainer3, youTubePlayerFragment3);
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
				releaseYouTubeFragment(youTubeFrameContainer1, youTubePlayerFragment1);
				releaseYouTubeFragment(youTubeFrameContainer2, youTubePlayerFragment2);
				releaseYouTubeFragment(youTubeFrameContainer3, youTubePlayerFragment3);
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
			youTubePlayerFragment1 = initYoutubeFragment(R.id.youTubeFrameContainer1);
			youTubeFrameContainer1.setVisibility(View.VISIBLE);
			closeYouTubeBtn1.setVisibility(View.VISIBLE);
		} else if (view.getId() == R.id.playBtn2) {
			youTubePlayerFragment2 = initYoutubeFragment(R.id.youTubeFrameContainer2);
			youTubeFrameContainer2.setVisibility(View.VISIBLE);
			closeYouTubeBtn2.setVisibility(View.VISIBLE);
		} else if (view.getId() == R.id.playBtn3) {
			youTubePlayerFragment3 = initYoutubeFragment(R.id.youTubeFrameContainer3);
			youTubeFrameContainer3.setVisibility(View.VISIBLE);
			closeYouTubeBtn3.setVisibility(View.VISIBLE);
		} else if (view.getId() == R.id.completeSignUpBtn) {
			if (!checkRegisterInfo()) {
				return;
			}

			if (!AppUtils.isNetworkAvailable(getActivity())) {
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
				return;
			}

			submitRegisterInfo();
		} else if (view.getId() == R.id.closeBtn1) { // TODO adjust properly
			releaseYouTubeFragment(youTubeFrameContainer1, youTubePlayerFragment1);
			youTubePlayerFragment1 = null;
			closeYouTubeBtn1.setVisibility(View.GONE);
		} else if (view.getId() == R.id.closeBtn2) {
			releaseYouTubeFragment(youTubeFrameContainer2, youTubePlayerFragment2);
			youTubePlayerFragment2 = null;
			closeYouTubeBtn2.setVisibility(View.GONE);
		} else if (view.getId() == R.id.closeBtn3) {
			releaseYouTubeFragment(youTubeFrameContainer3, youTubePlayerFragment3);
			youTubePlayerFragment3 = null;
			closeYouTubeBtn3.setVisibility(View.GONE);
		}
	}

	private YouTubePlayerSupportFragment initYoutubeFragment(int containerId) {
		return initYoutubeFragment(containerId, new YouTubePlayerSupportFragment());
	}

	private YouTubePlayerSupportFragment initYoutubeFragment(int containerId, YouTubePlayerSupportFragment youTubePlayerFragment) {
		getFragmentManager().beginTransaction()
				.replace(containerId, youTubePlayerFragment, YOUTUBE_FRAGMENT_TAG + containerId)
				.commit();
		youTubePlayerFragment.initialize(AppConstants.YOUTUBE_DEVELOPER_KEY, new YouTubeInitListener(containerId));
		return youTubePlayerFragment;
	}

	private void releaseYouTubeFragment(FrameLayout youTubeFrameContainer, YouTubePlayerSupportFragment youTubePlayerFragment) {
		if (youTubeFrameContainer != null)
			youTubeFrameContainer.setVisibility(View.GONE);
		if (youTubePlayerFragment != null && !youTubePlayerFragment.isDetached()) {
			getFragmentManager().beginTransaction()
					.detach(youTubePlayerFragment)
					.commit();
		}
	}

	private class YouTubeInitListener implements YouTubePlayer.OnInitializedListener {
		private static final int RECOVERY_DIALOG_REQUEST = 1;
		private int containerId;

		public YouTubeInitListener(int containerId) {
			this.containerId = containerId;
		}

		@Override
		public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
			switch (containerId) {
				case R.id.youTubeFrameContainer1:
					if (!wasRestored) {
						youTubePlayer.cueVideo(YOUTUBE_DEMO_LINK1);
					}
					youTubePlayer1 = youTubePlayer;
					youTubePlayer1.setOnFullscreenListener(WelcomeFragment.this);
					youTubePlayer1.setPlaybackEventListener(new YouTubePlaybackListener());
					break;
				case R.id.youTubeFrameContainer2:
					if (!wasRestored) {
						youTubePlayer.cueVideo(YOUTUBE_DEMO_LINK2);
					}
					youTubePlayer2 = youTubePlayer;
					youTubePlayer2.setOnFullscreenListener(WelcomeFragment.this);
					youTubePlayer2.setPlaybackEventListener(new YouTubePlaybackListener());
					break;
				case R.id.youTubeFrameContainer3:
					if (!wasRestored) {
						youTubePlayer.cueVideo(YOUTUBE_DEMO_LINK3);
					}
					youTubePlayer3 = youTubePlayer;
					youTubePlayer3.setOnFullscreenListener(WelcomeFragment.this);
					youTubePlayer3.setPlaybackEventListener(new YouTubePlaybackListener());
					break;
			}

		}

		@Override
		public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
			if (errorReason.isUserRecoverableError()) {
				errorReason.getErrorDialog(getActivity(), RECOVERY_DIALOG_REQUEST).show();
			} else {
				String errorMessage = String.format("error_player", errorReason.toString());
				Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
			}
		}
	}

	private class YouTubePlaybackListener implements YouTubePlayer.PlaybackEventListener/*, YouTubePlayer.PlayerStateChangeListener*/ {

		@Override
		public void onPlaying() {
			showCloseButton(youTubeFrameContainer1, closeYouTubeBtn1, false);
			showCloseButton(youTubeFrameContainer2, closeYouTubeBtn2, false);
			showCloseButton(youTubeFrameContainer3, closeYouTubeBtn3, false);
		}

		@Override
		public void onPaused() {
			showCloseButton(youTubeFrameContainer1, closeYouTubeBtn1, true);
			showCloseButton(youTubeFrameContainer2, closeYouTubeBtn2, true);
			showCloseButton(youTubeFrameContainer3, closeYouTubeBtn3, true);
		}

		@Override
		public void onStopped() {
			showCloseButton(youTubeFrameContainer1, closeYouTubeBtn1, true);
			showCloseButton(youTubeFrameContainer2, closeYouTubeBtn2, true);
			showCloseButton(youTubeFrameContainer3, closeYouTubeBtn3, true);
		}

		@Override
		public void onBuffering(boolean b) {

		}

		@Override
		public void onSeekTo(int i) {

		}

		private void showCloseButton(FrameLayout youTubeFrameContainer, Button closeYouTubeBtn, boolean show) {
			if (youTubeFrameContainer != null && youTubeFrameContainer.getVisibility() == View.VISIBLE) {
				closeYouTubeBtn.setVisibility(show? View.VISIBLE: View.GONE);
			}
		}
	}

	@Override
	public void onFullscreen(boolean youtubeFragmentGoFullScreen) {
		this.youtubeFragmentGoFullScreen = youtubeFragmentGoFullScreen;
	}

	/**
	 * @return true player was previously initiated and fullscreen was turned off
	 */
	public boolean hideYoutubeFullScreen() {
		if (youTubePlayer1 != null) {
			youTubePlayer1.setFullscreen(false);
			return true;
		} else if (youTubePlayer2 != null) {
			youTubePlayer2.setFullscreen(false);
			return true;
		} else if (youTubePlayer3 != null) {
			youTubePlayer3.setFullscreen(false);
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

		private final int screenWidth;
		private final int imageHeight;
		private RelativeLayout firstView;
		private RelativeLayout secondView;
		private RelativeLayout thirdView;
		private RelativeLayout signUpView;
		private boolean initiatedFirst;
		private boolean initiatedSecond;
		private boolean initiatedThird;

		private WelcomePagerAdapter() {
			screenWidth = getResources().getDisplayMetrics().widthPixels;
			imageHeight = (int) (screenWidth / 1.3f);
		}

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
					youTubeFrameContainer1 = (FrameLayout) firstView.findViewById(R.id.youTubeFrameContainer1);
					youTubeFrameContainer1.setVisibility(View.GONE);
				{
					RelativeLayout.LayoutParams youTubeContainerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, imageHeight);
					youTubeContainerParams.addRule(RelativeLayout.ALIGN_TOP, R.id.firstWelcomeImg);
					youTubeFrameContainer1.setLayoutParams(youTubeContainerParams);
				}

				closeYouTubeBtn1 = (RoboButton) firstView.findViewById(R.id.closeBtn1);
				closeYouTubeBtn1.setOnClickListener(WelcomeFragment.this);

				int orientation = getResources().getConfiguration().orientation; // auto-init for fullscreen
				if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
					Fragment fragmentByTag = getFragmentManager().findFragmentByTag(YOUTUBE_FRAGMENT_TAG + R.id.youTubeFrameContainer1);
					youTubePlayerFragment1 = initYoutubeFragment(R.id.youTubeFrameContainer1, (YouTubePlayerSupportFragment) fragmentByTag);
				}

				if (!initiatedFirst) {
					// add ImageView back
					ImageView imageView = new ImageView(getContext());
					imageView.setAdjustViewBounds(true);
					imageView.setId(R.id.firstWelcomeImg);
					imageView.setScaleType(ImageView.ScaleType.FIT_XY);
					imageView.setImageResource(R.drawable.img_welcome_back);

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
					youTubeFrameContainer2 = (FrameLayout) secondView.findViewById(R.id.youTubeFrameContainer2);
					youTubeFrameContainer2.setVisibility(View.GONE);
				{
					RelativeLayout.LayoutParams youTubeContainerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, imageHeight);
					youTubeContainerParams.addRule(RelativeLayout.ALIGN_TOP, R.id.secondWelcomeImg);
					youTubeFrameContainer2.setLayoutParams(youTubeContainerParams);
				}

				closeYouTubeBtn2 = (RoboButton) secondView.findViewById(R.id.closeBtn2);
				closeYouTubeBtn2.setOnClickListener(WelcomeFragment.this);

				orientation = getResources().getConfiguration().orientation; // auto-init for fullscreen
				if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
					Fragment fragmentByTag = getFragmentManager().findFragmentByTag(YOUTUBE_FRAGMENT_TAG + R.id.youTubeFrameContainer2);
					youTubePlayerFragment2 = initYoutubeFragment(R.id.youTubeFrameContainer2, (YouTubePlayerSupportFragment) fragmentByTag);
				}

				if (!initiatedSecond) {
					ImageView imageView = new ImageView(getContext());
					imageView.setAdjustViewBounds(true);
					imageView.setId(R.id.secondWelcomeImg);
					imageView.setScaleType(ImageView.ScaleType.FIT_XY);
					imageView.setImageResource(R.drawable.img_welcome_two_back);

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
					youTubeFrameContainer3 = (FrameLayout) thirdView.findViewById(R.id.youTubeFrameContainer3);
					youTubeFrameContainer3.setVisibility(View.GONE);
				{
					RelativeLayout.LayoutParams youTubeContainerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, imageHeight);
					youTubeContainerParams.addRule(RelativeLayout.ALIGN_TOP, R.id.thirdWelcomeImg);
					youTubeFrameContainer3.setLayoutParams(youTubeContainerParams);
				}

				closeYouTubeBtn3 = (RoboButton) thirdView.findViewById(R.id.closeBtn3);
				closeYouTubeBtn3.setOnClickListener(WelcomeFragment.this);

				orientation = getResources().getConfiguration().orientation; // auto-init for fullscreen
				if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
					Fragment fragmentByTag = getFragmentManager().findFragmentByTag(YOUTUBE_FRAGMENT_TAG + R.id.youTubeFrameContainer3);
					youTubePlayerFragment3 = initYoutubeFragment(R.id.youTubeFrameContainer3, (YouTubePlayerSupportFragment) fragmentByTag);
				}

				if (!initiatedThird) {
					ImageView imageView = new ImageView(getContext());
					imageView.setAdjustViewBounds(true);
					imageView.setId(R.id.thirdWelcomeImg);
					imageView.setScaleType(ImageView.ScaleType.FIT_XY);
					imageView.setImageResource(R.drawable.img_welcome_three_back);

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

				{ // Terms link handle
					TextView termsLinkTxt = (TextView) view.findViewById(R.id.termsLinkTxt);
					termsLinkTxt.setClickable(true);
					String termsText = getString(R.string.new_by_signing_up_accept_mobile) + StaticData.SYMBOL_NEW_STR + StaticData.SYMBOL_NEW_STR
							+ getString(R.string.new_by_signing_up_accept_mobile1) ;
					termsLinkTxt.setText(Html.fromHtml(termsText));
					Linkify.addLinks(termsLinkTxt, Linkify.WEB_URLS);
					termsLinkTxt.setMovementMethod(LinkMovementMethod.getInstance());
					termsLinkTxt.setLinkTextColor(Color.WHITE);
				}
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

			preferencesEditor.putString(AppConstants.USERNAME, getTextFromField(userNameEdt));
			preferencesEditor.putInt(AppConstants.USER_PREMIUM_STATUS, RestHelper.V_BASIC_MEMBER);
			processLogin(returnedObj.getData());
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {  // TODO restore
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == NETWORK_REQUEST) {
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
