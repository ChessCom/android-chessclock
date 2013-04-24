package com.chess.ui.fragments.sign_in;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.*;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.RegisterItem;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;
import com.facebook.android.Facebook;
import com.flurry.android.FlurryAgent;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.slidingmenu.lib.SlidingMenu;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.02.13
 * Time: 19:54
 */
public class WelcomeFragment extends ProfileSetupsFragment implements YouTubePlayer.OnInitializedListener, YouTubePlayer.OnFullscreenListener {

	private static final int PAGE_CNT = 5;

	private static final long ANIMATION_DELAY = 2000;
	private static final long REPEAT_TIMEOUT = 6000;
	private static final int DURATION = 450;
	public static final String YOUTUBE_DEMO_LINK = "AgTQJUhK2MY";
	private static final String YOUTUBE_FRAGMENT_TAG = "youtube fragment";

	private Interpolator accelerator = new AccelerateInterpolator();
	private Interpolator decelerator = new DecelerateInterpolator();

	private ObjectAnimator flipFirstHalf;

	private RadioGroup homePageRadioGroup;
	private LayoutInflater inflater;
	private ViewPager viewPager;
	private YouTubePlayerSupportFragment youTubePlayerFragment;
	private View youTubeFrameContainer;
	private boolean youtubeFragmentGoFullScreen;
	private View bottomButtonsLay;

	// SignUp Part

	protected Pattern emailPattern = Pattern.compile("[a-zA-Z0-9\\._%\\+\\-]+@[a-zA-Z0-9\\.\\-]+\\.[a-zA-Z]{2,4}");
	protected Pattern gMailPattern = Pattern.compile("[a-zA-Z0-9\\._%\\+\\-]+@[g]");   // TODO use for autoComplete
	private static final String DEFAULT_COUNTRY = "XX";  // International

	private EditText userNameEdt;
	private EditText emailEdt;
	private EditText passwordEdt;
	private EditText passwordRetypeEdt;

	private String userName;
	private String email;
	private String password;
	private RegisterUpdateListener registerUpdateListener;
	private String[] countryCodes;
	private String countryCodeName;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_welcome_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		getActivityFace().setTouchModeToSlidingMenu(SlidingMenu.TOUCHMODE_NONE);

		inflater = LayoutInflater.from(getActivity());

		viewPager = (ViewPager) view.findViewById(R.id.viewPager);
		WelcomePagerAdapter mainPageAdapter = new WelcomePagerAdapter();
		viewPager.setAdapter(mainPageAdapter);
		viewPager.setOnPageChangeListener(pageChangeListener);

		homePageRadioGroup = (RadioGroup) view.findViewById(R.id.pagerIndicatorGroup);
		for (int i = 0; i < PAGE_CNT; ++i) {
			inflater.inflate(R.layout.new_page_indicator_view, homePageRadioGroup, true);
		}

		view.findViewById(R.id.signUpBtn).setOnClickListener(this);
		view.findViewById(R.id.signInBtn).setOnClickListener(this);

		((RadioButton) homePageRadioGroup.getChildAt(0)).setChecked(true);

		bottomButtonsLay = view.findViewById(R.id.bottomButtonsLay);

		{// SignUp part
			countryCodes = getResources().getStringArray(R.array.new_countries_codes);
			registerUpdateListener = new RegisterUpdateListener();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		handler.postDelayed(startAnimation, ANIMATION_DELAY);

		// SignUp part
		String userCountry = AppData.getUserCountry(getActivity());
		if (userCountry == null) {
			String locale = getResources().getConfiguration().locale.getCountry();

			if (locale != null) {
				int i;
				boolean found = false;
				for (i = 0; i < countryCodes.length; i++) {
					String countryCode = countryCodes[i];
					if (locale.equals(countryCode)) {
						found = true;
						break;
					}
				}
				if (found) {
					countryCodeName = countryCodes[i];
				} else {
					countryCodeName = DEFAULT_COUNTRY;
				}
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		handler.removeCallbacks(startAnimation);

		if (!youtubeFragmentGoFullScreen) {
			youTubeFrameContainer.setVisibility(View.GONE);
			getFragmentManager().beginTransaction()
					.remove(youTubePlayerFragment)
					.commit();
		}
	}

	private final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			if (position == 4) {
				bottomButtonsLay.setVisibility(View.GONE);
				homePageRadioGroup.setVisibility(View.GONE);
			} else {
				hideKeyBoard();
				bottomButtonsLay.setVisibility(View.VISIBLE);
				homePageRadioGroup.setVisibility(View.VISIBLE);
				((RadioButton) homePageRadioGroup.getChildAt(position)).setChecked(true);
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int position) {
		}
	};

	public void showNextPage() {
		int currentItem = viewPager.getCurrentItem();
		if (currentItem <= PAGE_CNT) {
			viewPager.setCurrentItem(currentItem + 1, true);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.signInBtn) {
			getActivityFace().openFragment(new SignInFragment());
		} else if (view.getId() == R.id.signUpBtn) {
			getActivityFace().openFragment(new SignUpFragment());
		} else if (view.getId() == R.id.playBtn) {

			youTubeFrameContainer.setVisibility(View.VISIBLE);

		} else if (view.getId() == R.id.whatChessComTxt) {
			showNextPage();
		} else if (view.getId() == R.id.RegSubmitBtn) {
			if (!checkRegisterInfo()) {
				return;
			}

			if (!AppUtils.isNetworkAvailable(getActivity())) { // check only if live
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, BasePopupsFragment.NETWORK_CHECK_TAG);
				return;
			}

			submitRegisterInfo();
		}
	}

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
		if (!wasRestored) {
			youTubePlayer.cueVideo(YOUTUBE_DEMO_LINK);
		}
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

	private class WelcomePagerAdapter extends PagerAdapter {

		private RelativeLayout firstView;
		private RelativeLayout secondView;
		private RelativeLayout thirdView;
		private RelativeLayout fourthView;
		private RelativeLayout signUpView;
		private boolean initiatedFirst;
		private boolean initiatedSecond;
		private boolean initiatedThird;
		private boolean initiatedFour;
		private boolean initiatedFifth;

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
						firstView = (RelativeLayout) inflater.inflate(R.layout.new_welcome_one_frame, container, false);
					}
					view = firstView;

					if (!initiatedFirst) {
						{// add ImageView back
							ImageView imageView = new ImageView(getContext());
							imageView.setAdjustViewBounds(true);
							imageView.setScaleType(ImageView.ScaleType.FIT_XY);
							imageView.setImageResource(R.drawable.img_welcome_back);
							imageView.setId(R.id.firstBackImg);

							int screenWidth = getResources().getDisplayMetrics().widthPixels;
							int imageHeight = screenWidth / 2;

							RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
							params.addRule(RelativeLayout.CENTER_IN_PARENT);
							firstView.addView(imageView, 0, params);
						}

						{// add youTubeView to control visibility
							youTubeFrameContainer = firstView.findViewById(R.id.youTubeFrameContainer);

							FragmentManager fragmentManager = getFragmentManager();
							youTubePlayerFragment = (YouTubePlayerSupportFragment) fragmentManager.findFragmentByTag(YOUTUBE_FRAGMENT_TAG);
							if (youTubePlayerFragment == null) {
								youTubePlayerFragment = new YouTubePlayerSupportFragment();
								fragmentManager.beginTransaction()
										.add(R.id.youTubeFrameContainer, youTubePlayerFragment, YOUTUBE_FRAGMENT_TAG)
										.commit();
							}

							youTubePlayerFragment.initialize(AppConstants.YOUTUBE_DEVELOPER_KEY, WelcomeFragment.this);

							youTubeFrameContainer.setVisibility(View.GONE);
						}

						view.findViewById(R.id.playBtn).setOnClickListener(WelcomeFragment.this);

						View whatChessComTxt = view.findViewById(R.id.whatChessComTxt);
						whatChessComTxt.setOnClickListener(WelcomeFragment.this);

						flipFirstHalf = ObjectAnimator.ofFloat(whatChessComTxt, "rotationX", 0f, 90f);
						flipFirstHalf.setDuration(DURATION);
						flipFirstHalf.setInterpolator(accelerator);

						final ObjectAnimator flipSecondHalf = ObjectAnimator.ofFloat(whatChessComTxt, "rotationX", -90f, 0f);
						flipSecondHalf.setDuration(DURATION);
						flipSecondHalf.setInterpolator(decelerator);

						flipFirstHalf.addListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator anim) {
								flipSecondHalf.start();
							}
						});
						initiatedFirst = true;
					}
					break;
				case 1:
					if (secondView == null) {
						secondView = (RelativeLayout) inflater.inflate(R.layout.new_welcome_two_frame, container, false);
					}
					view = secondView;

					if (!initiatedSecond) {
						ImageView imageView = new ImageView(getContext());
						imageView.setAdjustViewBounds(true);
						imageView.setScaleType(ImageView.ScaleType.FIT_XY);
						imageView.setImageResource(R.drawable.img_welcome_two_back);

						int screenWidth = getResources().getDisplayMetrics().widthPixels;
						int imageHeight = screenWidth / 2;

						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
						params.addRule(RelativeLayout.CENTER_IN_PARENT);
						secondView.addView(imageView, 0, params);

						initiatedSecond = true;
					}
					break;
				case 2:
					if (thirdView == null) {
						thirdView = (RelativeLayout) inflater.inflate(R.layout.new_welcome_three_frame, container, false);
					}
					view = thirdView;

					if (!initiatedThird) {
						ImageView imageView = new ImageView(getContext());
						imageView.setAdjustViewBounds(true);
						imageView.setScaleType(ImageView.ScaleType.FIT_XY);
						imageView.setImageResource(R.drawable.img_welcome_three_back);

						int screenWidth = getResources().getDisplayMetrics().widthPixels;
						int imageHeight = screenWidth / 2;

						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
						params.addRule(RelativeLayout.CENTER_IN_PARENT);

						thirdView.addView(imageView, 0, params);

						initiatedThird = true;
					}
					break;
				case 3:
					if (fourthView == null) {
						fourthView = (RelativeLayout) inflater.inflate(R.layout.new_welcome_four_frame, container, false);
					}
					view = fourthView;

					if (!initiatedFour) {
						ImageView imageView = new ImageView(getContext());
						imageView.setAdjustViewBounds(true);
						imageView.setScaleType(ImageView.ScaleType.FIT_XY);
						imageView.setImageResource(R.drawable.img_welcome_four_back);

						int screenWidth = getResources().getDisplayMetrics().widthPixels;
						int imageHeight = screenWidth / 2;

						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
						params.addRule(RelativeLayout.CENTER_IN_PARENT);
						fourthView.addView(imageView, 0, params);

						initiatedFour = true;
					}
					break;
				case 4:
					if (signUpView == null) {
						signUpView = (RelativeLayout) inflater.inflate(R.layout.new_welcome_sign_up_frame, container, false);
					}
					view = signUpView;

//					if (!initiatedFifth) {

					userNameEdt = (EditText) view.findViewById(R.id.usernameEdt);
					emailEdt = (EditText) view.findViewById(R.id.emailEdt);
					passwordEdt = (EditText) view.findViewById(R.id.passwordEdt);
					passwordRetypeEdt = (EditText) view.findViewById(R.id.passwordRetypeEdt);
					view.findViewById(R.id.RegSubmitBtn).setOnClickListener(WelcomeFragment.this);

					userNameEdt.addTextChangedListener(new FieldChangeWatcher(userNameEdt));
					emailEdt.addTextChangedListener(new FieldChangeWatcher(emailEdt));
					passwordEdt.addTextChangedListener(new FieldChangeWatcher(passwordEdt));
					passwordRetypeEdt.addTextChangedListener(new FieldChangeWatcher(passwordRetypeEdt));

					setLoginFields(userNameEdt, passwordEdt);
//					}
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

	private Runnable startAnimation = new Runnable() {
		@Override
		public void run() {
			flipFirstHalf.start();
			handler.postDelayed(this, REPEAT_TIMEOUT);
		}
	};

	/* ------------- Sign Up Part --------------------------- */
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


		return true;
	}

	private void submitRegisterInfo() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_REGISTER);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_USER_NAME, userName);
		loadItem.addRequestParams(RestHelper.P_PASSWORD, password);
		loadItem.addRequestParams(RestHelper.P_EMAIL, email);
		loadItem.addRequestParams(RestHelper.P_COUNTRY_CODE, countryCodeName);
		loadItem.addRequestParams(RestHelper.P_APP_TYPE, RestHelper.V_ANDROID);

		new RequestJsonTask<RegisterItem>(registerUpdateListener).executeTask(loadItem);
	}

	private class RegisterUpdateListener extends CommonLogicFragment.ChessUpdateListener<RegisterItem> {

		public RegisterUpdateListener() {
			super(RegisterItem.class);
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
			if (requestCode == Facebook.DEFAULT_AUTH_ACTIVITY_CODE) {
				CommonLogicFragment.facebook.authorizeCallback(requestCode, resultCode, data);
			} else if (requestCode == BasePopupsFragment.NETWORK_REQUEST) {
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
