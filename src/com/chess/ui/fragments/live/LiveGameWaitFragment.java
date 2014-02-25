package com.chess.ui.fragments.live;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.lcc.android.DataNotValidException;
import com.chess.lcc.android.LiveConnectionHelper;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.LiveBaseFragment;
import com.chess.utilities.AppUtils;
import com.chess.utilities.LogMe;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.04.13
 * Time: 9:30
 */
public class LiveGameWaitFragment extends LiveBaseFragment {

	private static final String TAG = "LccLog-LiveGameWaitFragment";

	private static final long FINISH_FRAGMENT_DELAY = 200;

	protected static final String CHALLENGE_CREATED = "challenge_created";
	protected static final String CLOSE_ON_RESUME = "close_on_resume";

	private View loadingView;
	private LiveGameConfig liveGameConfig;
	//public boolean closeOnResume; // it was not safe here, because device can be rotated and lost this state

	public LiveGameWaitFragment() {
		Bundle bundle = new Bundle();
		setArguments(bundle);
	}

	public static LiveGameWaitFragment createInstance(LiveGameConfig config) {
		LiveGameWaitFragment fragment = new LiveGameWaitFragment();
		fragment.getArguments().putParcelable(CONFIG, config);
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
		return inflater.inflate(R.layout.live_game_wait_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.live_chess);

		loadingView = view.findViewById(R.id.loadingView);
		view.findViewById(R.id.cancelLiveBtn).setOnClickListener(this);

		if (AppUtils.isNexus4Kind(getActivity())) {
			view.findViewById(R.id.stubView).setVisibility(View.GONE);
			view.findViewById(R.id.stubControls).setVisibility(View.GONE);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!getArguments().getBoolean(CLOSE_ON_RESUME)) {
			getDataHolder().setLiveChessMode(true);
			// check is we really should perform it here
			isLCSBound = liveBaseActivity.connectToLiveChess(/*this*/);
			liveBaseActivity.setLiveFragmentFace(this);
			loadingView.setVisibility(View.VISIBLE);
			//
		} else {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					showPreviousFragmentSafe();
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

			LiveConnectionHelper liveHelper;
			try {
				liveHelper = getLiveHelper();
			} catch (DataNotValidException e) {
				logTest(e.getMessage());
				getActivityFace().showPreviousFragment();
				return;
			}
			// todo: check - probably cancel only latest issued challenge
			liveHelper.cancelAllOwnChallenges();

			getActivityFace().showPreviousFragment();
		}
	}

	@Override
	public void onLiveClientConnected() {
		super.onLiveClientConnected();

		LiveConnectionHelper liveHelper;
		try {
			liveHelper = getLiveHelper();
		} catch (DataNotValidException e) {
			LogMe.dl(TAG, e.getMessage());
			return;
		}

		if (!getArguments().getBoolean(CHALLENGE_CREATED) && !liveHelper.isUserPlaying()) {
			createSeek();
			getArguments().putBoolean(CHALLENGE_CREATED, true);
		}
	}

	@Override
	public void startGameFromService() {
		LogMe.dl(TAG, "startGameFromService");

		final Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					LiveConnectionHelper liveHelper;
					try {
						liveHelper = getLiveHelper();
					} catch (DataNotValidException e) {
						logTest(e.getMessage());
						getActivityFace().showPreviousFragment();   // TODO handle correctly
						return;
					}
					loadingView.setVisibility(View.GONE);
					logTest("challenge created, ready to start");

					Long gameId = liveHelper.getCurrentGameId();
					logTest("gameId = " + gameId);

					GameLiveFragment liveFragment = liveBaseActivity.getGameLiveFragment();
					if (liveFragment == null) {
						if (!isTablet) {
							liveFragment = GameLiveFragment.createInstance(gameId);
						} else {
							liveFragment = GameLiveFragmentTablet.createInstance(gameId);
						}
					}
//					getActivityFace().openFragment(liveFragment, true);
					getActivityFace().openFragment(liveFragment);

					getArguments().putBoolean(CLOSE_ON_RESUME, true);
				}
			});
		}
	}

	@Override
	public void createSeek() {
		if (liveGameConfig != null) {
			LiveConnectionHelper liveHelper;
			try {
				liveHelper = getLiveHelper();
			} catch (DataNotValidException e) {
				logTest(e.getMessage());
				showPreviousFragmentSafe();
				return;
			}
			liveHelper.createChallenge(liveGameConfig);
		}
	}

	@Override
	public void onChallengeRejected(String by) {
		super.onChallengeRejected(by);
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showPreviousFragmentSafe();
			}
		});
	}
}
