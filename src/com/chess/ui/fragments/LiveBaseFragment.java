package com.chess.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.chess.backend.LiveChessService;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.lcc.android.DataNotValidException;
import com.chess.lcc.android.LccHelper;
import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.live.client.Game;
import com.chess.model.GameLiveItem;
import com.chess.ui.activities.LiveBaseActivity;
import com.chess.ui.fragments.live.GameLiveFragment;
import com.chess.utilities.LogMe;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.04.13
 * Time: 13:06
 */
public class LiveBaseFragment extends CommonLogicFragment implements LccEventListener {

	protected LiveBaseActivity liveBaseActivity;
	private LiveChessService liveService;
	protected boolean isLCSBound;
	protected GameTaskListener gameTaskListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		liveBaseActivity = (LiveBaseActivity) activity;
		if (getAppData().isLiveChess()) {
			if (liveBaseActivity.isLCSBound()) {
				isLCSBound = true;
				liveService = liveBaseActivity.getLiveService();
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameTaskListener = new GameTaskListener();
	}

	protected LiveChessService getLiveService() throws DataNotValidException {
		LiveChessService service = liveBaseActivity.getLiveService();
		if (service == null || !service.isUserConnected()) {
			if (service == null) {
				throw new DataNotValidException(DataNotValidException.SERVICE_NULL);
			} else if (service.getLccHelper() == null) {
				throw new DataNotValidException(DataNotValidException.LCC_HELPER_NULL);
			} else if (service.getUser() == null) {
				throw new DataNotValidException(DataNotValidException.USER_NULL);
			} else /*if (!service.isConnected())*/ {
				throw new DataNotValidException(DataNotValidException.NOT_CONNECTED);
			}
		} else {
			return service;
		}
	}

	public void onLiveServiceConnected() {
		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (DataNotValidException e) {
			logTest(e.getMessage());
			showToast(e.getMessage());
			backToHomeFragment();
			return;
		}
		liveService.setLccEventListener(this);
		liveService.setGameTaskListener(gameTaskListener);

		if (liveService.isActiveGamePresent()) {
			synchronized(LccHelper.LOCK) {
				liveService.processFullGame();
			}
		} else {
			createSeek();
		}
	}

	public void onLiveServiceDisconnected() {
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
	public void onGameEnd(String gameEndMessage) {
	}

	@Override
	public void onInform(String title, String message) {
	}

//	@Override
//	public void onGameRecreate() {
//	}

	@Override
	public void startGameFromService() {
		LogMe.dl("lcc", "startGameFromService");

		final FragmentActivity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					LiveChessService liveService;
					try {
						liveService = getLiveService();
					} catch (DataNotValidException e) {
						logTest(e.getMessage());
						getActivityFace().showPreviousFragment();   // TODO handle correctly
						return;
					}
					loadingView.setVisibility(View.GONE);
					logTest("challenge created, ready to start");

					Long gameId = liveService.getCurrentGameId();
					logTest("gameId = " + gameId);
					getActivityFace().openFragment(GameLiveFragment.createInstance(gameId));

//					closeOnResume = true;
				}
			});
		}
	}

	@Override
	public void createSeek() {

	}

	@Override
	public void expireGame() {
	}

	public void setLCSBound(boolean LCSBound) {
		isLCSBound = LCSBound;
		if (isLCSBound) {
			liveService = liveBaseActivity.getLiveService();
		} else {
			onLiveServiceDisconnected();
		}
	}

	protected void logoutFromLive() {
		if (isLCSBound) {
			liveService.logout();
			liveBaseActivity.unBindLiveService();
		}
	}

	protected class GameTaskListener extends ActionBarUpdateListener<Game> {
		public GameTaskListener() {
			super(getInstance());
		}
	}

}
