package com.chess.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Challenge;
import com.chess.model.GameListItem;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.IntentConstants;
import com.chess.ui.fragments.PopupDialogFragment;
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
//		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);

		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);
		if (MopubHelper.isShowAds(mainApp)) {
			moPubView = (MoPubView) findViewById(R.id.mopub_adview);
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
		registerReceiver(challengesListUpdateReceiver, new IntentFilter(IntentConstants.CHALLENGES_LIST_UPDATE));
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
		unregisterReceiver(challengesListUpdateReceiver);
		super.onPause();
//		enableScreenLock();
	}

	@Override
	public void update(int code) {
		if (code == INIT_ACTIVITY) {
			if (appService != null) {
				if (!mainApp.isLiveChess()) {
					int UPDATE_DELAY = 120000;
					appService.RunRepeatableTask(OnlineScreenActivity.ONLINE_CALLBACK_CODE, 0, UPDATE_DELAY,
							"http://www." + LccHolder.HOST + AppConstants.API_ECHESS_OPEN_INVITES_ID +
									mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY),
							null);
				} else {
					update(OnlineScreenActivity.ONLINE_CALLBACK_CODE);
				}
			}
		} else if (code == OnlineScreenActivity.ONLINE_CALLBACK_CODE) {


		} else if (code == 2) {
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
			startActivity(mainApp.getMembershipAndroidIntent());

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
				lccHolder.getAndroid().runCancelChallengeTask(challenge);
				lccHolder.removeSeek(gameListElement.getGameId());
				update(4);
			} else if (pos == 1) {
				final Challenge challenge = lccHolder.getSeek(gameListElement.getGameId());
				LccHolder.LOG.info("Just keep my seek: " + challenge);
			}
		}
	}


}
