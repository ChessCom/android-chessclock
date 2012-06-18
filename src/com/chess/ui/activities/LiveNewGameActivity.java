package com.chess.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Challenge;
import com.chess.model.GameListItem;
import com.chess.utilities.MopubHelper;
import com.mopub.mobileads.MoPubView;

public class LiveNewGameActivity extends LiveBaseActivity implements OnClickListener {

	private Button currentGame;
	private GameListItem gameListElement;
	private ReleasedByMeDialogListener releasedByMeDialogListener;
	private MoPubView moPubAdView;

	private void init() {
		releasedByMeDialogListener = new ReleasedByMeDialogListener();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_new_game);

		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		moPubView = (MoPubView) findViewById(R.id.mopub_adview); // init anyway as it is declared in layout
		if (MopubHelper.isShowAds(this)) {
			MopubHelper.showBannerAd(upgradeBtn, moPubView, mainApp);
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

	@Override
	protected void onResume() {
		super.onResume();
		if (lccHolder.getCurrentGameId() == null) {
			currentGame.setVisibility(View.GONE);
		} else if (mainApp.isLiveChess()) {
			currentGame.setVisibility(View.VISIBLE);
		}
//		enableScreenLockTimer();
	}

	@Override
	protected void onPause() {
		/*if (MobclixHelper.isShowAds(mainApp)) {
			MobclixHelper.pauseAdview(MobclixHelper.getBannerAdview(mainApp), mainApp);
		}*/
		super.onPause();
	}

	@Override
	public void update(int code) {
		if (code == 2) {
			showToast(R.string.challengeaccepted);
			onPause();
			onResume();
		} else if (code == 3) {
			showToast(R.string.challengedeclined);
			onPause();
			onResume();
		} else if (code == 4) {
			onPause();
			onResume();
		}
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
			if (lccHolder.getCurrentGameId() != null && lccHolder.getGame(lccHolder.getCurrentGameId()) != null) {
				lccHolder.processFullGame(lccHolder.getGame(lccHolder.getCurrentGameId()));
			}
		}
	}


	private class ReleasedByMeDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
				final Challenge challenge = lccHolder.getSeek(gameListElement.getGameId());
				LccHolder.LOG.info("Cancel my seek: " + challenge);
				lccHolder.getAndroidStuff().runCancelChallengeTask(challenge);
				lccHolder.removeSeek(gameListElement.getGameId());
				update(4);
			} else if (pos == 1) {
				final Challenge challenge = lccHolder.getSeek(gameListElement.getGameId());
				LccHolder.LOG.info("Just keep my seek: " + challenge);
			}
		}
	}


}
