package com.chess.ui.fragments.sign_in;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.*;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.views.drawables.LogoBackgroundDrawable;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.slidingmenu.lib.SlidingMenu;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.02.13
 * Time: 19:54
 */
public class WelcomeFragment extends CommonLogicFragment implements YouTubePlayer.OnInitializedListener {

	private static final int PAGE_CNT = 4;

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


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_welcome_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		getActivityFace().setTouchModeToSlidingMenu(SlidingMenu.TOUCHMODE_NONE);
		showActionBar(false);

		LogoBackgroundDrawable logoBackgroundDrawable = new LogoBackgroundDrawable(getActivity());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			view.findViewById(R.id.mainView).setBackground(logoBackgroundDrawable);
		} else {
			view.findViewById(R.id.mainView).setBackgroundDrawable(logoBackgroundDrawable);
		}

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
	}

	@Override
	public void onResume() {
		super.onResume();

		handler.postDelayed(startAnimation, ANIMATION_DELAY);
	}

	private final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			((RadioButton) homePageRadioGroup.getChildAt(position)).setChecked(true);
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
	public void onClick(View v) {
		if (v.getId() == R.id.signInBtn) {
			getActivityFace().openFragment(new SignInFragment());
		} else if (v.getId() == R.id.signUpBtn) {
			getActivityFace().openFragment(new SignUpFragment());
		} else if (v.getId() == R.id.playBtn) {

			youTubeFrameContainer.setVisibility(View.VISIBLE);

		} else if (v.getId() == R.id.whatChessComTxt) {
			showNextPage();
		}
	}

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
		if (!wasRestored) {
			youTubePlayer.cueVideo(YOUTUBE_DEMO_LINK);
		}
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

	private class WelcomePagerAdapter extends PagerAdapter {

		private RelativeLayout firstView;
		private RelativeLayout secondView;
		private RelativeLayout thirdView;
		private RelativeLayout fourthView;
		private boolean initiatedFirst;
		private boolean initiatedSecond;
		private boolean initiatedThird;
		private boolean initiatedFour;

		@Override
		public int getCount() {
			return PAGE_CNT;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = null;
			switch (position) {
				case 0:
					if (firstView == null){
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
							if (youTubePlayerFragment == null){
								youTubePlayerFragment = new YouTubePlayerSupportFragment();
								fragmentManager.beginTransaction()
										.add(R.id.youTubeFrameContainer, youTubePlayerFragment, YOUTUBE_FRAGMENT_TAG)
										.commit();
							}

//							youTubePlayerFragment =	(YouTubePlayerSupportFragment) fragmentManager.findFragmentById(R.id.youtubeFragment);
							youTubePlayerFragment.initialize(AppConstants.YOUTUBE_DEVELOPER_KEY, WelcomeFragment.this);

							youTubeFrameContainer.setVisibility(View.GONE);
						}

						view.findViewById(R.id.playBtn).setOnClickListener(WelcomeFragment.this);

						View whatChessComTxt = view.findViewById(R.id.whatChessComTxt);
						whatChessComTxt.setOnClickListener(WelcomeFragment.this);

						flipFirstHalf = ObjectAnimator.ofFloat(whatChessComTxt, "rotationX", 0f, 90f);
						flipFirstHalf.setDuration(DURATION);
						flipFirstHalf.setInterpolator(accelerator);

						final ObjectAnimator flipSecondHalf = ObjectAnimator.ofFloat(whatChessComTxt ,"rotationX", -90f, 0f);
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
				default: break;
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
}
