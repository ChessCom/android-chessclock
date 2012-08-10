package com.chess.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.chess.R;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccChallengeTaskRunner;
import com.chess.lcc.android.OuterChallengeListener;
import com.chess.live.client.Challenge;
import com.chess.live.util.GameTimeConfig;
import com.chess.model.PopupItem;
import com.chess.ui.fragments.PopupDialogFragment;
import com.chess.utilities.AppUtils;
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

	private static final String TAG = "HomeScreenActivity";

	protected static final String CHALLENGE_TAG = "challenge_tag";
	protected static final String LOGOUT_TAG = "logout_tag";

	protected MoPubInterstitial moPubInterstitial;

	protected Challenge currentChallenge;
    private LccChallengeTaskRunner challengeTaskRunner;
    private ChallengeTaskListener challengeTaskListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home_screen);
		AppUtils.setBackground(findViewById(R.id.mainView), this);

		Bundle extras = getIntent().getExtras();
		if(extras != null){
			int cmd = extras.getInt(StaticData.NAVIGATION_CMD);
			if(cmd == StaticData.NAV_FINISH_2_LOGIN){
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
		getLccHolder().setOuterChallengeListener(new LiveOuterChallengeListener());

		challengeTaskListener = new ChallengeTaskListener();
		challengeTaskRunner = new LccChallengeTaskRunner(challengeTaskListener);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (MopubHelper.isShowAds(this)) {
			showFullScreenAd();
		}

		adjustActionBar();
	}


	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if (fragment.getTag().equals(LOGOUT_TAG)) {
			getLccHolder().logout();
			getActionBarHelper().showMenuItemById(R.id.menu_singOut, getLccHolder().isConnected());
		}else if(fragment.getTag().equals(CHALLENGE_TAG)){
			Log.i(TAG, "Accept challenge: " + currentChallenge);
            challengeTaskRunner.runAcceptChallengeTask(currentChallenge);
			challengeTaskRunner.declineAllChallenges(currentChallenge, getLccHolder().getChallenges());
		}
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {// Challenge declined!
		if (fragment.getTag().equals(CHALLENGE_TAG)) {
			Log.i(TAG, "Decline challenge: " + currentChallenge);
            fragment.dismiss();
            challengeTaskRunner.declineCurrentChallenge(currentChallenge, getLccHolder().getChallenges());
        }else
            fragment.dismiss();
	}

	private void adjustActionBar(){
		getActionBarHelper().showMenuItemById(R.id.menu_singOut, getLccHolder().isConnected());
		getActionBarHelper().showMenuItemById(R.id.menu_search, false);
		getActionBarHelper().showMenuItemById(R.id.menu_settings, false);
//		getActionBarHelper().showMenuItemById(R.id.menu_new_game, false);
		getActionBarHelper().showMenuItemById(R.id.menu_refresh, false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.sign_out, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_singOut, getLccHolder().isConnected(), menu);
		getActionBarHelper().showMenuItemById(R.id.menu_search, false, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_settings, false, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_refresh, false, menu);
//		getActionBarHelper().showMenuItemById(R.id.menu_new_game, false, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_singOut:
				showPopupDialog(R.string.confirm, R.string.signout_confirm, LOGOUT_TAG);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

    private class ChallengeTaskListener extends AbstractUpdateListener<Challenge> {
        public ChallengeTaskListener() {
            super(getContext());
        }
    }

	private class LiveOuterChallengeListener implements OuterChallengeListener {
		@Override
		public void showDelayedDialog(Challenge challenge) {
			currentChallenge = challenge;

			popupItem.setPositiveBtnId(R.string.accept);
			popupItem.setNegativeBtnId(R.string.decline);
			showPopupDialog(R.string.you_been_challenged, composeMessage(challenge), CHALLENGE_TAG);
		}

		@Override
		public void showDialog(Challenge challenge) {
			if (popupManager.size() > 0) {
				return;
			}

			currentChallenge = challenge;

			PopupItem popupItem = new PopupItem();
			popupItem.setTitle(R.string.you_been_challenged);
			popupItem.setMessage(composeMessage(challenge));
			popupItem.setNegativeBtnId(R.string.decline);
			popupItem.setPositiveBtnId(R.string.accept);

			PopupDialogFragment popupDialogFragment = PopupDialogFragment.newInstance(popupItem);
			popupDialogFragment.show(getSupportFragmentManager(), CHALLENGE_TAG);

			popupManager.add(popupDialogFragment);
		}

		@Override
		public void hidePopups() {
			dismissAllPopups();
		}

		private String composeMessage(Challenge challenge){
			String rated = challenge.isRated()? getString(R.string.rated): getString(R.string.unrated);
			GameTimeConfig config = challenge.getGameTimeConfig();
			String blitz = StaticData.SYMBOL_EMPTY;
			if(config.isBlitz()){
				blitz = getString(R.string.blitz_game);
			}else if(config.isLightning()){
				blitz = getString(R.string.lightning_game);
			}else if(config.isStandard()){
				blitz = getString(R.string.standard_game);
			}

			String timeIncrement = StaticData.SYMBOL_EMPTY;

			if(config.getTimeIncrement() > 0){
				timeIncrement = " | "+ String.valueOf(config.getTimeIncrement()/10);
			}

			String timeMode = config.getBaseTime()/10/60 + timeIncrement + StaticData.SYMBOL_SPACE + blitz;
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
					.append(getString(R.string.opponent_)).append(StaticData.SYMBOL_SPACE)
					.append(challenge.getFrom().getUsername())
					.append(getString(R.string.time_)).append(StaticData.SYMBOL_SPACE)
					.append(timeMode)
					.append(getString(R.string.you_play)).append(StaticData.SYMBOL_SPACE)
					.append(playerColor).append(StaticData.SYMBOL_NEW_STR)
					.append(rated)
					.toString();
		}
	}


	private void showFullScreenAd() {
		if (!preferences.getBoolean(AppConstants.FULLSCREEN_AD_ALREADY_SHOWED, false)
				&& MopubHelper.isShowAds(this)) {

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
			// TODO check internet connection before connect
			Class<?> clazz = DataHolder.getInstance().isGuest() ? SignUpScreenActivity.class : LiveScreenActivity.class;
			startActivity(new Intent(this, clazz));

		} else if (v.getId() == R.id.playOnlineFrame) {
			// TODO check internet connection before connect
			Class<?> clazz = DataHolder.getInstance().isGuest() ? SignUpScreenActivity.class : OnlineScreenActivity.class;
			startActivity(new Intent(this, clazz));

		} else if (v.getId() == R.id.playComputerFrame) {
			startActivity(new Intent(this, ComputerScreenActivity.class));

		} else if (v.getId() == R.id.tacticsTrainerFrame) {
			Intent intent = new Intent(this, GameTacticsScreenActivity.class);
//			intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_TACTICS);
			startActivity(intent);

		} else if (v.getId() == R.id.videoLessonsFrame) {
			startActivity(new Intent(this, VideoScreenActivity.class));

		} else if (v.getId() == R.id.settingsFrame) {
			startActivity(new Intent(this, PreferencesScreenActivity.class));
		}
	}

	public void OnInterstitialLoaded() {
		if (moPubInterstitial.isReady()) {
			Log.d("HOME", "mopub interstitial ad listener: loaded and ready");
			moPubInterstitial.show();

			preferencesEditor.putBoolean(AppConstants.FULLSCREEN_AD_ALREADY_SHOWED, true);
			preferencesEditor.commit();
		}
		else {
			Log.d("HOME", "mopub interstitial ad listener: loaded, but not ready");
		}
	}

	public void OnInterstitialFailed() {
		Log.d("HOME", "mopub interstitial ad listener: failed");
	}
}