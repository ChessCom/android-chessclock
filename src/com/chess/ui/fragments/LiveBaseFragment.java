package com.chess.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.chess.R;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.lcc.android.DataNotValidException;
import com.chess.lcc.android.LccHelper;
import com.chess.lcc.android.LiveConnectionHelper;
import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.live.client.Game;
import com.chess.model.GameLiveItem;
import com.chess.ui.activities.LiveBaseActivity;
import com.chess.ui.fragments.live.GameLiveFragment;
import com.chess.ui.fragments.live.GameLiveFragmentTablet;
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
	protected LiveConnectionHelper liveHelper;
	protected boolean isLCSBound;
	protected GameTaskListener gameTaskListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		liveBaseActivity = (LiveBaseActivity) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameTaskListener = new GameTaskListener();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (getDataHolder().isLiveChess()) {
			setLCSBound(liveBaseActivity.isLCSBound()); // it also updated when fragment returns from back stack
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (getDataHolder().isLiveChess()) {
			setLCSBound(liveBaseActivity.isLCSBound());
		}
		// update state of inherited fragments re-registering services
		if (isLCSBound) {
			LiveConnectionHelper liveHelper;
			try {
				liveHelper = getLiveHelper();
				liveHelper.setLccEventListener(this);
				liveHelper.setGameTaskListener(gameTaskListener);
			} catch (DataNotValidException e) {
				e.printStackTrace();
			}
		}
	}

	protected LiveConnectionHelper getLiveHelper() throws DataNotValidException {
		LiveConnectionHelper liveHelper = liveBaseActivity.getLiveHelper();
		if (liveHelper == null || !liveHelper.isUserConnected()) {
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

	/*
	public void onLiveServiceConnected() {
	}

	public void onLiveServiceDisconnected() {
	}
	*/

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

	public void setLCSBound(boolean LCSBound) {
		isLCSBound = LCSBound;
		if (isLCSBound) {
			liveHelper = liveBaseActivity.getLiveHelper();
		} else {
			//onLiveServiceDisconnected();
		}
	}

	protected void logoutFromLive() {
		if (isLCSBound) {
			liveHelper.logout();
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
		}//

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
			synchronized(LccHelper.GAME_SYNC_LOCK) {
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
			}
		} else {
			createSeek();
		}

		dismissProgressDialog();
		dismissNetworkCheckDialog();
	}

	protected void dismissNetworkCheckDialog() {
		dismissFragmentDialogByTag(NETWORK_CHECK_TAG);
	}

	protected void connectLive() {

		if (!isNetworkAvailable()) {
			dismissNetworkCheckDialog();
			popupItem.setPositiveBtnId(R.string.check_connection);
			showPopupDialog(R.string.no_network, NETWORK_CHECK_TAG);
		}

		if (!isLCSBound) {
			liveBaseActivity.connectLcc();
			showPopupProgressDialog();
		}
	}
}
