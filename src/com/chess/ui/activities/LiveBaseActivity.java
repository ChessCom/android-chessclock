package com.chess.ui.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
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
import com.chess.lcc.android.*;
import com.chess.lcc.android.interfaces.LiveChessClientEventListenerFace;
import com.chess.live.client.Challenge;
import com.chess.live.client.Game;
import com.chess.live.util.GameTimeConfig;
import com.chess.model.PopupItem;
import com.chess.ui.fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.PopupDialogFragment;
import com.facebook.android.LoginButton;

import java.util.Map;

/**
 * LiveBaseActivity class
 *
 * @author alien_roger
 * @created at: 11.04.12 9:00
 */
public abstract class LiveBaseActivity extends CoreActivityActionBar implements LiveChessClientEventListenerFace {

	private static final String TAG = "LiveBaseActivity";

	protected static final String CHALLENGE_TAG = "challenge_tag";
	protected static final String LOGOUT_TAG = "logout_tag";
	private static final String CONNECT_FAILED_TAG = "connect_failed";
	private static final String OBSOLETE_VERSION_TAG = "obsolete version";

	protected LiveOuterChallengeListener outerChallengeListener;
	protected Challenge currentChallenge;
	protected LccChallengeTaskRunner challengeTaskRunner;
	protected ChallengeTaskListener challengeTaskListener;
	protected GameTaskListener gameTaskListener;
	private LccHolder lccHolder;
	private Menu menu;
	private LiveChessServiceConnectionListener liveChessServiceConnectionListener;
	protected boolean isLCSBound;
	//private boolean shouldBeConnectedToLive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		challengeTaskListener = new ChallengeTaskListener();
		gameTaskListener = new GameTaskListener();

		/*gameTaskRunner = new LccGameTaskRunner(gameTaskListener, lccHolder);
		challengeTaskRunner = new LccChallengeTaskRunner(challengeTaskListener, lccHolder);
		lccHolder.setLiveChessClientEventListener(this);*/

		outerChallengeListener = new LiveOuterChallengeListener();

		liveChessServiceConnectionListener = new LiveChessServiceConnectionListener();
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (!isLCSBound) {
			bindService(new Intent(this, LiveChessService.class), liveChessServiceConnectionListener, BIND_AUTO_CREATE);
		}

		if (lccHolder != null) {
			lccHolder.setLiveChessClientEventListener(this);
			lccHolder.setOuterChallengeListener(outerChallengeListener);
			challengeTaskRunner = new LccChallengeTaskRunner(challengeTaskListener, lccHolder);
		}

		/*if (AppData.isLiveChess(this) && !AppUtils.isNetworkAvailable(this)) { // check only if live
			popupItem.setPositiveBtnId(R.string.wireless_settings);
			showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
		} else {
			//LccHolder.getInstance(this).checkAndConnect();
			shouldBeConnectedToLive = true;
		}*/
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(liveChessServiceConnectionListener);
	}

	protected boolean checkIfLiveUserAlive(){
		boolean alive = true;
		if (getLccHolder().getUser() == null) {
			if (AppData.isLiveChess(this)) {
				getLccHolder().logout();
			}
			backToHomeActivity();
			alive = false;
		}
		return alive;
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (lccHolder != null) {
			getActionBarHelper().showMenuItemById(R.id.menu_signOut, lccHolder.isConnected(), menu);
			getActionBarHelper().showMenuItemById(R.id.menu_signOut, lccHolder.isConnected());
			executePausedActivityLiveEvents();
		}
	}

	@Override
	protected void adjustActionBar() {
		super.adjustActionBar();
		boolean isConnected = getLccHolder() != null && getLccHolder().isConnected();
		if (getLccHolder() != null) {
			getActionBarHelper().showMenuItemById(R.id.menu_signOut, isConnected);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		dismissFragmentDialog();
	}

	public void executePausedActivityLiveEvents() {

		Map<LiveEvent.Event, LiveEvent> pausedActivityLiveEvents = getLccHolder().getPausedActivityLiveEvents();
		Log.d("LCCLOG", "executePausedActivityLiveEvents size=" + pausedActivityLiveEvents.size() + ", events=" + pausedActivityLiveEvents);

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
					outerChallengeListener.showDelayedDialog(challengeEvent.getChallenge());
				} else {
					outerChallengeListener.showDialog(challengeEvent.getChallenge());
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
				getLccHolder().logout();
			}
			backToHomeActivity();
		} else if (tag.equals(OBSOLETE_VERSION_TAG)) {
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

			backToHomeActivity();

		} else if (tag.equals(LOGOUT_TAG)) {
			getLccHolder().logout();
			backToHomeActivity();
		} else if (tag.contains(CHALLENGE_TAG)) { // Challenge accepted!
			Log.i(TAG, "Accept challenge: " + currentChallenge);
			challengeTaskRunner.declineAllChallenges(currentChallenge, getLccHolder().getChallenges());
			challengeTaskRunner.runAcceptChallengeTask(currentChallenge);
			popupManager.remove(fragment);
		} /*else if (tag.equals(NETWORK_CHECK_TAG)) {
			startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), NETWORK_REQUEST);
		}*/
		super.onPositiveBtnClick(fragment);
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onNegativeBtnClick(fragment);
			return;
		}

		if (tag.equals(CHALLENGE_TAG)) {// Challenge declined!
			Log.i(TAG, "Decline challenge: " + currentChallenge);
			challengeTaskRunner.declineCurrentChallenge(currentChallenge, getLccHolder().getChallenges());
			popupManager.remove(fragment);
		}
		super.onNegativeBtnClick(fragment);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		/*if (resultCode == RESULT_OK && requestCode == NETWORK_REQUEST) {
			//LccHolder.getInstance(this).checkAndConnect();
			shouldBeConnectedToLive = true;
		}*/
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

	private class LiveChessServiceConnectionListener implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			Log.d("lcclog", "SERVICE: LIVE onLiveServiceConnected");

			isLCSBound = true;

			LiveChessService.ServiceBinder serviceBinder = (LiveChessService.ServiceBinder) iBinder;
			lccHolder = serviceBinder.getLccHolder();

			getActionBarHelper().showMenuItemById(R.id.menu_signOut, lccHolder.isConnected(), menu);
			getActionBarHelper().showMenuItemById(R.id.menu_signOut, lccHolder.isConnected());

			lccHolder.setLiveChessClientEventListener(LiveBaseActivity.this);
			lccHolder.setOuterChallengeListener(outerChallengeListener);
			challengeTaskRunner = new LccChallengeTaskRunner(challengeTaskListener, lccHolder);
			executePausedActivityLiveEvents();

			onLiveServiceConnected();

			/*if (shouldBeConnectedToLive) {
				getLccHolder().getService().checkAndConnect();
			}
			*/
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Log.d(TAG, "SERVICE: onServiceDisconnected");
			isLCSBound = false;
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
					.append(challenge.getFrom().getUsername())
					.append(getString(R.string.time_)).append(StaticData.SYMBOL_SPACE)
					.append(timeMode)
					.append(getString(R.string.you_play)).append(StaticData.SYMBOL_SPACE)
					.append(playerColor).append(StaticData.SYMBOL_NEW_STR)
					.append(rated)
					.toString();
		}
	}

	private class ChallengeTaskListener extends ActionBarUpdateListener<Challenge> {
		public ChallengeTaskListener() {
			super(getInstance());
		}

		@Override
		public void updateData(Challenge returnedObj) {
			super.updateData(returnedObj);
			challengeTaskUpdated(returnedObj);
		}
	}

	protected void challengeTaskUpdated(Challenge challenge){

	}

	private class GameTaskListener extends ActionBarUpdateListener<Game> {
		public GameTaskListener() {
			super(getInstance());
		}
	}

	protected LccHolder getLccHolder() {
		return lccHolder;
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

				getLccHolder().logout();

				((TextView) customView.findViewById(R.id.titleTxt)).setText(message);

				EditText usernameEdt = (EditText) customView.findViewById(R.id.usernameEdt);
				EditText passwordEdt = (EditText) customView.findViewById(R.id.passwordEdt);
				setLoginFields(usernameEdt, passwordEdt);

				customView.findViewById(R.id.re_signin).setOnClickListener(LiveBaseActivity.this);

				LoginButton facebookLoginButton = (LoginButton) customView.findViewById(R.id.re_fb_connect);
				facebookInit(facebookLoginButton);
				facebookLoginButton.logout();

				usernameEdt.setText(AppData.getUserName(LiveBaseActivity.this));
			}
		});
	}

	@Override
	public void onConnectionFailure(String message) {
		if (isPaused) {
			LiveEvent connectionFailureEvent = new LiveEvent();
			connectionFailureEvent.setEvent(LiveEvent.Event.CONNECTION_FAILURE);
			connectionFailureEvent.setMessage(message);
			getLccHolder().getPausedActivityLiveEvents().put(connectionFailureEvent.getEvent(), connectionFailureEvent);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		this.menu = menu;
		boolean isConnected = getLccHolder() != null && getLccHolder().isConnected();
		if (getLccHolder() != null) {
			getActionBarHelper().showMenuItemById(R.id.menu_signOut, isConnected, menu);
		}
		return result;
	}

	protected void onLiveServiceConnected() {
	}
}
