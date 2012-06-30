package com.chess.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import com.chess.R;
import com.chess.backend.statics.AppData;
import com.chess.utilities.MopubHelper;
import com.mopub.mobileads.MoPubView;

public class LiveNewGameActivity extends LiveBaseActivity  {

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

		findViewById(R.id.friendchallenge).setOnClickListener(this);
		findViewById(R.id.challengecreate).setOnClickListener(this);

		currentGame = (Button) findViewById(R.id.currentGame);
		currentGame.setOnClickListener(this);
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
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		getLccHolder().logout();
		backToHomeActivity();
	}

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
