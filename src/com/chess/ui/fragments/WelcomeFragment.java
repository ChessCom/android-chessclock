package com.chess.ui.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.ui.views.drawables.LogoBackgroundDrawable;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;
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
	private Interpolator accelerator = new AccelerateInterpolator();
	private Interpolator decelerator = new DecelerateInterpolator();

	private ObjectAnimator flipFirstHalf;

	private RadioGroup homePageRadioGroup;
	private LayoutInflater inflater;
	private ViewPager viewPager;
	private YouTubePlayerView youTubeView;
	private YouTubePlayerSupportFragment youTubePlayerFragment;


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

			youTubePlayerFragment.getView().setVisibility(View.VISIBLE);

		} else if (v.getId() == R.id.whatChessComTxt) {
			showNextPage();
		}
	}

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
		if (!wasRestored) {
			youTubePlayer.cueVideo("AgTQJUhK2MY");
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

		private View firstView;
		private View secondView;
		private View thirdView;
		private View fourthView;
		private boolean initiated;

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
						firstView = inflater.inflate(R.layout.new_welcome_one_frame, container, false);
					}
					view = firstView;

					if (!initiated) {
						{// add youTubeView programmatically to adjust height

							youTubePlayerFragment =
									(YouTubePlayerSupportFragment) getFragmentManager().findFragmentById(R.id.youtubeFragment);
							youTubePlayerFragment.initialize(AppConstants.YOUTUBE_DEVELOPER_KEY, WelcomeFragment.this);
							youTubePlayerFragment.getView().setVisibility(View.GONE);

/*
							// 641 x 324
							float k = (float)641/324;
							int height = (int) (getResources().getDisplayMetrics().widthPixels / k);
							RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
							params.addRule(RelativeLayout.ALIGN_TOP, R.id.firstBackImg);
							((ViewGroup) view).addView(youTubeView, params);
*/
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
						initiated = true;
					}
					break;
				case 1:
					if (secondView == null) {
						secondView = inflater.inflate(R.layout.new_welcome_two_frame, container, false);
					}
					view = secondView;
					break;
				case 2:
					if (thirdView == null) {
						thirdView = inflater.inflate(R.layout.new_welcome_three_frame, container, false);
					}
					view = thirdView;
					break;
				case 3:
					if (fourthView == null) {
						fourthView = inflater.inflate(R.layout.new_welcome_three_frame, container, false);
					}
					view = fourthView;
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
