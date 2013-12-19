package com.chess.ui.fragments.live;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.backend.LiveChessService;
import com.chess.lcc.android.DataNotValidException;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.LiveBaseFragment;
import com.chess.utilities.LogMe;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.04.13
 * Time: 9:30
 */
public class LiveGameWaitFragment extends LiveBaseFragment {

	private static final long FINISH_FRAGMENT_DELAY = 200;

	private View loadingView;
	private LiveGameConfig liveGameConfig;
	public boolean closeOnResume;

	public LiveGameWaitFragment() { }

	public static LiveGameWaitFragment createInstance(LiveGameConfig config) {
		LiveGameWaitFragment fragment = new LiveGameWaitFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable(CONFIG, config);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			liveGameConfig = getArguments().getParcelable(CONFIG);
		} else if (savedInstanceState != null) { // we start this fragment from broadcast receiver while waiting live game to be created, so no config here.
			liveGameConfig = savedInstanceState.getParcelable(CONFIG);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_live_game_wait_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.live_chess);

		loadingView = view.findViewById(R.id.loadingView);
		view.findViewById(R.id.cancelLiveBtn).setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!closeOnResume) {
			getAppData().setLiveChessMode(true);
			liveBaseActivity.connectLcc();
			loadingView.setVisibility(View.VISIBLE);
		} else {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					getActivityFace().showPreviousFragment();
				}
			}, FINISH_FRAGMENT_DELAY);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(CONFIG, liveGameConfig);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.cancelLiveBtn) {

			LiveChessService liveService;
			try {
				liveService = getLiveService();
			} catch (DataNotValidException e) {
				logTest(e.getMessage());
				getActivityFace().showPreviousFragment();
				return;
			}
			// todo: check - probably cancel only latest issued challenge
			liveService.cancelAllOwnChallenges();

			getActivityFace().showPreviousFragment();
		}
	}

	@Override
	public void startGameFromService() {
		LogMe.dl("lcc", "startGameFromService");

		final Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					LiveChessService liveService;
					try {
						liveService = getLiveService();
					} catch (DataNotValidException e) {
						logTest(e.getMessage());
						showToast(e.getMessage() + " go back");
						getActivityFace().showPreviousFragment();   // TODO handle correctly
						return;
					}
					loadingView.setVisibility(View.GONE);
					logTest("challenge created, ready to start");

					Long gameId = liveService.getCurrentGameId();
					logTest("gameId = " + gameId);
					getActivityFace().openFragment(GameLiveFragment.createInstance(gameId));

					closeOnResume = true;
				}
			});
		}
	}

	@Override
	public void createSeek() {
		if (liveGameConfig != null) {
			LiveChessService liveService;
			try {
				liveService = getLiveService();
			} catch (DataNotValidException e) {
				logTest(e.getMessage());
				getActivityFace().showPreviousFragment();
				return;
			}
			liveService.createChallenge(liveGameConfig);
		}
	}

	@Override
	public void onChallengeRejected(String by) {
		super.onChallengeRejected(by);
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActivityFace().showPreviousFragment();
			}
		});
	}
}
