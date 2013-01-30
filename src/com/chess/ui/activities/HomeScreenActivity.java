package com.chess.ui.activities;

import actionbarcompat.ActionBarActivityHome;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LiveChessService;
import com.chess.backend.RestHelper;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.CheckUpdateTask;
import com.chess.lcc.android.LccChallengeTaskRunner;
import com.chess.lcc.android.LccHolder;
import com.chess.lcc.android.LiveEvent;
import com.chess.lcc.android.OuterChallengeListener;
import com.chess.lcc.android.interfaces.LiveChessClientEventListenerFace;
import com.chess.live.client.Challenge;
import com.chess.live.util.GameTimeConfig;
import com.chess.model.PopupItem;
import com.chess.ui.fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.PopupDialogFragment;
import com.chess.ui.interfaces.PopupDialogFace;
import com.chess.utilities.AppUtils;
import com.chess.utilities.InneractiveAdHelper;
import com.facebook.android.Facebook;
import com.facebook.android.LoginButton;
import com.flurry.android.FlurryAgent;
import com.inneractive.api.ads.InneractiveAd;
import com.mopub.mobileads.AdView;
import com.mopub.mobileads.MoPubInterstitial;

import java.util.HashMap;
import java.util.Map;

public class HomeScreenActivity extends ActionBarActivityHome implements PopupDialogFace,
		LiveChessClientEventListenerFace, View.OnClickListener, MoPubInterstitial.MoPubInterstitialListener {

	private static final String TAG = "HomeScreenActivity";
	private static final String CONNECT_FAILED_TAG = "connect_failed";
	public static final String OBSOLETE_VERSION_TAG = "obsolete version";
	protected static final String CHALLENGE_TAG = "challenge_tag";
	protected static final String LOGOUT_TAG = "logout_tag";

	protected LccHolder lccHolder;
	private boolean forceFlag;
	protected Challenge currentChallenge;
	private LccChallengeTaskRunner challengeTaskRunner;
	private LiveOuterChallengeListener liveOuterChallengeListener;
	protected ChallengeTaskListener challengeTaskListener;
	protected MoPubInterstitial moPubInterstitial;
	private Menu menu;
	private LiveChessServiceConnectionListener liveChessServiceConnectionListener;
	private boolean isLCSBound;

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

		liveOuterChallengeListener = new LiveOuterChallengeListener();
		challengeTaskListener = new ChallengeTaskListener();
		liveChessServiceConnectionListener = new LiveChessServiceConnectionListener();

		registerGcmService();
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (!isLCSBound) {
			bindService(new Intent(this, LiveChessService.class), liveChessServiceConnectionListener, BIND_AUTO_CREATE);
		}

		if (lccHolder != null) {
			lccHolder.setLiveChessClientEventListener(this);
			lccHolder.setOuterChallengeListener(liveOuterChallengeListener);
			challengeTaskRunner = new LccChallengeTaskRunner(challengeTaskListener, lccHolder);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		long startDay = preferences.getLong(AppConstants.START_DAY, 0);
		Log.d("CheckUpdateTask", "startDay loaded, = " + startDay);

		if (startDay == 0 || !DateUtils.isToday(startDay)) {
			checkUpdate();
		}

		showFullScreenAd();
		adjustActionBar();

		if (lccHolder != null) {
			getActionBarHelper().showMenuItemById(R.id.menu_signOut, lccHolder.isConnected(), menu);
			getActionBarHelper().showMenuItemById(R.id.menu_signOut, lccHolder.isConnected());
			executePausedActivityLiveEvents();
		}
	}

	public void executePausedActivityLiveEvents() {

		Map<LiveEvent.Event, LiveEvent> pausedActivityLiveEvents = lccHolder.getPausedActivityLiveEvents();
		Log.d("LCCLOG", "executePausedActivityLiveEvents size=" + pausedActivityLiveEvents.size() + ", events=" + pausedActivityLiveEvents);

		if (pausedActivityLiveEvents.size() > 0) {
			LiveEvent connectionFailureEvent = pausedActivityLiveEvents.get(LiveEvent.Event.CONNECTION_FAILURE);
			if (connectionFailureEvent != null) {
				pausedActivityLiveEvents.remove(LiveEvent.Event.CONNECTION_FAILURE);
				processConnectionFailure(connectionFailureEvent.getMessage());
			}

			LiveEvent challengeEvent = pausedActivityLiveEvents.get(LiveEvent.Event.CHALLENGE);
			if (challengeEvent != null) {
				pausedActivityLiveEvents.remove(LiveEvent.Event.CHALLENGE);
				if (challengeEvent.isChallengeDelayed()) {
					liveOuterChallengeListener.showDelayedDialog(challengeEvent.getChallenge());
				} else {
					liveOuterChallengeListener.showDialog(challengeEvent.getChallenge());
				}
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		preferencesEditor.putLong(AppConstants.LAST_ACTIVITY_PAUSED_TIME, System.currentTimeMillis());
		preferencesEditor.commit();

		//mainApp.setForceBannerAdOnFailedLoad(false);
	}

	private class LiveChessServiceConnectionListener implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			Log.d("lcclog", "SERVICE: HOME onLiveServiceConnected");

			isLCSBound = true;

			LiveChessService.ServiceBinder serviceBinder = (LiveChessService.ServiceBinder) iBinder;
			lccHolder = serviceBinder.getLccHolder();

			getActionBarHelper().showMenuItemById(R.id.menu_signOut, lccHolder.isConnected(), menu);
			getActionBarHelper().showMenuItemById(R.id.menu_signOut, lccHolder.isConnected());

			lccHolder.setLiveChessClientEventListener(HomeScreenActivity.this);
			lccHolder.setOuterChallengeListener(liveOuterChallengeListener);
			challengeTaskRunner = new  LccChallengeTaskRunner(challengeTaskListener, lccHolder);
			executePausedActivityLiveEvents();

			//showFullScreenAd();

			/*if (shouldBeConnectedToLive) {
				getLccHolder().getService().checkAndConnect();
			}
			isLCSBound = true;*/
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Log.d(TAG, "SERVICE: onServiceDisconnected");
			isLCSBound = false;
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(CONNECT_FAILED_TAG)) {
//			if (DataHolder.getInstance().isLiveChess()) {
			if (AppData.isLiveChess(this)) {
				lccHolder.logout();
			}
		}
		 else if (tag.equals(OBSOLETE_VERSION_TAG)) {
			// Show site and
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AppData.setLiveChessMode(getContext(), false);
//					DataHolder.getInstance().setLiveChess(false);
					lccHolder.setConnected(false);
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse(RestHelper.PLAY_ANDROID_HTML)));
				}
			});
		}
		if (tag.equals(CHECK_UPDATE_TAG)) {
			if (forceFlag) {
				// drop start day
				preferencesEditor.putLong(AppConstants.START_DAY, 0);
				preferencesEditor.commit();

				backToLoginActivity();
			}
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.GOOGLE_PLAY_URI));
			startActivity(intent);
		}
		else if (tag.equals(LOGOUT_TAG)) {
			getLccHolder().logout();
			getActionBarHelper().showMenuItemById(R.id.menu_signOut, getLccHolder().isConnected());
		} else if (tag.equals(CHALLENGE_TAG)) {
			Log.i(TAG, "Accept challenge: " + currentChallenge);
			challengeTaskRunner.runAcceptChallengeTask(currentChallenge);
			challengeTaskRunner.declineAllChallenges(currentChallenge, getLccHolder().getChallenges());
		}

		super.onPositiveBtnClick(fragment);
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {// Challenge declined!
		String tag = fragment.getTag();
		if (tag == null) {
			super.onNegativeBtnClick(fragment);
			return;
		}

		if (tag.equals(CHALLENGE_TAG)) {
			Log.i(TAG, "Decline challenge: " + currentChallenge);
			//fragment.dismiss();
			popupManager.remove(fragment);
			challengeTaskRunner.declineCurrentChallenge(currentChallenge, getLccHolder().getChallenges());
		}
		super.onNegativeBtnClick(fragment);
	}

	private void adjustActionBar(){
		boolean isConnected = getLccHolder() != null && getLccHolder().isConnected();
		if (getLccHolder() != null) {
			getActionBarHelper().showMenuItemById(R.id.menu_signOut, isConnected);
		}
		getActionBarHelper().showMenuItemById(R.id.menu_search, false);
		getActionBarHelper().showMenuItemById(R.id.menu_settings, false);
		getActionBarHelper().showMenuItemById(R.id.menu_new_game, false);
		getActionBarHelper().showMenuItemById(R.id.menu_refresh, false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.sign_out, menu);

		this.menu = menu;
		boolean isConnected = getLccHolder() != null && getLccHolder().isConnected();
		if (getLccHolder() != null) {
			getActionBarHelper().showMenuItemById(R.id.menu_signOut, isConnected, menu);
		}

		getActionBarHelper().showMenuItemById(R.id.menu_search, false, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_settings, false, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_refresh, false, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_new_game, false, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private class ChallengeTaskListener extends AbstractUpdateListener<Challenge> {
		public ChallengeTaskListener() {
			super(getContext());
		}
	}

	// ---------- LiveChessClientEventListenerFace ----------------
	@Override
	public void onConnecting() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActionBarHelper().showMenuItemById(R.id.menu_signOut, false);
				getActionBarHelper().setRefreshActionItemState(true);
			}
		});
	}

	@Override
	public void onConnectionEstablished() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActionBarHelper().setRefreshActionItemState(false);
				getActionBarHelper().showMenuItemById(R.id.menu_signOut, true);
			}
		});
	}

	@Override
	public void onSessionExpired(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				final LinearLayout customView = (LinearLayout) inflater.inflate(R.layout.popup_relogin_frame, null, false);

				PopupItem popupItem = new PopupItem();
				popupItem.setCustomView(customView);

				PopupCustomViewFragment reLoginFragment = PopupCustomViewFragment.newInstance(popupItem);
				reLoginFragment.show(getSupportFragmentManager(), RE_LOGIN_TAG);

				lccHolder.logout();

				((TextView) customView.findViewById(R.id.titleTxt)).setText(message);

				EditText usernameEdt = (EditText) customView.findViewById(R.id.usernameEdt);
				EditText passwordEdt = (EditText) customView.findViewById(R.id.passwordEdt);
				setLoginFields(usernameEdt, passwordEdt);

				customView.findViewById(R.id.re_signin).setOnClickListener(HomeScreenActivity.this);

				LoginButton facebookLoginButton = (LoginButton) customView.findViewById(R.id.re_fb_connect);
                facebookInit(facebookLoginButton);
				facebookLoginButton.logout();

				usernameEdt.setText(AppData.getUserName(HomeScreenActivity.this));
			}
		});
	}

	@Override
	public void onConnectionFailure(String message) {
		if (isPaused) {
			LiveEvent connectionFailureEvent = new LiveEvent();
			connectionFailureEvent.setEvent(LiveEvent.Event.CONNECTION_FAILURE);
			connectionFailureEvent.setMessage(message);
			lccHolder.getPausedActivityLiveEvents().put(connectionFailureEvent.getEvent(), connectionFailureEvent);
		} else {
			processConnectionFailure(message);
		}
	}

	private void processConnectionFailure(String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActionBarHelper().setRefreshActionItemState(false);
				getActionBarHelper().showMenuItemById(R.id.menu_signOut, false);
			}
		});

		showPopupDialog(R.string.error, message, CONNECT_FAILED_TAG);
		getLastPopupFragment().setButtons(1);
	}

    @Override
    public void onConnectionBlocked(boolean blocked) {
    }

    @Override
	public void onObsoleteProtocolVersion() {
		showPopupDialog(R.string.version_check, R.string.version_is_obsolete_update, OBSOLETE_VERSION_TAG);
		getLastPopupFragment().setButtons(1);
		getLastPopupFragment().setCancelable(false);
	}

	@Override
	public void onFriendsStatusChanged(){
	}

	@Override
	public void onAdminAnnounce(String message) {
		showSinglePopupDialog(message);
		getLastPopupFragment().setButtons(1);
	}

	// -----------------------------------------------------


	private void checkUpdate() {
		new CheckUpdateTask(new CheckUpdateListener()).executeTask(RestHelper.GET_ANDROID_VERSION);
	}

	private class CheckUpdateListener extends AbstractUpdateListener<Boolean> {
		public CheckUpdateListener() {
			super(getContext());
		}

		@Override
		public void showProgress(boolean show) {
		}

		@Override
		public void updateData(Boolean returnedObj) {
			forceFlag = returnedObj;
			if (isPaused)
				return;

			showPopupDialog(R.string.update_check, R.string.update_available_please_update, CHECK_UPDATE_TAG);
			getLastPopupFragment().setButtons(1);
		}
	}

	protected void backToLoginActivity() {
		Intent intent = new Intent(this, LoginScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	/*@Override
	public void onClick(View view) {
		if(view.getId() == R.id.re_signin){
			signInUser();
		}
	}*/

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK && requestCode == Facebook.DEFAULT_AUTH_ACTIVITY_CODE){
			facebook.authorizeCallback(requestCode, resultCode, data);
		}
	}

	public LccHolder getLccHolder() {
		return lccHolder;
	}

	private class LiveOuterChallengeListener implements OuterChallengeListener {
		@Override
		public void showDelayedDialog(Challenge challenge) {
			if (isPaused) {
				LiveEvent liveEvent = new LiveEvent();
				liveEvent.setEvent(LiveEvent.Event.CHALLENGE);
				liveEvent.setChallenge(challenge);
				liveEvent.setChallengeDelayed(true);
				getLccHolder().getPausedActivityLiveEvents().put(liveEvent.getEvent(), liveEvent);
			} else {
				currentChallenge = challenge;
				popupItem.setPositiveBtnId(R.string.accept);
				popupItem.setNegativeBtnId(R.string.decline);
				showPopupDialog(R.string.you_been_challenged, composeMessage(challenge), CHALLENGE_TAG);
			}
		}

		@Override
		public void showDialog(Challenge challenge) {
			if (isPaused) {
				LiveEvent liveEvent = new LiveEvent();
				liveEvent.setEvent(LiveEvent.Event.CHALLENGE);
				liveEvent.setChallenge(challenge);
				liveEvent.setChallengeDelayed(false);
				getLccHolder().getPausedActivityLiveEvents().put(liveEvent.getEvent(), liveEvent);
			} else {

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
		if (!preferences.getBoolean(AppConstants.FULLSCREEN_AD_ALREADY_SHOWED, false) && AppUtils.isNeedToUpgrade(this, getLccHolder())) {

			// TODO handle for support show ad on tablet in portrait mode
			// TODO: add support for tablet ad units

			if (InneractiveAdHelper.IS_SHOW_FULLSCREEN_ADS) {

				// todo: use special viewgroup for fullscreen ad
				// todo: test cases when ad is loaded after onPause etc
				InneractiveAd.displayInterstitialAd(this, (LinearLayout) findViewById(R.id.mainView),
						InneractiveAdHelper.FULLSCREEN_APP_ID,
						new InneractiveAdHelper.InneractiveAdListenerImpl(InneractiveAd.IaAdType.Interstitial, preferencesEditor));

			} else {
				moPubInterstitial = new MoPubInterstitial(this, "agltb3B1Yi1pbmNyDQsSBFNpdGUYwLyBEww"); // chess.com
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
		if (isLCSBound) {
		unbindService(liveChessServiceConnectionListener);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_signOut:
				showPopupDialog(R.string.confirm, R.string.signout_confirm, LOGOUT_TAG);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.re_signin){
			signInUser();

		} else if (v.getId() == R.id.playLiveFrame) {
			Class<?> clazz = AppData.isGuest(this) ? SignUpScreenActivity.class : LiveScreenActivity.class;
			startActivity(new Intent(this, clazz));

		} else if (v.getId() == R.id.playOnlineFrame) {
			Class<?> clazz = AppData.isGuest(this) ? SignUpScreenActivity.class : OnlineScreenActivity.class;
			startActivity(new Intent(this, clazz));

		} else if (v.getId() == R.id.playComputerFrame) {
			startActivity(new Intent(this, ComputerScreenActivity.class));

		} else if (v.getId() == R.id.tacticsTrainerFrame) {
			Intent intent = new Intent(this, GameTacticsScreenActivity.class);
			startActivity(intent);

		} else if (v.getId() == R.id.videoLessonsFrame) {
			startActivity(new Intent(this, VideoScreenActivity.class));

		} else if (v.getId() == R.id.settingsFrame) {
			startActivity(new Intent(this, PreferencesScreenActivity.class));
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