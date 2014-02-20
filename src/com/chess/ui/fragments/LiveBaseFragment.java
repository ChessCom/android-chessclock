package com.chess.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import com.chess.R;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.lcc.android.DataNotValidException;
import com.chess.lcc.android.LiveConnectionHelper;
import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.live.client.Game;
import com.chess.model.DataHolder;
import com.chess.model.GameLiveItem;
import com.chess.ui.activities.LiveBaseActivity;
import com.chess.ui.fragments.live.*;
import com.chess.utilities.LogMe;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.04.13
 * Time: 13:06
 */
public abstract class LiveBaseFragment extends CommonLogicFragment implements LccEventListener {

	private static final String TAG = "LccLog-LiveBaseFragment";

	protected LiveBaseActivity liveBaseActivity;
	protected boolean isLCSBound;
	protected GameTaskListener gameTaskListener;
	protected boolean isLiveFragment;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		liveBaseActivity = (LiveBaseActivity) activity;

		isLiveFragment = liveBaseActivity.isLiveFragment(getClass().getSimpleName());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameTaskListener = new GameTaskListener();
	}

	@Override
	public void onResume() {
		super.onResume();

		updateLCSBoundState();

		// probably we have to create some common way how to determine "embedded" fragments
		// (for example LiveChat fragment embedded into Live Game and Observed fragments for tablets,
		// and we should not setLccEventListener(this) for such embedded fragment)
		if (this instanceof LiveChatFragment && isTablet && !inPortrait()) {
			return;
		}

		if (isLiveFragment) {
			setLCSBound(liveBaseActivity.isLCSBound());
			DataHolder.getInstance().setLiveChessMode(true);

			LiveConnectionHelper liveHelper;
			try {
				liveHelper = getLiveHelper();
				liveHelper.stopIdleTimeOutCounter();

				if (isLCSBound) {
					// update state of inherited fragments re-registering services
					liveHelper.setLccEventListener(this);
					liveHelper.setGameTaskListener(gameTaskListener);
				}
			} catch (DataNotValidException e) {
				LogMe.dl(TAG, e.getMessage());
			}

			if (!isLCSBound) {

				if (!isNetworkAvailable()) {
					dismissNetworkCheckDialog();
					popupItem.setPositiveBtnId(R.string.check_connection);
					showPopupDialog(R.string.no_network, NETWORK_CHECK_TAG);
				}

				liveBaseActivity.performServiceConnection();
				showPopupProgressDialog();
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		dismissNetworkCheckDialog();
	}

	protected LiveConnectionHelper getLiveHelper() throws DataNotValidException {
		LiveConnectionHelper liveHelper = liveBaseActivity.getLiveHelper();
		// I think we should not check for "isConnected" case here, because Live can be in "reconnecting" state
		// and isConnected=false; for example we still should be able to call getLiveHelper().stopIdleTimer() when
		// user returns to fragment even if lcc is in reconnecting state that time
		if (liveHelper == null || liveHelper.getUser() == null /*|| !liveHelper.isUserConnected()*/) {
			if (liveHelper == null) {
				throw new DataNotValidException(DataNotValidException.SERVICE_NULL);
			} else if (liveHelper.getLccHelper() == null) {
				throw new DataNotValidException(DataNotValidException.LCC_HELPER_NULL);
			} else if (liveHelper.getUser() == null) {
				throw new DataNotValidException(DataNotValidException.USER_NULL);
			} else /*if (!liveHelper.isConnected())*/ {
				throw new DataNotValidException(DataNotValidException.NOT_CONNECTED);
			}
		} else {
			return liveHelper;
		}
	}

	public void onLiveServiceConnected() {
		setLCSBound(true);
	}

	public void onLiveServiceDisconnected() {
		setLCSBound(false);
	}

	@Override
	public void setWhitePlayerTimer(String timer) {
	}

	@Override
	public void setBlackPlayerTimer(String timer) {
	}

	@Override
	public void onGameRefresh(GameLiveItem gameItem) {
	}

	@Override
	public void onDrawOffered(String drawOfferUsername) {
	}

	@Override
	public void onGameEnd(Game game, String gameEndMessage) {
	}

	@Override
	public void onInform(String title, String message) {
	}

	@Override
	public void startGameFromService() {
	}

	@Override
	public void createSeek() {
	}

	@Override
	public void onClockFinishing() {
	}

	@Override
	public void showPiecesMovesAnimation(boolean show) {

	}

	@Override
	public void onFriendsStatusChanged() {
	}

	@Override
	public void onChallengeRejected(final String by) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showToast(getString(R.string.challengeRejected, by)); // todo: show dialog popup with OK button if necessary
			}
		});
	}

	@Override
	public void expireGame() {
	}

	@Override
	public void updateOpponentOnlineStatus(boolean online) {
	}

	protected void updateLCSBoundState() {
		isLCSBound = liveBaseActivity.isLCSBound();
	}

	public void setLCSBound(boolean LCSBound) {
		//LogMe.dl(TAG, "fragment setLCSBound=" + LCSBound);
		isLCSBound = LCSBound;
	}

	protected void logoutFromLive() {
		if (isLCSBound) {

			try {
				LiveConnectionHelper liveHelper = getLiveHelper();
				liveHelper.logout();
			} catch (DataNotValidException e) {
				LogMe.dl(TAG, e.getMessage());
			}

			liveBaseActivity.unBindAndStopLiveService();
		}
	}

	protected class GameTaskListener extends ActionBarUpdateListener<Game> {
		public GameTaskListener() {
			super(getInstance());
		}
	}

	public void onLiveClientConnected() {

		// onAttach
		liveBaseActivity = (LiveBaseActivity) getActivity();
		if (getDataHolder().isLiveChess()) {
			setLCSBound(liveBaseActivity.isLCSBound());
		}

		LiveConnectionHelper liveHelper;
		try {
			liveHelper = getLiveHelper();
		} catch (DataNotValidException e) {
			LogMe.dl(TAG, e.getMessage());
			return;
		}
		liveHelper.setLccEventListener(this);
		liveHelper.setGameTaskListener(gameTaskListener);

		if (liveHelper.isActiveGamePresent() && !liveHelper.isCurrentGameObserved()) {
			Long gameId = liveHelper.getCurrentGameId();

			GameLiveFragment liveFragment = liveBaseActivity.getGameLiveFragment();
			if (liveFragment == null) {
				if (!isTablet) {
					liveFragment = GameLiveFragment.createInstance(gameId);
				} else {
					liveFragment = GameLiveFragmentTablet.createInstance(gameId);
				}
			} else {
				liveFragment.invalidateGameScreen();
			}
//				getActivityFace().openFragment(liveFragment, true);
			getActivityFace().openFragment(liveFragment);
		} else {
			createSeek();
		}

		dismissProgressDialog();
		dismissNetworkCheckDialog();
	}

	protected void dismissNetworkCheckDialog() {
		dismissFragmentDialogByTag(NETWORK_CHECK_TAG);
	}

	protected void openLiveFragment(LiveConnectionHelper liveHelper) {

		Long gameId = liveHelper.getCurrentGameId();

		if (gameId == null || getActivity() == null) { // check, getActivity() == null when side fragment is open and we return from background
			LogMe.dl("DEBUG: gameId=null !");
			return;
		}

		GameLiveFragment gameLiveFragment;
		if (liveHelper.isCurrentGameObserved()) {
			gameLiveFragment = ((LiveBaseActivity) getActivity()).getGameLiveObserverFragment();
			if (gameLiveFragment == null) {
				if (isTablet) {
					gameLiveFragment = GameLiveObserveFragmentTablet.createInstance(gameId);
				} else {
					gameLiveFragment = GameLiveObserveFragment.createInstance(gameId);
				}
			}
		} else {
			gameLiveFragment = ((LiveBaseActivity) getActivity()).getGameLiveFragment();
			if (gameLiveFragment == null) {
				if (isTablet) {
					gameLiveFragment = GameLiveFragmentTablet.createInstance(gameId);
				} else {
					gameLiveFragment = GameLiveFragment.createInstance(gameId);
				}
			}
		}
		if (gameLiveFragment != null) {
			getActivityFace().openFragment(gameLiveFragment);
		}
	}
}
