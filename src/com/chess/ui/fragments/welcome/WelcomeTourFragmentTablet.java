package com.chess.ui.fragments.welcome;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.statics.AppConstants;
import com.chess.statics.WelcomeHolder;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.FragmentTabsFace;
import com.chess.widgets.RoboButton;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.11.13
 * Time: 17:12
 */
public class WelcomeTourFragmentTablet extends CommonLogicFragment implements YouTubePlayer.OnFullscreenListener {

	private static final int PAGE_CNT = 3;

	public static final String YOUTUBE_DEMO_LINK1 = "AgTQJUhK2MY";
	public static final String YOUTUBE_DEMO_LINK2 = "AgTQJUhK2MY";
	public static final String YOUTUBE_DEMO_LINK3 = "AgTQJUhK2MY";
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

	private YouTubePlayer youTubePlayer1;
	private YouTubePlayer youTubePlayer2;
	private YouTubePlayer youTubePlayer3;
	private RoboButton closeYouTubeBtn1;
	private RoboButton closeYouTubeBtn2;
	private RoboButton closeYouTubeBtn3;
	private WelcomePagerAdapter mainPageAdapter;

	public WelcomeTourFragmentTablet() {
	}

	public static WelcomeTourFragmentTablet createInstance(FragmentTabsFace parentFace) {
		WelcomeTourFragmentTablet fragment = new WelcomeTourFragmentTablet();
		fragment.parentFace = parentFace;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mainPageAdapter = new WelcomePagerAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.welcome_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		enableSlideMenus(false);

		inflater = LayoutInflater.from(getActivity());

		viewPager = (ViewPager) view.findViewById(R.id.viewPager);
		viewPager.setAdapter(mainPageAdapter);
		viewPager.setOnPageChangeListener(pageChangeListener);

		homePageRadioGroup = (RadioGroup) view.findViewById(R.id.pagerIndicatorGroup);
		for (int i = 0; i < PAGE_CNT; ++i) {
			inflater.inflate(R.layout.page_indicator_view, homePageRadioGroup, true);
		}

		((RadioButton) homePageRadioGroup.getChildAt(0)).setChecked(true);
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
			homePageRadioGroup.setVisibility(View.VISIBLE);
			((RadioButton) homePageRadioGroup.getChildAt(position)).setChecked(true);

			if (parentFace != null) { // can be null after onSavedInstance
				parentFace.onPageSelected(position);
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
		} else if (view.getId() == R.id.loginLinkTxt) {
			((WelcomeTabsFragment) parentFace).openSignInFragment();
		} else if (view.getId() == R.id.closeBtn1) { // TODO adjust properly
			youTubePlayer1.pause();
			releaseYouTubeFragment(youTubeFrameContainer1, youTubePlayerFragment1);
			youTubePlayerFragment1 = null;
			closeYouTubeBtn1.setVisibility(View.GONE);
		} else if (view.getId() == R.id.closeBtn2) {
			youTubePlayer2.pause();
			releaseYouTubeFragment(youTubeFrameContainer2, youTubePlayerFragment2);
			youTubePlayerFragment2 = null;
			closeYouTubeBtn2.setVisibility(View.GONE);
		} else if (view.getId() == R.id.closeBtn3) {
			youTubePlayer3.pause();
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
					youTubePlayer1.setOnFullscreenListener(WelcomeTourFragmentTablet.this);
					youTubePlayer1.setPlaybackEventListener(new YouTubePlaybackListener());
					youTubePlayer1.loadVideo(YOUTUBE_DEMO_LINK1);
					break;
				case R.id.youTubeFrameContainer2:
					if (!wasRestored) {
						youTubePlayer.cueVideo(YOUTUBE_DEMO_LINK2);
					}
					youTubePlayer2 = youTubePlayer;
					youTubePlayer2.setOnFullscreenListener(WelcomeTourFragmentTablet.this);
					youTubePlayer2.setPlaybackEventListener(new YouTubePlaybackListener());
					youTubePlayer2.loadVideo(YOUTUBE_DEMO_LINK2);
					break;
				case R.id.youTubeFrameContainer3:
					if (!wasRestored) {
						youTubePlayer.cueVideo(YOUTUBE_DEMO_LINK3);
					}
					youTubePlayer3 = youTubePlayer;
					youTubePlayer3.setOnFullscreenListener(WelcomeTourFragmentTablet.this);
					youTubePlayer3.setPlaybackEventListener(new YouTubePlaybackListener());
					youTubePlayer3.loadVideo(YOUTUBE_DEMO_LINK3);
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
				closeYouTubeBtn.setVisibility(show ? View.VISIBLE : View.GONE);
			}
		}
	}

	@Override
	public void onFullscreen(boolean youtubeFragmentGoFullScreen) {
//		logTest(" WelcomeTourFragment - onFullscreen " + youtubeFragmentGoFullScreen);
		WelcomeHolder.getInstance().setFullscreen(true);
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

	private class WelcomePagerAdapter extends PagerAdapter {

		private final int screenWidth;
		private final int imageHeight;
		private RelativeLayout firstView;
		private RelativeLayout secondView;
		private RelativeLayout thirdView;
		private boolean initiatedFirst;
		private boolean initiatedSecond;
		private boolean initiatedThird;

		private WelcomePagerAdapter() {
			screenWidth = (int) (652 * density);
			imageHeight = (int) (screenWidth / 1.98f);
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
						firstView = (RelativeLayout) inflater.inflate(R.layout.welcome_frame_1, container, false);

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
				closeYouTubeBtn1.setOnClickListener(WelcomeTourFragmentTablet.this);

				int orientation = getResources().getConfiguration().orientation; // auto-init for fullscreen
				if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
					Fragment fragmentByTag = getFragmentManager().findFragmentByTag(YOUTUBE_FRAGMENT_TAG + R.id.youTubeFrameContainer1);
					if (fragmentByTag != null) {
						youTubePlayerFragment1 = initYoutubeFragment(R.id.youTubeFrameContainer1, (YouTubePlayerSupportFragment) fragmentByTag);
					}
				}

				if (!initiatedFirst) {
					// add ImageView back
					ImageView imageView = new ImageView(getContext());
					imageView.setAdjustViewBounds(true);
					imageView.setId(R.id.firstWelcomeImg);
					imageView.setScaleType(ImageView.ScaleType.FIT_XY);
					imageView.setImageResource(R.drawable.img_welcome_back_1);

					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
					params.addRule(RelativeLayout.CENTER_IN_PARENT);
					firstView.addView(imageView, 0, params);


					firstView.findViewById(R.id.playBtn1).setOnClickListener(WelcomeTourFragmentTablet.this);

					initiatedFirst = true;
				}
				break;
				case 1:
					if (secondView == null) {
						secondView = (RelativeLayout) inflater.inflate(R.layout.welcome_frame_2, container, false);
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
				closeYouTubeBtn2.setOnClickListener(WelcomeTourFragmentTablet.this);

				orientation = getResources().getConfiguration().orientation; // auto-init for fullscreen
				if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
					Fragment fragmentByTag = getFragmentManager().findFragmentByTag(YOUTUBE_FRAGMENT_TAG + R.id.youTubeFrameContainer2);
					if (fragmentByTag != null) {
						youTubePlayerFragment2 = initYoutubeFragment(R.id.youTubeFrameContainer2, (YouTubePlayerSupportFragment) fragmentByTag);
					}
				}

				if (!initiatedSecond) {
					ImageView imageView = new ImageView(getContext());
					imageView.setAdjustViewBounds(true);
					imageView.setId(R.id.secondWelcomeImg);
					imageView.setScaleType(ImageView.ScaleType.FIT_XY);
					imageView.setImageResource(R.drawable.img_welcome_back_2);

					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
					params.addRule(RelativeLayout.CENTER_IN_PARENT);
					secondView.addView(imageView, 0, params);

					secondView.findViewById(R.id.playBtn2).setOnClickListener(WelcomeTourFragmentTablet.this);

					initiatedSecond = true;
				}
				break;
				case 2:
					if (thirdView == null) {
						thirdView = (RelativeLayout) inflater.inflate(R.layout.welcome_frame_3, container, false);
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
				closeYouTubeBtn3.setOnClickListener(WelcomeTourFragmentTablet.this);

				orientation = getResources().getConfiguration().orientation; // auto-init for fullscreen
				if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
					Fragment fragmentByTag = getFragmentManager().findFragmentByTag(YOUTUBE_FRAGMENT_TAG + R.id.youTubeFrameContainer3);
					if (fragmentByTag != null) {
						youTubePlayerFragment3 = initYoutubeFragment(R.id.youTubeFrameContainer3, (YouTubePlayerSupportFragment) fragmentByTag);
					}
				}

				if (!initiatedThird) {
					ImageView imageView = new ImageView(getContext());
					imageView.setAdjustViewBounds(true);
					imageView.setId(R.id.thirdWelcomeImg);
					imageView.setScaleType(ImageView.ScaleType.FIT_XY);
					imageView.setImageResource(R.drawable.img_welcome_back_3);

					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
					params.addRule(RelativeLayout.CENTER_IN_PARENT);

					thirdView.addView(imageView, 0, params);

					thirdView.findViewById(R.id.playBtn3).setOnClickListener(WelcomeTourFragmentTablet.this);

					initiatedThird = true;
				}
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

}
