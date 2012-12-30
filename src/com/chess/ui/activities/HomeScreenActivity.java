package com.chess.ui.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccChallengeTaskRunner;
import com.chess.lcc.android.OuterChallengeListener;
import com.chess.live.client.Challenge;
import com.chess.live.util.GameTimeConfig;
import com.chess.model.PopupItem;
import com.chess.ui.popup_fragments.PopupDialogFragment;
import com.chess.utilities.AppUtils;
import com.chess.utilities.InneractiveAdHelper;
import com.flurry.android.FlurryAgent;
import com.inneractive.api.ads.InneractiveAd;
import com.mopub.mobileads.AdView;
import com.mopub.mobileads.MoPubInterstitial;

import java.util.HashMap;
import java.util.Map;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home_screen);
		AppUtils.setBackground(findViewById(R.id.mainView), this);

		findViewById(R.id.playLiveFrame).setOnClickListener(this);
		findViewById(R.id.playOnlineFrame).setOnClickListener(this);
		findViewById(R.id.playComputerFrame).setOnClickListener(this);
		findViewById(R.id.tacticsTrainerFrame).setOnClickListener(this);
		findViewById(R.id.videoLessonsFrame).setOnClickListener(this);
		findViewById(R.id.settingsFrame).setOnClickListener(this);

		// set listener to lccHolder
		getLccHolder().setOuterChallengeListener(new LiveOuterChallengeListener());

		challengeTaskRunner = new LccChallengeTaskRunner(new ChallengeTaskListener());

		registerGcmService();
	}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if(extras != null){
            int cmd = extras.getInt(StaticData.NAVIGATION_CMD);
            if(cmd == StaticData.NAV_FINISH_2_LOGIN){
                Intent loginIntent = new Intent(this, LoginScreenActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
                finish();
                extras.clear();
            } else if(cmd == StaticData.NAV_FINISH_2_SPLASH){
				Intent loginIntent = new Intent(this, SplashActivity.class);
				loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(loginIntent);
				finish();
				extras.clear();
			}
        }
    }

    @Override
	protected void onResume() {
		super.onResume();

		showFullScreenAd();

		adjustActionBar();
	}


	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(LOGOUT_TAG)) {
			getLccHolder().logout();
			getActionBarHelper().showMenuItemById(R.id.menu_singOut, getLccHolder().isConnected());
		} else if (tag.equals(CHALLENGE_TAG)) {
			Log.i(TAG, "Accept challenge: " + currentChallenge);
            challengeTaskRunner.runAcceptChallengeTask(currentChallenge);
			challengeTaskRunner.declineAllChallenges(currentChallenge, getLccHolder().getChallenges());
		}
		super.onPositiveBtnClick(fragment);
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onNegativeBtnClick(fragment);
			return;
		}

		if (tag.equals(CHALLENGE_TAG)) {
			fragment.dismiss();

			// todo: refactor with new LCC
			if(!getLccHolder().isConnected() || getLccHolder().getClient() == null){ // TODO should leave that screen on connection lost or when LCC is become null
				getLccHolder().logout();
				backToHomeActivity();
				return;
			}

			Log.i(TAG, "Decline challenge: " + currentChallenge);
			challengeTaskRunner.declineCurrentChallenge(currentChallenge, getLccHolder().getChallenges());
        }
		super.onNegativeBtnClick(fragment);
	}

	private void adjustActionBar(){
		getActionBarHelper().showMenuItemById(R.id.menu_singOut, getLccHolder().isConnected());
		getActionBarHelper().showMenuItemById(R.id.menu_search, false);
		getActionBarHelper().showMenuItemById(R.id.menu_settings, false);
		getActionBarHelper().showMenuItemById(R.id.menu_new_game, false);
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
		getActionBarHelper().showMenuItemById(R.id.menu_new_game, false, menu);
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
		if (!preferences.getBoolean(AppConstants.FULLSCREEN_AD_ALREADY_SHOWED, false) && AppUtils.isNeedToUpgrade(this)) {

			// TODO handle for support show ad on tablet in portrait mode
			// TODO: add support for tablet ad units

			if (InneractiveAdHelper.IS_SHOW_FULLSCREEN_ADS) {

				// todo: use special viewgroup for fullscreen ad
				// todo: test cases when ad is loaded after onPause etc
				InneractiveAd.displayInterstitialAd(this, (LinearLayout) findViewById(R.id.mainView),
						InneractiveAdHelper.FULLSCREEN_APP_ID,
						new InneractiveAdHelper.InneractiveAdListenerImpl(InneractiveAd.IaAdType.Interstitial, preferencesEditor));

			} else {
				moPubInterstitial = new MoPubInterstitial(this, "agltb3B1Yi1pbmNyDQsSBFNpdGUYwLyBEww"); // chess.com // TODO move to constants
				//moPubInterstitial = new MoPubInterstitial(this, "12345"); // test
				//moPubInterstitial = new MoPubInterstitial(this, "agltb3B1Yi1pbmNyDAsSBFNpdGUYsckMDA"); // test
				moPubInterstitial.setListener(this);
				moPubInterstitial.load();
			}

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
	public void onClick(View view) {
		if (view.getId() == R.id.playLiveFrame) {
			Class<?> clazz = AppData.isGuest(this) ? SignUpScreenActivity.class : LiveScreenActivity.class;
			startAnimatedActivity(view, clazz);

		} else if (view.getId() == R.id.playOnlineFrame) {
			Class<?> clazz = AppData.isGuest(this) ? SignUpScreenActivity.class : OnlineScreenActivity.class;
			startAnimatedActivity(view, clazz);

		} else if (view.getId() == R.id.playComputerFrame) {
			startAnimatedActivity(view, ComputerScreenActivity.class);

		} else if (view.getId() == R.id.tacticsTrainerFrame) {
			startAnimatedActivity(view, GameTacticsScreenActivity.class);

		} else if (view.getId() == R.id.videoLessonsFrame) {
			startAnimatedActivity(view, VideoScreenActivity.class);

		} else if (view.getId() == R.id.settingsFrame) {
			startAnimatedActivity(view, PreferencesScreenActivity.class);
		}
	}

	private void startAnimatedActivity(View view, Class clazz) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			// Create a scale-up animation that originates at the button
			// being pressed.
//			ActivityOptions opts = ActivityOptions.makeCustomAnimation(
//					this, R.anim.hyperspace_out, R.anim.hyperspace_in);
//			// Request the activity be started, using the custom animation options.
//			startActivity(new Intent(MainActivity.this, AnimationActivity.class),
//					opts.toBundle());

			ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0,
					view.getWidth()/2, view.getHeight()/2);
			// Request the activity be started, using the custom animation options.
			startActivity(new Intent(this, clazz), opts.toBundle());

		} else {
			startActivity(new Intent(this, clazz));
		}
	}

	public void OnInterstitialLoaded() {
		if (moPubInterstitial.isReady()) {
			Log.d(AdView.MOPUB, "interstitial ad listener: loaded and ready");
			moPubInterstitial.show();

			preferencesEditor.putBoolean(AppConstants.FULLSCREEN_AD_ALREADY_SHOWED, true);
			preferencesEditor.commit();
		}
		else {
			Log.d(AdView.MOPUB, "interstitial ad listener: loaded, but not ready");
		}

		String response = moPubInterstitial.getMoPubInterstitialView().getResponseString();
		if (response != null && response.contains(AppConstants.MATOMY_AD)) {
			Map<String, String> params = new HashMap<String, String>();
			params.put(AppConstants.RESPONSE, response);
			FlurryAgent.logEvent(FlurryData.MATOMY_AD_FULLSCREEN_LOADED, params);
		}
	}

	public void OnInterstitialFailed() {
		Log.d(AdView.MOPUB, "interstitial ad listener: failed");

		String response = moPubInterstitial.getMoPubInterstitialView().getResponseString();
		if (response != null && response.contains(AppConstants.MATOMY_AD)) {
			Map<String, String> params = new HashMap<String, String>();
			params.put(AppConstants.RESPONSE, response);
			FlurryAgent.logEvent(FlurryData.MATOMY_AD_FULLSCREEN_FAILED, params);
		}
	}
}