package com.chess.ui.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import com.chess.R;
import com.chess.backend.LiveChessService;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.LoginItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.lcc.android.LiveEvent;
import com.chess.lcc.android.OuterChallengeListener;
import com.chess.lcc.android.interfaces.LccConnectionUpdateFace;
import com.chess.lcc.android.interfaces.LiveChessClientEventListener;
import com.chess.live.client.Challenge;
import com.chess.live.client.Game;
import com.chess.live.util.GameTimeConfig;
import com.chess.model.PopupItem;
import com.chess.statics.AppConstants;
import com.chess.statics.IntentConstants;
import com.chess.statics.Symbol;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.fragments.LiveBaseFragment;
import com.chess.ui.fragments.live.*;
import com.chess.ui.fragments.popup_fragments.PopupDialogFragment;
import com.chess.utilities.AppUtils;
import com.chess.utilities.LogMe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



/**
 * LiveBaseActivity class
 *
 * @author alien_roger
 * @created at: 11.04.12 9:00
 */
public abstract class LiveBaseActivity extends CoreActivityActionBar implements LiveChessClientEventListener {

	private static final String TAG = "LccLog-LiveBaseActivity";

	protected static final String CHALLENGE_TAG = "challenge_tag";
	protected static final String LOGOUT_TAG = "logout_tag";
	private static final String CONNECT_FAILED_TAG = "connect_failed";
	private static final String OBSOLETE_VERSION_TAG = "obsolete version";
	private static final String EXIT_GAME_TAG = "exit_game";
	private static final long RETRY_DELAY = 100;

	protected LiveOuterChallengeListener outerChallengeListener;
	protected Challenge currentChallenge;

	protected ChallengeTaskListener challengeTaskListener;
	protected GameTaskListener gameTaskListener;
	private LiveServiceConnectionListener liveServiceConnectionListener;
	protected boolean isLCSBound;
	protected LiveChessService liveService;
	private List<PopupDialogFragment> popupChallengesList;
	private boolean needReLoginToLive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameTaskListener = new GameTaskListener();
		challengeTaskListener = new ChallengeTaskListener();
		outerChallengeListener = new LiveOuterChallengeListener();
		liveServiceConnectionListener = new LiveServiceConnectionListener();

		popupChallengesList = new ArrayList<PopupDialogFragment>();
	}

	@Override
	protected void onStart() {
		super.onStart();

//		LogMe.dl(TAG, "onStart: getAppData().isLiveChess() = " + getAppData().isLiveChess());
//		LogMe.dl(TAG, "onStart: isLCSBound = " + isLCSBound);

		if (getAppData().isLiveChess()) {
			if (!AppUtils.isNetworkAvailable(this)) {
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.no_network, NETWORK_CHECK_TAG);
			}
			if (isLCSBound) {
				onLiveServiceConnected();
			} else {
				bindAndStartLiveService();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isLCSBound) {
			executePausedActivityLiveEvents();
			liveService.stopIdleTimeOutCounter();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		dismissFragmentDialog();

		Log.d("TEST", " LiveBaseActivity go pause, isLCSBound = " + isLCSBound);
		if (isLCSBound) {
			liveService.startIdleTimeOutCounter();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindLiveService();
	}

	public void unBindAndStopLiveService() {
//		LogMe.dl(TAG, "unBindAndStopLiveService: isLCSBound=" + isLCSBound);
		unbindLiveService();
		stopService(new Intent(this, LiveChessService.class));
	}

	private void unbindLiveService() {
		if (isLCSBound) {
			unbindService(liveServiceConnectionListener);
			isLCSBound = false;
		}
	}

	protected boolean checkIfLiveUserAlive() {
		boolean alive = false;
		if (isLCSBound) {
			if (liveService.getUser() == null) {
				if (getAppData().isLiveChess()) {
					liveService.logout();
				}
				unBindAndStopLiveService();
				alive = false;
			} else {
				alive = true;
			}
		}
		return alive;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {   // TODO refactor with showPreviousFragment logic
		if (keyCode == KeyEvent.KEYCODE_BACK && !getSlidingMenu().isMenuShowing()) {
			if (getAppData().isLiveChess() && isLCSBound) {

				Fragment fragmentByTag = getGameLiveFragment();
				if (fragmentByTag != null && fragmentByTag.isVisible()) {
					if (liveService.getCurrentGame() != null && !liveService.getCurrentGame().isGameOver()) {
						showPopupDialog(R.string.leave_game, EXIT_GAME_TAG);
						return true;
					}
				}

				fragmentByTag = getSupportFragmentManager().findFragmentByTag(LiveGameWaitFragment.class.getSimpleName());
				if (fragmentByTag != null && fragmentByTag.isVisible()) {
					showPopupDialog(R.string.leave_game, EXIT_GAME_TAG);
					return true;
				}

				fragmentByTag = getGameLiveObserverFragment();
				if (fragmentByTag != null && fragmentByTag.isVisible()) {
					liveService.exitGameObserving();
					return super.onKeyUp(keyCode, event);
				}

				fragmentByTag = getLiveHomeFragment();
				if (fragmentByTag != null && fragmentByTag.isVisible()) {
					liveService.logout();
					unBindAndStopLiveService();
					return super.onKeyUp(keyCode, event);
				}
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	public void executePausedActivityLiveEvents() {

		Map<LiveEvent.Event, LiveEvent> pausedActivityLiveEvents = liveService.getPausedActivityLiveEvents();
//		LogMe.dl(TAG, "executePausedActivityLiveEvents size=" + pausedActivityLiveEvents.size() + ", events=" + pausedActivityLiveEvents);

		if (pausedActivityLiveEvents.size() > 0) {

			LiveEvent connectionFailureEvent = pausedActivityLiveEvents.get(LiveEvent.Event.CONNECTION_FAILURE);
			if (connectionFailureEvent != null) {
				pausedActivityLiveEvents.remove(LiveEvent.Event.CONNECTION_FAILURE);
				processConnectionFailure(connectionFailureEvent.getMessage());
				// todo: clear all events because of ConnectionFailure?
			}

			LiveEvent challengeEvent = pausedActivityLiveEvents.get(LiveEvent.Event.CHALLENGE);
			if (challengeEvent != null) {
				pausedActivityLiveEvents.remove(LiveEvent.Event.CHALLENGE);
				if (challengeEvent.isChallengeDelayed()) {
					outerChallengeListener.showDelayedDialogImmediately(challengeEvent.getChallenge());
				} else {
					outerChallengeListener.showDialogImmediately(challengeEvent.getChallenge());
				}
			}
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
			if (getAppData().isLiveChess()) {
				liveService.logout();
			}
			unBindAndStopLiveService();
		} else if (tag.equals(OBSOLETE_VERSION_TAG)) {
			// Show site and
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					getAppData().setLiveChessMode(false);
					liveService.setConnected(false);
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.getInstance().PLAY_ANDROID_HTML)));
				}
			});

			unBindAndStopLiveService();

		} else if (tag.equals(LOGOUT_TAG)) {
			if (isLCSBound) {
				liveService.logout();
				unBindAndStopLiveService();
			}
		} else if (tag.contains(CHALLENGE_TAG)) { // Challenge accepted!
			Log.d(TAG, "Accept challenge: " + currentChallenge);
			liveService.declineAllChallenges(currentChallenge);
			liveService.runAcceptChallengeTask(currentChallenge);

			popupChallengesList.remove(fragment);

			// open game wait fragment from here. If user close it it means he declined challenge
			sendBroadcast(new Intent(IntentConstants.START_LIVE_GAME));
		} else if (tag.equals(NETWORK_CHECK_TAG)) {
			startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), NETWORK_REQUEST);

		} else if (tag.equals(EXIT_GAME_TAG)) {
			liveService.runMakeResignAndExitTask();
			onBackPressed();
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

			popupChallengesList.remove(fragment);
			if (isLCSBound) {

				// todo: refactor with new LCC
				if (!liveService.isConnected() || liveService.getClient() == null) { // TODO should leave that screen on connection lost or when LCC is become null
					liveService.logout();
					unBindAndStopLiveService();
					return;
				}

				Log.d(TAG, "Decline challenge: " + currentChallenge);
				liveService.declineCurrentChallenge(currentChallenge);
			}
		}
		super.onNegativeBtnClick(fragment);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		LogMe.dl(TAG, "onActivityResult, resultCode = " + resultCode + " data = " + data);
		if (resultCode == RESULT_OK && requestCode == NETWORK_REQUEST) {
			bindAndStartLiveService();
		}
	}

	public void connectLcc() {
//		LogMe.dl(TAG, "connectLcc: getAppData().isLiveChess() = " + getAppData().isLiveChess());
//		LogMe.dl(TAG, "connectLcc: isLCSBound = " + isLCSBound);
		if (getAppData().isLiveChess()) {
			if (!AppUtils.isNetworkAvailable(this)) {
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.no_network, NETWORK_CHECK_TAG);
			}
			if (isLCSBound) {
				if (liveService.getLccHelper() != null && liveService.getLccHelper().isConnected()) {
					onLiveClientConnected();
				} else {
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (isLCSBound) {
								if (liveService.getLccHelper() != null && liveService.getLccHelper().isConnected()) {
									onLiveClientConnected();
									handler.removeCallbacks(this);
								} else {
									handler.postDelayed(this, RETRY_DELAY);
									Log.d("TEST", "Service bounded but client not connected");
								}
							}
						}
					}, RETRY_DELAY);
					Log.d("TEST", "Service bounded but client not connected");
				}
			} else {
				bindAndStartLiveService();
			}
		}
	}

	private void bindAndStartLiveService() {
//		Log.d(TAG, "bindAndStartLiveService " + getClass());

		startService(new Intent(this, LiveChessService.class));
		bindService(new Intent(this, LiveChessService.class), liveServiceConnectionListener, BIND_AUTO_CREATE);
	}

	public boolean isLCSBound() {
		return isLCSBound;
	}

	private class LiveServiceConnectionListener implements ServiceConnection, LccConnectionUpdateFace {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//			LogMe.dl(TAG, "onServiceConnected");

			LiveChessService.ServiceBinder serviceBinder = (LiveChessService.ServiceBinder) iBinder;
			liveService = serviceBinder.getService();
			isLCSBound = true;
			liveService.setLiveChessClientEventListener(LiveBaseActivity.this);

			if (getSupportFragmentManager() == null) {
				return;
			}
			for (Fragment fragment : getSupportFragmentManager().getFragments()) {
				if (fragment != null && fragment.isVisible()) {
					if (fragment instanceof LiveBaseFragment) {
						((LiveBaseFragment) fragment).onLiveServiceConnected();
						((LiveBaseFragment) fragment).setLCSBound(isLCSBound);
					}
				}
			}

			onLiveServiceConnected();
			liveService.checkAndConnect(this);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
//			LogMe.dl(TAG, "SERVICE: onServiceDisconnected");
			isLCSBound = false;
			if (getSupportFragmentManager() == null) {
				return;
			}
			for (Fragment fragment : getSupportFragmentManager().getFragments()) {
				if (fragment != null && fragment.isVisible()) {
					if (fragment instanceof LiveBaseFragment) {
						((LiveBaseFragment) fragment).onLiveServiceDisconnected();
						((LiveBaseFragment) fragment).setLCSBound(isLCSBound);
					}
				}
			}
		}

		@Override
		public void onConnected() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onLiveClientConnected();
				}
			});
		}
	}

	protected void onLiveClientConnected() {
		if (getSupportFragmentManager() == null) {
			return;
		}
		for (Fragment fragment : getSupportFragmentManager().getFragments()) {
			if (fragment != null && fragment.isVisible()) {
				if (fragment instanceof LiveBaseFragment) {
					((LiveBaseFragment) fragment).onLiveClientConnected();
				}
			}
		}
	}

	private class LiveOuterChallengeListener implements OuterChallengeListener {
		@Override
		public void showDelayedDialog(Challenge challenge) {
			if (isPaused) {
				LiveEvent liveEvent = new LiveEvent();
				liveEvent.setEvent(LiveEvent.Event.CHALLENGE);
				liveEvent.setChallenge(challenge);
				liveEvent.setChallengeDelayed(true);
				liveService.getPausedActivityLiveEvents().put(liveEvent.getEvent(), liveEvent);
			} else {
				showDelayedDialogImmediately(challenge);
			}
		}

		@Override
		public void showDelayedDialogImmediately(Challenge challenge) {
			LogMe.dl(TAG, "CHALLENGE showDelayedDialogImmediately -> popupDialogFragment.show ");
			currentChallenge = challenge;

			popupItem.setPositiveBtnId(R.string.accept);
			popupItem.setNegativeBtnId(R.string.decline);
			showPopupDialog(R.string.you_been_challenged, composeMessage(challenge), CHALLENGE_TAG);
		}

		@Override
		public void showDialogImmediately(Challenge challenge) {
			if (popupChallengesList.size() > 0) {
				LogMe.dl("LCCLOG", "show challenge dialog: popupManager.size() " + popupManager.size());
				return;
			}

			currentChallenge = challenge;

			PopupItem popupItem = new PopupItem();
			popupItem.setTitle(R.string.you_been_challenged);
			popupItem.setMessage(composeMessage(challenge));

			PopupDialogFragment popupDialogFragment = PopupDialogFragment.createInstance(popupItem);
			popupDialogFragment.show(getSupportFragmentManager(), CHALLENGE_TAG);
			LogMe.dl(TAG, "CHALLENGE showDialogImmediately -> popupDialogFragment.show ");
			popupChallengesList.add(popupDialogFragment);
		}

		@Override
		public void showDialog(Challenge challenge) {
			LogMe.dl(TAG, "CHALLENGE showDialog -> isPaused = " + isPaused);
			if (isPaused) {
				LiveEvent liveEvent = new LiveEvent();
				liveEvent.setEvent(LiveEvent.Event.CHALLENGE);
				liveEvent.setChallenge(challenge);
				liveEvent.setChallengeDelayed(false);
				liveService.getPausedActivityLiveEvents().put(liveEvent.getEvent(), liveEvent);
			} else {
				SoundPlayer.getInstance(getContext()).playNotify();
				showDialogImmediately(challenge);
			}
		}

		@Override
		public void hidePopups() {
			dismissAllPopups();
		}

		private String composeMessage(Challenge challenge) {
			String rated = challenge.isRated() ? getString(R.string.rated) : getString(R.string.unrated);
			GameTimeConfig config = challenge.getGameTimeConfig();
			String blitz = Symbol.EMPTY;
			if (config.isBlitz()) {
				blitz = getString(R.string.blitz_game);
			} else if (config.isLightning()) {
				blitz = getString(R.string.lightning_game);
			} else if (config.isStandard()) {
				blitz = getString(R.string.standard_game);
			}

			String timeIncrement = Symbol.EMPTY;

			if (config.getTimeIncrement() > 0) {
				timeIncrement = " | " + String.valueOf(config.getTimeIncrement() / 10);
			}

			String timeMode = config.getBaseTime() / 10 / 60 + timeIncrement + Symbol.SPACE + blitz;
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
					.append(getString(R.string.opponent_)).append(Symbol.SPACE)
					.append(challenge.getFrom().getUsername()).append(Symbol.NEW_STR)
					.append(getString(R.string.time_)).append(Symbol.SPACE)
					.append(timeMode).append(Symbol.NEW_STR)
					.append(getString(R.string.you_play)).append(Symbol.SPACE)
					.append(playerColor).append(Symbol.NEW_STR)
					.append(rated)
					.toString();
		}
	}

	public class ChallengeTaskListener extends ActionBarUpdateListener<Challenge> {
		public ChallengeTaskListener() {
			super(getInstance());
		}

		@Override
		public void updateData(Challenge returnedObj) {
			super.updateData(returnedObj);
			challengeTaskUpdated(returnedObj);
		}
	}

	protected void challengeTaskUpdated(Challenge challenge) {

	}

	private class GameTaskListener extends ActionBarUpdateListener<Game> {
		public GameTaskListener() {
			super(getInstance());
		}
	}

	// ---------- LiveChessClientEventListener ----------------
	@Override
	public void onConnecting() {
	}

	@Override
	public void onConnectionEstablished() {
	}

	@Override
	public void onSessionExpired(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				performReloginForLive();
			}
		});
	}

	private void performReloginForLive() {
		// Logout first to make clear connect
		liveService.logout();
		unBindAndStopLiveService();

		String password = getAppData().getPassword();
		if (!TextUtils.isEmpty(password)) {

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_LOGIN);
			loadItem.setRequestMethod(RestHelper.POST);
			loadItem.addRequestParams(RestHelper.P_DEVICE_ID, getDeviceId());
			loadItem.addRequestParams(RestHelper.P_USER_NAME_OR_MAIL, getAppData().getUsername());
			loadItem.addRequestParams(RestHelper.P_PASSWORD, password);
			loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.P_USERNAME);
			loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.P_TACTICS_RATING);

			new RequestJsonTask<LoginItem>(new LoginUpdateListener()).executeTask(loadItem);
		} else {
			loginWithFacebook(getAppData().getFacebookToken());
		}

		needReLoginToLive = true;
	}

	@Override
	public void onConnectionFailure(String message) {
		if (isPaused) {
			LiveEvent connectionFailureEvent = new LiveEvent();
			connectionFailureEvent.setEvent(LiveEvent.Event.CONNECTION_FAILURE);
			connectionFailureEvent.setMessage(message);
			if (liveService.getLccHelper() == null) {
				throw new IllegalStateException(" LccHelper became NULL");
			}
			liveService.getPausedActivityLiveEvents().put(connectionFailureEvent.getEvent(), connectionFailureEvent);
		} else {
			processConnectionFailure(message);
		}
	}

	private void processConnectionFailure(String message) {
		if (message.equals(getString(R.string.pleaseLoginAgain))) {
			performReloginForLive();
		} else {
			showPopupDialog(R.string.error, message, CONNECT_FAILED_TAG, 1);
			getLastPopupFragment().setCancelable(false);
		}
	}

	@Override
	public void onConnectionBlocked(final boolean blocked) {

		GameLiveFragment gameLiveFragment = getGameLiveFragment();
		if (gameLiveFragment != null) {
			gameLiveFragment.onConnectionBlocked(blocked);
			return;
		}

		gameLiveFragment = getGameLiveObserverFragment();
		if (gameLiveFragment != null) {
			gameLiveFragment.onConnectionBlocked(blocked);
		}

	}

	@Override
	public void onObsoleteProtocolVersion() {
		popupItem.setButtons(1);
		showPopupDialog(R.string.version_check, R.string.version_is_obsolete_update, OBSOLETE_VERSION_TAG);
		getLastPopupFragment().setCancelable(false);
	}

	@Override
	public void onFriendsStatusChanged() {

	}

	@Override
	public void onAdminAnnounce(String message) {
		showSinglePopupDialog(message);
	}

	// -----------------------------------------------------

	private class LoginUpdateListener extends AbstractUpdateListener<LoginItem> {
		public LoginUpdateListener() {
			super(getContext(), LoginItem.class);
		}

		@Override
		public void showProgress(boolean show) { // DO not show progress as we already showing it while making first attempt to connect
//			if (show) {
//				showPopupHardProgressDialog(R.string.signing_in_);
//			} else {
//				if (isPaused) {
//					return;
//				}
//
//				dismissProgressDialog();
//			}
		}

		@Override
		public void updateData(LoginItem returnedObj) {
			LoginItem.Data loginData = returnedObj.getData();

			if (!TextUtils.isEmpty(loginData.getUsername())) {
				preferencesEditor.putString(AppConstants.USERNAME, loginData.getUsername().trim().toLowerCase());
			}
			preferencesEditor.putInt(AppConstants.USER_PREMIUM_STATUS, loginData.getPremiumStatus());
			preferencesEditor.putString(AppConstants.LIVE_SESSION_ID, loginData.getSessionId());
			preferencesEditor.putLong(AppConstants.LIVE_SESSION_ID_SAVE_TIME, System.currentTimeMillis());
			preferencesEditor.putString(AppConstants.USER_TOKEN, loginData.getLoginToken());
			preferencesEditor.putLong(AppConstants.USER_TOKEN_SAVE_TIME, System.currentTimeMillis());
			preferencesEditor.commit();

			registerGcmService();

			getAppData().setLiveChessMode(true);
			connectLcc();
		}
	}

	@Override
	protected void afterLogin() {
		super.afterLogin();

		if (needReLoginToLive) {
			getAppData().setLiveChessMode(true);
			connectLcc();
		}
	}

	protected void onLiveServiceConnected() {
		LogMe.dl(TAG, "onLiveServiceConnected: liveService.getLccHelper() = " + liveService.getLccHelper());
		if (liveService.getLccHelper() == null) {
			return;
		}

		liveService.setOuterChallengeListener(outerChallengeListener);
		liveService.setChallengeTaskListener(challengeTaskListener);

		if (getSupportFragmentManager() == null) {
			return;
		}
		for (Fragment fragment : getSupportFragmentManager().getFragments()) {
			if (fragment != null && fragment.isVisible()) {
				if (fragment instanceof LiveBaseFragment) {
					((LiveBaseFragment) fragment).onLiveServiceConnected();
				}
			}
		}
	}

	protected GameLiveFragment getGameLiveFragment() {
		GameLiveFragment gameLiveFragment;
		if (!isTablet) {
			gameLiveFragment = (GameLiveFragment) findFragmentByTag(GameLiveFragment.class.getSimpleName());
		} else {
			gameLiveFragment = (GameLiveFragmentTablet) findFragmentByTag(GameLiveFragmentTablet.class.getSimpleName());
		}
		return gameLiveFragment;
	}

	protected GameLiveObserveFragment getGameLiveObserverFragment() {
		GameLiveObserveFragment gameLiveFragment;
		if (!isTablet) {
			gameLiveFragment = (GameLiveObserveFragment) findFragmentByTag(GameLiveObserveFragment.class.getSimpleName());
		} else {
			gameLiveFragment = (GameLiveObserveFragmentTablet) findFragmentByTag(GameLiveObserveFragmentTablet.class.getSimpleName());
		}
		return gameLiveFragment;
	}

	protected LiveHomeFragment getLiveHomeFragment() {
		LiveHomeFragment liveHomeFragment;
		if (!isTablet) {
			liveHomeFragment = (LiveHomeFragment) findFragmentByTag(LiveHomeFragment.class.getSimpleName());
		} else {
			liveHomeFragment = (LiveHomeFragmentTablet) findFragmentByTag(LiveHomeFragmentTablet.class.getSimpleName());
		}
		return liveHomeFragment;
	}

	public LiveChessService getLiveService() {
		return liveService;
	}
}
