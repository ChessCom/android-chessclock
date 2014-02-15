package com.chess.ui.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import com.chess.R;
import com.chess.backend.LiveChessService;
import com.chess.backend.RestHelper;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.lcc.android.LiveConnectionHelper;
import com.chess.lcc.android.LiveEvent;
import com.chess.lcc.android.OuterChallengeListener;
import com.chess.lcc.android.interfaces.LccConnectionUpdateFace;
import com.chess.lcc.android.interfaces.LiveChessClientEventListener;
import com.chess.lcc.android.interfaces.LiveUiUpdateListener;
import com.chess.live.client.Challenge;
import com.chess.live.client.Game;
import com.chess.live.util.GameTimeConfig;
import com.chess.model.PopupItem;
import com.chess.statics.IntentConstants;
import com.chess.statics.Symbol;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.fragments.LiveBaseFragment;
import com.chess.ui.fragments.live.*;
import com.chess.ui.fragments.popup_fragments.PopupDialogFragment;
import com.chess.ui.fragments.settings.SettingsGeneralFragment;
import com.chess.ui.fragments.settings.SettingsGeneralFragmentTablet;
import com.chess.ui.fragments.settings.SettingsLiveChessFragment;
import com.chess.ui.fragments.stats.StatsGameDetailsFragment;
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
public abstract class LiveBaseActivity extends CoreActivityActionBar implements LiveChessClientEventListener, LiveUiUpdateListener {

	private static final String TAG = "LccLog-LiveBaseActivity";

	protected static final String CHALLENGE_TAG = "challenge_tag";
	private static final String CONNECT_FAILED_TAG = "connect_failed";
	private static final String OBSOLETE_VERSION_TAG = "obsolete version";
	private static final String EXIT_GAME_TAG = "exit_game";

	protected LiveOuterChallengeListener outerChallengeListener;
	protected Challenge currentChallenge;

	protected ChallengeTaskListener challengeTaskListener;
	protected GameTaskListener gameTaskListener;
	private LiveServiceConnectionListener liveServiceConnectionListener;
	protected boolean isLCSBound;
	protected LiveConnectionHelper liveHelper;
	private List<PopupDialogFragment> popupChallengesList;


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
	protected void onResume() {
		super.onResume();

		LogMe.dl(TAG, "LiveBaseActivity onResume isLiveChess()=" + getDataHolder().isLiveChess() + ", isLCSBound=" + isLCSBound);

		if (getDataHolder().isLiveChess()) {
			if (isLCSBound) {
				onLiveServiceConnected();
				executePausedActivityLiveEvents();
			} else {
//				Log.d(TAG, "onStart -> bindAndStartLiveService " + getClass());
				bindAndStartLiveService();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		dismissFragmentDialog();

//		liveConnector.removeCallbacks();

		//Log.d(TAG, "LiveBaseActivity go pause, isLCSBound = " + isLCSBound);
		if (isLCSBound) {
			getDataHolder().setLiveChessMode(false);
			liveHelper.startIdleTimeOutCounter();
			setLCSBound(false);  // we drop here flag because we can't drop it after shutdown, as Activity will not exist and only service will be alive
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
			setLCSBound(false);
		}
	}

	/*
	protected boolean checkIfLiveUserAlive() {
		boolean alive = false;
		if (isLCSBound) {
			if (liveHelper.getUser() == null) {
				if (getAppData().isLiveChess()) {
					liveHelper.logout();
				}
				unBindAndStopLiveService();
				alive = false;
			} else {
				alive = true;
			}
		}
		return alive;
	}
	*/

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {   // TODO refactor with showPreviousFragment logic
		if (keyCode == KeyEvent.KEYCODE_BACK && !getSlidingMenu().isMenuShowing()) {

			LogMe.dl(TAG, "LBA back button pressed isLiveChess()=" + getDataHolder().isLiveChess());
			LogMe.dl(TAG, "LBA back button pressed isLCSBound=" + isLCSBound);

			if (getDataHolder().isLiveChess() && isLCSBound) {

				Fragment fragmentByTag = getGameLiveFragment();
				if (fragmentByTag != null && fragmentByTag.isVisible()) {
					if (liveHelper.getCurrentGame() != null && !liveHelper.getCurrentGame().isGameOver()) {
						showPopupDialog(R.string.leave_game, EXIT_GAME_TAG);
						return true;
					}
				}

				// Why we should try to resign current game by clicking back on Wait fragment?
				// Can we avoid this?
				/*fragmentByTag = getSupportFragmentManager().findFragmentByTag(LiveGameWaitFragment.class.getSimpleName());
				if (fragmentByTag != null && fragmentByTag.isVisible()) {
					showPopupDialog(R.string.leave_game, EXIT_GAME_TAG);
					return true;
				}*/

				fragmentByTag = getGameLiveObserverFragment();
				if (fragmentByTag != null && fragmentByTag.isVisible()) {
					liveHelper.exitGameObserving();
					return super.onKeyUp(keyCode, event);
				}

				// todo: @lcc - we should exit Live even if isLCSBound=false
				fragmentByTag = getLiveHomeFragment();
				if (fragmentByTag != null && fragmentByTag.isVisible()) {
					liveHelper.logout();
					getDataHolder().setLiveChessMode(false);
					unBindAndStopLiveService();
					return super.onKeyUp(keyCode, event);
				}
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	public void executePausedActivityLiveEvents() {

		Map<LiveEvent.Event, LiveEvent> pausedActivityLiveEvents = liveHelper.getPausedActivityLiveEvents();
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
			if (getDataHolder().isLiveChess()) {
				liveHelper.logout();
			}
			unBindAndStopLiveService();
		} else if (tag.equals(OBSOLETE_VERSION_TAG)) {
			// Show site and
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					getDataHolder().setLiveChessMode(false);
					liveHelper.setConnected(false);
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.getInstance().PLAY_ANDROID_HTML)));
				}
			});

			unBindAndStopLiveService();

		} else if (tag.contains(CHALLENGE_TAG)) { // Challenge accepted!
			Log.d(TAG, "Accept challenge: " + currentChallenge);
			liveHelper.declineAllChallenges(currentChallenge);
			liveHelper.runAcceptChallengeTask(currentChallenge);

			popupChallengesList.remove(fragment);

			// open game wait fragment from here. If user close it it means he declined challenge
			sendBroadcast(new Intent(IntentConstants.START_LIVE_GAME));
		} else if (tag.equals(EXIT_GAME_TAG)) {
			liveHelper.runMakeResignAndExitTask();
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
				if (!liveHelper.isConnected() || liveHelper.getClient() == null) { // TODO should leave that screen on connection lost or when LCC is become null
					liveHelper.logout();
					unBindAndStopLiveService();
					return;
				}

				Log.d(TAG, "Decline challenge: " + currentChallenge);
				liveHelper.declineCurrentChallenge(currentChallenge);
			}
		}
		super.onNegativeBtnClick(fragment);
	}

	private void bindAndStartLiveService() {
//		Log.d(TAG, "bindAndStartLiveService " + getClass());

		startService(new Intent(this, LiveChessService.class));
		bindService(new Intent(this, LiveChessService.class), liveServiceConnectionListener, BIND_AUTO_CREATE);
	}

	public boolean isLCSBound() {
		return isLCSBound;
	}

	public void setLCSBound(boolean isLCSBound) {
		//LogMe.dl(TAG, "activity setLCSBound=" + isLCSBound);
		this.isLCSBound = isLCSBound;
	}

	@Override
	public void performServiceConnection() {
		if (isLCSBound) {
			if (liveHelper.getLccHelper() != null && liveHelper.isConnected()) {
				onLiveClientConnected();
			} else { // lccHelper here null, so we need to start again connection logic and create all instances
//				Log.d(TAG, "performServiceConnection is LCSBound, lcc helper null or !connected-> bindAndStartLiveService " + getClass());
				bindAndStartLiveService();
			}
		} else {
//			Log.d(TAG, "performServiceConnection, !isLCSBound -> bindAndStartLiveService " + getClass());
			bindAndStartLiveService();
		}
	}

	private class LiveServiceConnectionListener implements ServiceConnection, LccConnectionUpdateFace {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//			LogMe.dl(TAG, "onServiceConnected");

			LiveChessService.ServiceBinder serviceBinder = (LiveChessService.ServiceBinder) iBinder;
			liveHelper = serviceBinder.getService().getLiveConnectionHelper();
			setLCSBound(true);
			liveHelper.setLiveChessClientEventListener(LiveBaseActivity.this);

			liveHelper.setLiveUiUpdateListener(LiveBaseActivity.this);
			liveHelper.setLoginErrorUpdateListener(LiveBaseActivity.this);

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

			onLiveServiceConnected();
			liveHelper.checkAndConnect(this);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
//			LogMe.dl(TAG, "SERVICE: onServiceDisconnected");
			setLCSBound(false);

			if (getSupportFragmentManager() == null) {
				return;
			}
			for (Fragment fragment : getSupportFragmentManager().getFragments()) {
				if (fragment != null && fragment.isVisible()) {
					if (fragment instanceof LiveBaseFragment) {
						((LiveBaseFragment) fragment).onLiveServiceDisconnected();
					}
				}
			}
		}

		@Override
		public void onConnected() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setLCSBound(true);
					onLiveClientConnected();
				}
			});
		}

		@Override
		public void onShutdown() {
			setLCSBound(false);
		}
	}

	protected void onLiveClientConnected() {
		dismissNetworkCheckDialog();
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
				liveHelper.getPausedActivityLiveEvents().put(liveEvent.getEvent(), liveEvent);
			} else {
				showDelayedDialogImmediately(challenge);
			}
		}

		@Override
		public void showDelayedDialogImmediately(Challenge challenge) {
			currentChallenge = challenge;

			popupItem.setPositiveBtnId(R.string.accept);
			popupItem.setNegativeBtnId(R.string.decline);
			showPopupDialog(R.string.you_been_challenged, composeMessage(challenge), CHALLENGE_TAG);
		}

		@Override
		public void showDialogImmediately(Challenge challenge) {
			if (popupChallengesList.size() > 0) {
				return;
			}

			currentChallenge = challenge;

			PopupItem popupItem = new PopupItem();
			popupItem.setTitle(R.string.you_been_challenged);
			popupItem.setMessage(composeMessage(challenge));

			PopupDialogFragment popupDialogFragment = PopupDialogFragment.createInstance(popupItem);
			popupDialogFragment.show(getSupportFragmentManager(), CHALLENGE_TAG);
			popupChallengesList.add(popupDialogFragment);
		}

		@Override
		public void showDialog(Challenge challenge) {
			if (isPaused) {
				LiveEvent liveEvent = new LiveEvent();
				liveEvent.setEvent(LiveEvent.Event.CHALLENGE);
				liveEvent.setChallenge(challenge);
				liveEvent.setChallengeDelayed(false);
				liveHelper.getPausedActivityLiveEvents().put(liveEvent.getEvent(), liveEvent);
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
				blitz = Symbol.wrapInPars(getString(R.string.blitz));
			} else if (config.isLightning()) {
				blitz = Symbol.wrapInPars(getString(R.string.lightning));
			} else if (config.isStandard()) {
				blitz = Symbol.wrapInPars(getString(R.string.standard));
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
	public void onConnectionFailure(String message) {
		if (isPaused) {
			LiveEvent connectionFailureEvent = new LiveEvent();
			connectionFailureEvent.setEvent(LiveEvent.Event.CONNECTION_FAILURE);
			connectionFailureEvent.setMessage(message);
			if (liveHelper.getLccHelper() == null) {
				throw new IllegalStateException(" LccHelper became NULL");
			}
			liveHelper.getPausedActivityLiveEvents().put(connectionFailureEvent.getEvent(), connectionFailureEvent);
		} else {
			processConnectionFailure(message);
		}
	}

	private void processConnectionFailure(String message) {
		if (message.equals(getString(R.string.pleaseLoginAgain))) {

			if (isLCSBound) {
				liveHelper.logout();
				unBindAndStopLiveService();
			}

			liveHelper.performReloginForLive();
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
	public void onAdminAnnounce(String message) {
		showSinglePopupDialog(message);
	}

	// -----------------------------------------------------

	protected void onLiveServiceConnected() {
		LogMe.dl(TAG, "onLiveServiceConnected: liveHelper.getLccHelper() = " + liveHelper.getLccHelper());
		if (liveHelper.getLccHelper() == null) {
			return;
		}

		liveHelper.stopIdleTimeOutCounter(); // screen rotated case

		liveHelper.setOuterChallengeListener(outerChallengeListener);
		liveHelper.setChallengeTaskListener(challengeTaskListener);

		/*
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
		*/
	}

	public GameLiveFragment getGameLiveFragment() {
		GameLiveFragment gameLiveFragment;
		if (!isTablet) {
			gameLiveFragment = (GameLiveFragment) findFragmentByTag(GameLiveFragment.class.getSimpleName());
		} else {
			gameLiveFragment = (GameLiveFragmentTablet) findFragmentByTag(GameLiveFragmentTablet.class.getSimpleName());
		}
		return gameLiveFragment;
	}

	public GameLiveObserveFragment getGameLiveObserverFragment() {
		GameLiveObserveFragment gameLiveFragment;
		if (!isTablet) {
			gameLiveFragment = (GameLiveObserveFragment) findFragmentByTag(GameLiveObserveFragment.class.getSimpleName());
		} else {
			gameLiveFragment = (GameLiveObserveFragmentTablet) findFragmentByTag(GameLiveObserveFragmentTablet.class.getSimpleName());
		}
		return gameLiveFragment;
	}

	public LiveHomeFragment getLiveHomeFragment() {
		LiveHomeFragment liveHomeFragment;
		if (!isTablet) {
			liveHomeFragment = (LiveHomeFragment) findFragmentByTag(LiveHomeFragment.class.getSimpleName());
		} else {
			liveHomeFragment = (LiveHomeFragmentTablet) findFragmentByTag(LiveHomeFragmentTablet.class.getSimpleName());
		}
		return liveHomeFragment;
	}

	public LiveConnectionHelper getLiveHelper() {
		return liveHelper;
	}

	private void dismissNetworkCheckDialog() {
		dismissFragmentDialogByTag(NETWORK_CHECK_TAG);
	}

	@Override
	public void registerGcm() {
		registerGcmService();
	}

	public boolean isLiveFragment(String fragmentName) {
		String liveFragment1 = LiveHomeFragment.class.getSimpleName();
		String liveFragment2 = LiveHomeFragmentTablet.class.getSimpleName();
		String liveFragment3 = GameLiveFragment.class.getSimpleName();
		String liveFragment4 = GameLiveFragmentTablet.class.getSimpleName();
		String liveFragment5 = GameLiveObserveFragment.class.getSimpleName();
		String liveFragment6 = GameLiveObserveFragmentTablet.class.getSimpleName();
		String liveFragment7 = LiveChatFragment.class.getSimpleName();
		String liveFragment8 = LiveGameWaitFragment.class.getSimpleName();
		String liveFragment9 = SettingsLiveChessFragment.class.getSimpleName();
		String liveFragment10 = SettingsGeneralFragment.class.getSimpleName();
		String liveFragment11 = SettingsGeneralFragmentTablet.class.getSimpleName();
		String liveFragment12 = LiveGamesArchiveFragment.class.getSimpleName();
		String liveFragment13 = GameLiveArchiveFragment.class.getSimpleName();
		String liveFragment14 = GameLiveArchiveAnalysisFragment.class.getSimpleName();
		String liveFragment15 = StatsGameDetailsFragment.class.getSimpleName();

		return fragmentName.equals(liveFragment1)
				|| fragmentName.equals(liveFragment2)
				|| fragmentName.equals(liveFragment3)
				|| fragmentName.equals(liveFragment4)
				|| fragmentName.equals(liveFragment5)
				|| fragmentName.equals(liveFragment6)
				|| fragmentName.equals(liveFragment7)
				|| fragmentName.equals(liveFragment8)
				|| fragmentName.equals(liveFragment9)
				|| fragmentName.equals(liveFragment10)
				|| fragmentName.equals(liveFragment11)
				|| fragmentName.equals(liveFragment12)
				|| fragmentName.equals(liveFragment13)
				|| fragmentName.equals(liveFragment14)
				|| fragmentName.equals(liveFragment15);
	}
}
