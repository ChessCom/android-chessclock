package com.chess.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.lcc.android.OuterChallengeListener;
import com.chess.live.client.Challenge;
import com.chess.live.util.GameTimeConfig;
import com.chess.model.PopupItem;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.CoreActivityHome;
import com.chess.ui.fragments.PopupDialogFragment;
import com.chess.utilities.CommonUtils;
import com.chess.utilities.MopubHelper;
import com.mopub.mobileads.MoPubInterstitial;

/**
 * HomeScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 6:29
 */
public class HomeScreenActivity extends CoreActivityHome implements View.OnClickListener,
		MoPubInterstitial.MoPubInterstitialListener {
	private MoPubInterstitial moPubInterstitial;

	protected static final String CHALLENGE_TAG = "challenge_tag";
	protected static final String LOGOUT_TAG = "logout_tag";

	protected LiveOuterChallengeListener outerChallengeListener;
	protected Challenge currentChallenge;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home_screen);
		CommonUtils.setBackground(findViewById(R.id.mainView), this);

		Bundle extras = getIntent().getExtras();
		if(extras != null){
			int cmd = extras.getInt(StaticData.NAVIGATION_CMD);
			if(cmd == StaticData.NAV_FINISH_2_LOGIN){
				Log.d("TEST","launching login activity from home");
				Intent intent = new Intent(this, LoginScreenActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				extras.clear();
			}
		}

		findViewById(R.id.playLiveFrame).setOnClickListener(this);
		findViewById(R.id.playOnlineFrame).setOnClickListener(this);
		findViewById(R.id.playComputerFrame).setOnClickListener(this);
		findViewById(R.id.tacticsTrainerFrame).setOnClickListener(this);
		findViewById(R.id.videoLessonsFrame).setOnClickListener(this);
		findViewById(R.id.settingsFrame).setOnClickListener(this);



		// set listener to lccHolder
		outerChallengeListener = new LiveOuterChallengeListener();
		lccHolder.setOuterChallengeListener(outerChallengeListener);
	}

	@Override
	public void update(int code) {
	}

	@Override
	protected void onResume() {
		getActionBarHelper().hideMenuItemById(R.id.menu_singOut, lccHolder.isConnected());

		if (MopubHelper.isShowAds(mainApp)) {
			showFullScreenAd();
		}

		/*if(interAdView == null)*/
		/*interAdView = new MMAdView(this, "77015", MMAdView.FULLSCREEN_AD_TRANSITION, true, null);
		interAdView.setId(MMAdViewSDK.DEFAULT_VIEWID);
		interAdView.callForAd();
		interAdView.setListener(new MMAdView.MMAdListener() {

			public void MMAdReturned(MMAdView mmAdView) {
				if (mmAdView.check()) {
					mmAdView.display();
				}
			}

			public void MMAdFailed(MMAdView mmAdView) {
			}

			public void MMAdClickedToNewBrowser(MMAdView mmAdView) {
			}

			public void MMAdClickedToOverlay(MMAdView mmAdView) {
			}

			public void MMAdOverlayLaunched(MMAdView mmAdView) {
			}

			public void MMAdRequestIsCaching(MMAdView mmAdView) {
			}

			public void MMAdCachingCompleted(MMAdView mmAdView, boolean success) {
				if (success && mmAdView.check()) {
					mmAdView.display();
				}
			}
		});*/

		super.onResume();
	}


	@Override
	public void onLeftBtnClick(PopupDialogFragment fragment) {
		if(fragment.getTag().equals(LOGOUT_TAG)){
			lccHolder.logout();
			getActionBarHelper().hideMenuItemById(R.id.menu_singOut, lccHolder.isConnected());
		}else if(fragment.getTag().equals(CHALLENGE_TAG)){
			LccHolder.LOG.info("Accept challenge: " + currentChallenge);
			lccHolder.getAndroid().runAcceptChallengeTask(currentChallenge);
			lccHolder.declineAllChallenges(currentChallenge);
		}
		fragment.getDialog().dismiss();
	}

	@Override
	public void onRightBtnClick(PopupDialogFragment fragment) {// Challenge declined!
		if (fragment.getTag().equals(CHALLENGE_TAG)) {
			LccHolder.LOG.info("Decline challenge: " + currentChallenge);
//			lccHolder.getAndroid().runRejectChallengeTask(currentChallenge);
			lccHolder.declineCurrentChallenge(currentChallenge);
		}
		fragment.getDialog().dismiss();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.sign_out, menu);
		getActionBarHelper().hideMenuItemById(R.id.menu_singOut, lccHolder.isConnected(), menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_singOut:
				PopupItem popupItem = new PopupItem();
				popupItem.setTitle(R.string.confirm);
				popupItem.setMessage(R.string.signout_confirm);

//				popupDialogFragment.updatePopupItem(popupItem);
				PopupDialogFragment popupDialogFragment = PopupDialogFragment.newInstance(popupItem, this);
				popupDialogFragment.show(getSupportFragmentManager(), LOGOUT_TAG);
//				popupManager.add(popupDialogFragment);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class LiveOuterChallengeListener implements OuterChallengeListener {
		@Override
		public void showDelayedDialog(Challenge challenge) {
			currentChallenge = challenge;
			PopupItem popupItem = new PopupItem();
			popupItem.setTitle(R.string.you_been_challenged);
			popupItem.setMessage(composeMessage(challenge));
			popupItem.setRightBtnId(R.string.decline);
			popupItem.setLeftBtnId(R.string.accept);

			PopupDialogFragment popupDialogFragment = PopupDialogFragment.newInstance(popupItem, HomeScreenActivity.this);
//			popupDialogFragment.updatePopupItem(popupItem);
			popupDialogFragment.show(getSupportFragmentManager(), CHALLENGE_TAG);
		}

		@Override
		public void showDialog(Challenge challenge) {
			if(popupDialogFragment.getDialog() != null && popupDialogFragment.getDialog().isShowing()){
				return;
			}

			currentChallenge = challenge;
//			PopupItem popupItem = new PopupItem();
			popupItem.setTitle(R.string.you_been_challenged);
			popupItem.setMessage(composeMessage(challenge));
			popupItem.setRightBtnId(R.string.decline);
			popupItem.setLeftBtnId(R.string.accept);

//			PopupDialogFragment popupFragment = PopupDialogFragment.newInstance(popupItem, LiveBaseActivity.this);
			popupDialogFragment.updatePopupItem(popupItem);
			popupDialogFragment.show(getSupportFragmentManager(), CHALLENGE_TAG);
		}

		@Override
		public void hidePopups() {
			dismissAllPopups();
		}

		private String composeMessage(Challenge challenge){
			String rated = challenge.isRated()? getString(R.string.rated): getString(R.string.unrated);
			GameTimeConfig config = challenge.getGameTimeConfig();
			String blitz = "";
			if(config.isBlitz()){
				blitz = "(Blitz)";
			}else if(config.isLightning()){
				blitz = "(Lightning)";
			}else if(config.isStandard()){
				blitz = "(Standard)";
			}

			String timeIncrement = "";

			if(config.getTimeIncrement() > 0){
				timeIncrement = " | "+ String.valueOf(config.getTimeIncrement()/10);
			}

			String timeMode = config.getBaseTime()/10/60 + timeIncrement + AppConstants.SYMBOL_SPACE + blitz;
			String playerColor;

			switch (challenge.getColor()) {
				case UNDEFINED:
					playerColor = getString(R.string.random);
					break;
				case WHITE:
					playerColor = getString(R.string.black);
					break;
				case BLACK:
					playerColor = getString(R.string.white);
					break;
				default:
					playerColor = getString(R.string.random);
					break;
			}

			return new StringBuilder()
					.append(getString(R.string.opponent_)).append(AppConstants.SYMBOL_SPACE)
					.append(challenge.getFrom().getUsername()).append(AppConstants.SYMBOL_NEW_STR)
					.append(getString(R.string.time_)).append(AppConstants.SYMBOL_SPACE)
					.append(timeMode).append(AppConstants.SYMBOL_NEW_STR)
					.append(getString(R.string.you_play)).append(AppConstants.SYMBOL_SPACE)
					.append(playerColor).append(AppConstants.SYMBOL_NEW_STR)
					.append(rated)
					.toString();
		}
	}
	/*private class MobFullScreeListener implements MobclixFullScreenAdViewListener {

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
	}*/

	private void showFullScreenAd() {
		if (!mainApp.getSharedData().getBoolean(AppConstants.FULLSCREEN_AD_ALREADY_SHOWED, false)
				&& MopubHelper.isShowAds(mainApp)) {

			// TODO handle for support show ad on tablet in portrait mode
			// TODO: add support for tablet ad units
			moPubInterstitial = new MoPubInterstitial(this, "agltb3B1Yi1pbmNyDQsSBFNpdGUYwLyBEww"); // chess.com
			//moPubInterstitial = new MoPubInterstitial(this, "12345"); // test
			//moPubInterstitial = new MoPubInterstitial(this, "agltb3B1Yi1pbmNyDAsSBFNpdGUYsckMDA"); // test
			moPubInterstitial.setListener(this);
			moPubInterstitial.load();

			/*MobclixFullScreenAdView fsAdView = new MobclixFullScreenAdView(this);
			fsAdView.addMobclixAdViewListener(mobFullScreeListener);
			fsAdView.requestAndDisplayAd();*/
		}
	}

	protected void onDestroy() {
		if (moPubInterstitial != null) {
			moPubInterstitial.destroy();
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.playLiveFrame) {
			Class<?> clazz = mainApp.guest ? SignUpScreenActivity.class : LiveScreenActivity.class;
			startActivity(new Intent(context, clazz));

		} else if (v.getId() == R.id.playOnlineFrame) {
			Class<?> clazz = mainApp.guest ? SignUpScreenActivity.class : OnlineScreenActivity.class;
			startActivity(new Intent(context, clazz));

		} else if (v.getId() == R.id.playComputerFrame) {
			startActivity(new Intent(context, ComputerScreenActivity.class));

		} else if (v.getId() == R.id.tacticsTrainerFrame) {
			Intent intent = new Intent(context, GameTacticsScreenActivity.class);
			intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_TACTICS);
//			intent.putExtra(AppConstants.LIVE_CHESS, false); // TODO remove
			startActivity(intent);
//			startActivity(new Intent(context, GameTacticsScreenActivity.class));

		} else if (v.getId() == R.id.videoLessonsFrame) {
			startActivity(new Intent(context, VideoScreenActivity.class));

		} else if (v.getId() == R.id.settingsFrame) {
			startActivity(new Intent(context, PreferencesScreenActivity.class));
		}
	}

	public void OnInterstitialLoaded() {
		if (moPubInterstitial.isReady()) {
			Log.d("HOME", "mopub interstitial ad listener: loaded and ready");
			moPubInterstitial.show();
			// TODO: UNCOMMENT
			mainApp.getSharedDataEditor().putBoolean(AppConstants.FULLSCREEN_AD_ALREADY_SHOWED, true);
			mainApp.getSharedDataEditor().commit();
		}
		else {
			Log.d("HOME", "mopub interstitial ad listener: loaded, but not ready");
		}
	}

	public void OnInterstitialFailed() {
		Log.d("HOME", "mopub interstitial ad listener: failed");
	}
}