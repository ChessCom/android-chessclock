package com.chess.ui.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LiveChessService;
import com.chess.backend.RestHelper;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LiveEvent;
import com.chess.lcc.android.OuterChallengeListener;
import com.chess.lcc.android.interfaces.LccConnectionUpdateFace;
import com.chess.lcc.android.interfaces.LiveChessClientEventListener;
import com.chess.live.client.Challenge;
import com.chess.live.client.Game;
import com.chess.live.util.GameTimeConfig;
import com.chess.model.PopupItem;
import com.chess.ui.fragments.live.LiveGameWaitFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.popup_fragments.PopupDialogFragment;
import com.chess.utilities.AppUtils;
import com.facebook.widget.LoginButton;

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

		if (AppData.isLiveChess(this)) {
			if (!AppUtils.isNetworkAvailable(this)) {
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
			}
			Log.d("TEST", "onStart isLCSBound = " + isLCSBound + " in " + LiveBaseActivity.this);
			if (isLCSBound) {
				onLiveServiceConnected();
			} else {
				bindService(new Intent(this, LiveChessService.class), liveServiceConnectionListener, BIND_AUTO_CREATE);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unBindLiveService();
	}

	public void unBindLiveService() {
		if (isLCSBound) {
			unbindService(liveServiceConnectionListener);
			isLCSBound = false;
		}
		stopService(new Intent(this, LiveChessService.class));
	}

	protected boolean checkIfLiveUserAlive() {
		boolean alive = false;
		if (isLCSBound) {
			if (liveService.getUser() == null) {
				if (AppData.isLiveChess(this)) {
					liveService.logout();
				}
//				backToHomeActivity();
				unBindLiveService();
				alive = false;
			} else {
				alive = true;
			}
		}
		return alive;
	}

	@Override
	protected void onPause() {
		super.onPause();

		dismissFragmentDialog();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isLCSBound) {
			executePausedActivityLiveEvents();
		}
	}

	public void executePausedActivityLiveEvents() {

		Map<LiveEvent.Event, LiveEvent> pausedActivityLiveEvents = liveService.getPausedActivityLiveEvents();
		Log.d(TAG, "executePausedActivityLiveEvents size=" + pausedActivityLiveEvents.size() + ", events=" + pausedActivityLiveEvents);

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
			if (AppData.isLiveChess(this)) {
				liveService.logout();
			}
			unBindLiveService();
//			backToHomeActivity();
		} else if (tag.equals(OBSOLETE_VERSION_TAG)) {
			// Show site and
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AppData.setLiveChessMode(getContext(), false);
					liveService.setConnected(false);
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.PLAY_ANDROID_HTML)));
				}
			});

			unBindLiveService();
//			backToHomeActivity();

		} else if (tag.equals(LOGOUT_TAG)) {
			if (isLCSBound) {
				liveService.logout();
				unBindLiveService();
			}
//			backToHomeActivity();
		} else if (tag.contains(CHALLENGE_TAG)) { // Challenge accepted!
			Log.i(TAG, "Accept challenge: " + currentChallenge);
			liveService.declineAllChallenges(currentChallenge);
			liveService.runAcceptChallengeTask(currentChallenge);


			popupChallengesList.remove(fragment);
		} else if (tag.equals(NETWORK_CHECK_TAG)) {
			startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), NETWORK_REQUEST);
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
					unBindLiveService();
					return;
				}

				Log.i(TAG, "Decline challenge: " + currentChallenge);
				liveService.declineCurrentChallenge(currentChallenge);
			}
		}
		super.onNegativeBtnClick(fragment);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && requestCode == NETWORK_REQUEST) {
			bindService(new Intent(this, LiveChessService.class), liveServiceConnectionListener, BIND_AUTO_CREATE);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//			case R.id.menu_singOut:
//				showPopupDialog(R.string.confirm, R.string.signout_confirm, LOGOUT_TAG);
//				break;
//		}
		return super.onOptionsItemSelected(item);
	}

	public void connectLcc() {
		if (AppData.isLiveChess(this)) {
			if (!AppUtils.isNetworkAvailable(this)) {
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
			}
			Log.d("TEST", "onStart isLCSBound = " + isLCSBound + " in " + LiveBaseActivity.this);
			if (isLCSBound) {
				onLiveServiceConnected();
			} else {
				bindService(new Intent(this, LiveChessService.class), liveServiceConnectionListener, BIND_AUTO_CREATE);
			}
		}
	}

	public boolean isLCSBound() {
		return isLCSBound;
	}

	private class LiveServiceConnectionListener implements ServiceConnection, LccConnectionUpdateFace {


		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			Log.d(TAG, "onLiveServiceConnected");
			Log.d("TEST", "onLiveServiceConnected in " + LiveBaseActivity.this);

			LiveChessService.ServiceBinder serviceBinder = (LiveChessService.ServiceBinder) iBinder;
			liveService = serviceBinder.getService();
			isLCSBound = true;
			liveService.setLiveChessClientEventListener(LiveBaseActivity.this);

			LiveGameWaitFragment waitFragment = (LiveGameWaitFragment) findFragmentByTag(LiveGameWaitFragment.class.getSimpleName());
			if (waitFragment != null) {
				waitFragment.setLCSBound(isLCSBound);
			}

			liveService.checkAndConnect(this);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Log.d(TAG, "SERVICE: onServiceDisconnected");
			isLCSBound = false;
			LiveGameWaitFragment waitFragment = (LiveGameWaitFragment) findFragmentByTag(LiveGameWaitFragment.class.getSimpleName());
			if (waitFragment != null) {
				waitFragment.setLCSBound(isLCSBound);
			}
		}

		@Override
		public void onConnected() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onLiveServiceConnected();
				}
			});
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
			Log.d(TAG, "CHALLENGE showDelayedDialogImmediately -> popupDialogFragment.show ");
			currentChallenge = challenge;

			popupItem.setPositiveBtnId(R.string.accept);
			popupItem.setNegativeBtnId(R.string.decline);
			showPopupDialog(R.string.you_been_challenged, composeMessage(challenge), CHALLENGE_TAG);
		}

		@Override
		public void showDialogImmediately(Challenge challenge) {
			if (popupChallengesList.size() > 0) {
				Log.d("LCCLOG", "show challenge dialog: popupManager.size() " + popupManager.size());
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
			Log.d(TAG, "CHALLENGE showDialogImmediately -> popupDialogFragment.show ");
			popupChallengesList.add(popupDialogFragment);
		}

		@Override
		public void showDialog(Challenge challenge) {
			Log.d(TAG, "CHALLENGE showDialog -> isPaused = " + isPaused);
			if (isPaused) {
				LiveEvent liveEvent = new LiveEvent();
				liveEvent.setEvent(LiveEvent.Event.CHALLENGE);
				liveEvent.setChallenge(challenge);
				liveEvent.setChallengeDelayed(false);
				liveService.getPausedActivityLiveEvents().put(liveEvent.getEvent(), liveEvent);
			} else {
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
			String blitz = StaticData.SYMBOL_EMPTY;
			if (config.isBlitz()) {
				blitz = getString(R.string.blitz_game);
			} else if (config.isLightning()) {
				blitz = getString(R.string.lightning_game);
			} else if (config.isStandard()) {
				blitz = getString(R.string.standard_game);
			}

			String timeIncrement = StaticData.SYMBOL_EMPTY;

			if (config.getTimeIncrement() > 0) {
				timeIncrement = " | " + String.valueOf(config.getTimeIncrement() / 10);
			}

			String timeMode = config.getBaseTime() / 10 / 60 + timeIncrement + StaticData.SYMBOL_SPACE + blitz;
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
					.append(challenge.getFrom().getUsername()).append(StaticData.SYMBOL_NEW_STR)
					.append(getString(R.string.time_)).append(StaticData.SYMBOL_SPACE)
					.append(timeMode).append(StaticData.SYMBOL_NEW_STR)
					.append(getString(R.string.you_play)).append(StaticData.SYMBOL_SPACE)
					.append(playerColor).append(StaticData.SYMBOL_NEW_STR)
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
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
//				getActionBarHelper().showMenuItemById(R.id.menu_signOut, false);
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
//				getActionBarHelper().showMenuItemById(R.id.menu_signOut, true);
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

				liveService.logout();
				unBindLiveService();

				((TextView) customView.findViewById(R.id.titleTxt)).setText(message);

				EditText usernameEdt = (EditText) customView.findViewById(R.id.usernameEdt);
				EditText passwordEdt = (EditText) customView.findViewById(R.id.passwordEdt);
				setLoginFields(usernameEdt, passwordEdt);

				customView.findViewById(R.id.re_signin).setOnClickListener(LiveBaseActivity.this);

				LoginButton facebookLoginButton = (LoginButton) customView.findViewById(R.id.re_fb_connect);
				facebookInit(facebookLoginButton);
//				facebookLoginButton.logout();

				usernameEdt.setText(AppData.getUserName(LiveBaseActivity.this));

				needReLoginToLive = true;
			}
		});
	}

	@Override
	public void onConnectionFailure(String message) {
		if (isPaused) {
			LiveEvent connectionFailureEvent = new LiveEvent();
			connectionFailureEvent.setEvent(LiveEvent.Event.CONNECTION_FAILURE);
			connectionFailureEvent.setMessage(message);
			liveService.getPausedActivityLiveEvents().put(connectionFailureEvent.getEvent(), connectionFailureEvent);
		} else {
			processConnectionFailure(message);
		}
	}

	private void processConnectionFailure(String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
//				getActionBarHelper().setRefreshActionItemState(false);
//				getActionBarHelper().showMenuItemById(R.id.menu_signOut, false);
			}
		});

		showPopupDialog(R.string.error, message, CONNECT_FAILED_TAG, 1);
		getLastPopupFragment().setCancelable(false);
	}

	@Override
	public void onConnectionBlocked(final boolean blocked) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActionBarHelper().setRefreshActionItemState(blocked);
			}
		});
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		adjustSignOutActionMenu(menu);
		return result;
	}

	protected void adjustSignOutActionMenu(Menu menu) {
//		boolean isConnected = false;
//		if (isLCSBound) {
//			isConnected = liveService.isConnected();
//		}
//		if (HONEYCOMB_PLUS_API) {// this item doesn't exist at this point in pre-ICS
////			getActionBarHelper().showMenuItemById(R.id.menu_signOut, isConnected, menu);
//		}
	}

	protected void onLiveServiceConnected() {
//		Log.d("TEST", "onLiveConnected callback, liveService.isConnected() = " + liveService.isConnected()
//				+ " in " + LiveBaseActivity.this);
//		Log.d(TAG, " onLiveServiceConnected callback, liveService.isConnected() = " + liveService.isConnected());
//		getActionBarHelper().showMenuItemById(R.id.menu_signOut, liveService.isConnected());

		if (liveService.getLccHelper() == null) {
			return;
		}

		liveService.setOuterChallengeListener(outerChallengeListener);
		liveService.setChallengeTaskListener(challengeTaskListener);

		LiveGameWaitFragment waitFragment = (LiveGameWaitFragment) findFragmentByTag(LiveGameWaitFragment.class.getSimpleName());
		if (waitFragment != null) {
			waitFragment.onLiveServiceConnected();
		}
		//executePausedActivityLiveEvents();
	}

	public LiveChessService getLiveService() {
		return liveService;
	}
}
