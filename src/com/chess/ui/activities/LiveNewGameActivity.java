package com.chess.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.chess.R;
import com.chess.backend.statics.AppData;
import com.chess.utilities.AppUtils;
import com.chess.utilities.InneractiveAdHelper;
import com.chess.utilities.MopubHelper;
import com.inneractive.api.ads.InneractiveAd;
import com.mopub.mobileads.MoPubView;

public class LiveNewGameActivity extends LiveBaseActivity  {

	private Button currentGameBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_new_game);

		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		if (AppUtils.isNeedToUpgrade(this)) {
			if (InneractiveAdHelper.IS_SHOW_BANNER_ADS) {
				inneractiveBannerAd = (InneractiveAd) findViewById(R.id.inneractiveBannerAd);
				InneractiveAdHelper.showBannerAd(upgradeBtn, inneractiveBannerAd, this);
			} else {
				//moPubView = (MoPubView) findViewById(R.id.mopub_adview);
				MopubHelper.showBannerAd(upgradeBtn, moPubView, this);
			}
		}

		findViewById(R.id.friendchallenge).setOnClickListener(this);
		findViewById(R.id.challengecreate).setOnClickListener(this);

		currentGameBtn = (Button) findViewById(R.id.currentGameBtn);
		currentGameBtn.setOnClickListener(this);

		AppData.setLiveChessMode(this, true);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (getLccHolder().currentGameExist()) {
			currentGameBtn.setVisibility(View.VISIBLE);
		} else {
			currentGameBtn.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(AppData.getMembershipAndroidIntent(this));
		} else if (view.getId() == R.id.friendchallenge) {
			startActivity(new Intent(this, LiveFriendChallengeActivity.class));
		} else if (view.getId() == R.id.challengecreate) {
			startActivity(new Intent(this, LiveOpenChallengeActivity.class));
		} else if (view.getId() == R.id.currentGameBtn) {
			getLccHolder().checkAndProcessFullGame();
		}
	}
}
