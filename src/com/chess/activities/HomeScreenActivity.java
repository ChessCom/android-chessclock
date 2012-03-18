package com.chess.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.chess.R;
import com.chess.core.AppConstants;
import com.chess.core.CoreActivityHome;
import com.chess.utilities.CommonUtils;
import com.chess.utilities.MobclixHelper;
import com.mobclix.android.sdk.MobclixFullScreenAdView;
import com.mobclix.android.sdk.MobclixFullScreenAdViewListener;

/**
 * HomeScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 6:29
 */
public class HomeScreenActivity extends CoreActivityHome implements View.OnClickListener {

	private MobFullScreeListener mobFullScreeListener;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home_screen);

		CommonUtils.setBackground(findViewById(R.id.mainView), this);

		findViewById(R.id.playLiveFrame).setOnClickListener(this);
		findViewById(R.id.playOnlineFrame).setOnClickListener(this);
		findViewById(R.id.playComputerFrame).setOnClickListener(this);
		findViewById(R.id.tacticsTrainerFrame).setOnClickListener(this);
		findViewById(R.id.videoLessonsFrame).setOnClickListener(this);
		findViewById(R.id.settingsFrame).setOnClickListener(this);

		mobFullScreeListener = new MobFullScreeListener();
	}

	@Override
	public void Update(int code) {
	}

	@Override
	protected void onResume() {
		if (MobclixHelper.isShowAds(mainApp)) {
			showFullScreenAd();
		}
		super.onResume();
	}

	private class MobFullScreeListener implements MobclixFullScreenAdViewListener {

		@Override
		public String query() {
			return null;
		}

		@Override
		public void onPresentAd(MobclixFullScreenAdView arg0) {
			System.out.println("mobclix fullscreen onPresentAd");

		}

		@Override
		public void onFinishLoad(MobclixFullScreenAdView arg0) {
			System.out.println("mobclix fullscreen onFinishLoad");

		}

		@Override
		public void onFailedLoad(MobclixFullScreenAdView adView, int errorCode) {
			System.out.println("mobclix fullscreen onFailedLoad errorCode=" + errorCode);
		}

		@Override
		public void onDismissAd(MobclixFullScreenAdView arg0) {
			System.out.println("mobclix fullscreen onDismissAd");
		}

		@Override
		public String keywords() {
			return null;
		}
	}

	private void showFullScreenAd() {
		if (!mainApp.getSharedData().getBoolean(AppConstants.FULLSCREEN_AD_ALREADY_SHOWED, false)
				&& MobclixHelper.isShowAds(mainApp)) {
			// TODO handle for support show ad on tablet in portrait mode
//			MobclixFullScreenAdView fsAdView = new MobclixFullScreenAdView(this);
//			fsAdView.addMobclixAdViewListener(mobFullScreeListener);
//			fsAdView.requestAndDisplayAd();

			// MoPubInterstitial interstitial = new MoPubInterstitial(this,
// "agltb3B1Yi1pbmNyDQsSBFNpdGUYioOrAgw");
			/*
			 * MoPubInterstitial interstitial = new MoPubInterstitial(this,
			 * "agltb3B1Yi1pbmNyDAsSBFNpdGUYsckMDA"); // test
			 * interstitial.showAd();
			 */
			mainApp.getSharedDataEditor().putBoolean(AppConstants.FULLSCREEN_AD_ALREADY_SHOWED, true);
			mainApp.getSharedDataEditor().commit();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.playLiveFrame) {
			Class<?> clazz = mainApp.guest ? SignUpScreenActivity.class
					: LiveScreenActivity.class;
			Intent intent = new Intent(context, clazz);
			intent.putExtra(AppConstants.LIVE_CHESS, true);
			startActivity(intent);

		} else if (v.getId() == R.id.playOnlineFrame) {
			Class<?> clazz = mainApp.guest ? SignUpScreenActivity.class
					: OnlineScreenActivity.class;
			Intent intent = new Intent(context, clazz);
			intent.putExtra(AppConstants.LIVE_CHESS, false);
			startActivity(intent);

		} else if (v.getId() == R.id.playComputerFrame) {
			startActivity(new Intent(context, ComputerScreenActivity.class));

		} else if (v.getId() == R.id.tacticsTrainerFrame) {
			Intent intent = new Intent(context, GameTacticsScreenActivity.class);
			intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_TACTICS);
			intent.putExtra(AppConstants.LIVE_CHESS, false);
			startActivity(intent);

		} else if (v.getId() == R.id.videoLessonsFrame) {
			startActivity(new Intent(context, VideoScreenActivity.class));

		} else if (v.getId() == R.id.settingsFrame) {
			startActivity(new Intent(context, PreferencesScreenActivity.class));
//			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST + "/login.html?als="
//					+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&goto=http%3A%2F%2Fwww." + LccHolder.HOST
//					+ "%2Fmembership.html?c=androidads")));
//		} else if (v.getId() == R.id.logOutFrame) {
		}
	}


}