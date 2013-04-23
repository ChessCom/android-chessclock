package com.chess.ui.fragments.sign_in;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
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
 * Date: 23.04.13
 * Time: 7:23
 */
public class WelcomeOneFragment extends CommonLogicFragment implements YouTubePlayer.OnInitializedListener, YouTubePlayer.OnFullscreenListener {

	private static final long ANIMATION_DELAY = 2000;
	private static final long REPEAT_TIMEOUT = 6000;
	private static final int DURATION = 450;
	public static final String YOUTUBE_DEMO_LINK = "AgTQJUhK2MY";
	private static final String YOUTUBE_FRAGMENT_TAG = "youtube fragment";

	private Interpolator accelerator = new AccelerateInterpolator();
	private Interpolator decelerator = new DecelerateInterpolator();

	private ObjectAnimator flipFirstHalf;
	private YouTubePlayerSupportFragment youTubePlayerFragment;
	private View youTubeFrameContainer;
	private boolean youtubeFragmentGoFullScreen;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_welcome_one_main_frame, container, false);
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

		{// add ImageView back
			ViewGroup firstView = (ViewGroup) view.findViewById(R.id.firstView);
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
			youTubeFrameContainer = view.findViewById(R.id.youTubeFrameContainer);

			FragmentManager fragmentManager = getFragmentManager();
			youTubePlayerFragment = (YouTubePlayerSupportFragment) fragmentManager.findFragmentByTag(YOUTUBE_FRAGMENT_TAG);
			if (youTubePlayerFragment == null) {
				youTubePlayerFragment = new YouTubePlayerSupportFragment();
				fragmentManager.beginTransaction()
						.add(R.id.youTubeFrameContainer, youTubePlayerFragment, YOUTUBE_FRAGMENT_TAG)
						.commit();
			}

			youTubePlayerFragment.initialize(AppConstants.YOUTUBE_DEVELOPER_KEY, this);

			youTubeFrameContainer.setVisibility(View.GONE);
		}

		view.findViewById(R.id.playBtn).setOnClickListener(this);

		View whatChessComTxt = view.findViewById(R.id.whatChessComTxt);
		whatChessComTxt.setOnClickListener(this);

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
}
