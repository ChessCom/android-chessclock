package com.chess.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.chess.R;
import com.chess.backend.statics.AppData;
import com.chess.utilities.MopubHelper;
import com.mopub.mobileads.MoPubView;

public class LiveNewGameActivity2 extends LiveBaseActivity2 implements OnClickListener {

	private Button currentGame;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_new_game);

		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		moPubView = (MoPubView) findViewById(R.id.mopub_adview); // init anyway as it is declared in layout
		if (MopubHelper.isShowAds(this)) {
			MopubHelper.showBannerAd(upgradeBtn, moPubView, this);
		}

		/*if (MobclixHelper.isShowAds(mainApp)) {
			if (MobclixHelper.getBannerAdviewWrapper(mainApp) == null || MobclixHelper.getBannerAdview(mainApp) == null) {
				MobclixHelper.initializeBannerAdView(this, mainApp);
			}
		}*/
		init();

		findViewById(R.id.friendchallenge).setOnClickListener(this);
		findViewById(R.id.challengecreate).setOnClickListener(this);

		currentGame = (Button) findViewById(R.id.currentGame);
		currentGame.setOnClickListener(this);
	}

	private void init() {
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (getLccHolder().getCurrentGameId() == null) {
			currentGame.setVisibility(View.GONE);
		} else {
			currentGame.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onPause() {
		/*if (MobclixHelper.isShowAds(mainApp)) {
			MobclixHelper.pauseAdview(MobclixHelper.getBannerAdview(mainApp), mainApp);
		}*/
		super.onPause();
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		getLccHolder().logout();
		backToHomeActivity();
	}

	/*@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		System.out.println("LCCLOG: onWindowFocusChanged hasFocus " + hasFocus);
		if (hasFocus && MobclixHelper.isShowAds(mainApp) && mainApp.isForceBannerAdOnFailedLoad()) {
			MobclixHelper.showBannerAd(upgradeBtn, this, mainApp);
		}
	}*/

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(AppData.getMembershipAndroidIntent(this));

		} else if (view.getId() == R.id.friendchallenge) {
			startActivity(new Intent(this, LiveFriendChallengeActivity.class));
		} else if (view.getId() == R.id.challengecreate) {
			startActivity(new Intent(this, LiveCreateChallengeActivity.class));
		} else if (view.getId() == R.id.currentGame) {
			getLccHolder().checkAndProcessFullGame();
		}
	}
}
