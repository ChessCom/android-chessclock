package com.chess.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.DailySeekItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.views.NewGameCompView;
import com.chess.ui.views.NewGameDailyView;
import com.chess.ui.views.NewGameDefaultView;
import com.chess.ui.views.NewGameLiveView;


/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.01.13
 * Time: 9:04
 */
public class NewGamesFragment extends CommonLogicFragment implements View.OnTouchListener {

	private static final String NO_INVITED_FRIENDS_TAG = "no invited friends";
	private static final String ERROR_TAG = "send request failed popup";

	private static final int DAILY_BASE_ID = 0x00001000;
	private static final int LIVE_BASE_ID = 0x00002000;
	private static final int COMP_BASE_ID = 0x00003000;

	private final static int DAILY_RIGHT_BUTTON_ID = DAILY_BASE_ID + NewGameDailyView.RIGHT_BUTTON_ID;
	private final static int DAILY_LEFT_BUTTON_ID = DAILY_BASE_ID + NewGameDefaultView.LEFT_BUTTON_ID;
	private final static int DAILY_PLAY_BUTTON_ID = DAILY_BASE_ID + NewGameDefaultView.PLAY_BUTTON_ID;

	private final static int LIVE_LEFT_BUTTON_ID = LIVE_BASE_ID + NewGameDefaultView.LEFT_BUTTON_ID;
	private final static int LIVE_PLAY_BUTTON_ID = LIVE_BASE_ID + NewGameDefaultView.PLAY_BUTTON_ID;

	private final static int COMP_LEFT_BUTTON_ID = COMP_BASE_ID + NewGameDefaultView.LEFT_BUTTON_ID;
	private final static int COMP_PLAY_BUTTON_ID = COMP_BASE_ID + NewGameDefaultView.PLAY_BUTTON_ID;

	private NewGameDailyView dailyGamesSetupView;
	private NewGameLiveView liveGamesSetupView;
	private NewGameCompView compGamesSetupView;
	private CreateChallengeUpdateListener createChallengeUpdateListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_new_games_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setupNewGameViews(view);
		createChallengeUpdateListener = new CreateChallengeUpdateListener();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		int id = v.getId();
		if (id == DAILY_RIGHT_BUTTON_ID) {
		} else if (id == DAILY_LEFT_BUTTON_ID) {
		} else if (id == DAILY_PLAY_BUTTON_ID) {
			createDailyChallenge();


		} else if (id == LIVE_LEFT_BUTTON_ID) {
		} else if (id == LIVE_PLAY_BUTTON_ID) {


			createLiveChallenge();
		} else if (id == COMP_LEFT_BUTTON_ID) {

		} else if (id == COMP_PLAY_BUTTON_ID) {
			compGamesSetupView.getNewCompGameConfig();
		}

	}

	private void createDailyChallenge() {
		// create challenge using formed configuration
		NewGameDailyView.NewDailyGameConfig newDailyGameConfig = dailyGamesSetupView.getNewDailyGameConfig();

		int color = newDailyGameConfig.getUserColor();
		int days = newDailyGameConfig.getDaysPerMove();
		int gameType = newDailyGameConfig.getGameType();
		String isRated = newDailyGameConfig.isRated() ? RestHelper.V_TRUE : RestHelper.V_FALSE;
		String opponentName = newDailyGameConfig.getOpponentName();

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_SEEKS);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
		loadItem.addRequestParams(RestHelper.P_DAYS_PER_MOVE, days);
		loadItem.addRequestParams(RestHelper.P_USER_SIDE, color);
		loadItem.addRequestParams(RestHelper.P_IS_RATED, isRated);
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, gameType);
		if (!TextUtils.isEmpty(opponentName)) {
			loadItem.addRequestParams(RestHelper.P_OPPONENT, opponentName);
		}

		new RequestJsonTask<DailySeekItem>(createChallengeUpdateListener).executeTask(loadItem);
	}

	private void createLiveChallenge() { // TODO
		// create new live game with defined parameters
//		liveGamesSetupView.getNewLiveGameConfig();

//		Challenge challenge = LiveChessClientFacade.createCustomSeekOrChallenge(
//				getLccHolder().getUser(),
//				friendsSpinner.getSelectedItem().toString().trim(),
//				pieceColor, rated, gameTimeConfig,
//				minMembershipLevel, minRating, maxRating);
//
//		// todo: refactor with new LCC
//		if(!getLccHolder().isConnected() || getLccHolder().getClient() == null){ // TODO should leave that screen on connection lost or when LCC is become null
//			getLccHolder().logout();
//			backToHomeActivity();
//			return;
//		}
//
//		FlurryAgent.logEvent(FlurryData.CHALLENGE_CREATED);
//		challengeTaskRunner.runSendChallengeTask(challenge);

	}

	private class CreateChallengeUpdateListener extends ActionBarUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(getInstance(), DailySeekItem.class);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			showSinglePopupDialog(R.string.congratulations, R.string.onlinegamecreated);
		}

		@Override
		public void errorHandle(String resultMessage) {
			showPopupDialog(getString(R.string.error), resultMessage, ERROR_TAG);
		}
	}

	private void setupNewGameViews(View view) {
		{// Daily Games setup
			dailyGamesSetupView = (NewGameDailyView) view.findViewById(R.id.dailyGamesSetupView);

			NewGameDefaultView.ViewConfig dailyConfig = new NewGameDefaultView.ViewConfig();
			dailyConfig.setBaseId(DAILY_BASE_ID);
			dailyConfig.setHeaderIcon(R.drawable.ic_daily_game);
			dailyConfig.setHeaderText(R.string.new_daily_chess);
			dailyConfig.setTitleText(R.string.new_per_turn);
			// set default value
			{// TODO remove after debug - we set here a test value
				AppData.setDefaultDailyMode(getActivity(), 3);
			}
			int defaultDailyMode = AppData.getDefaultDailyMode(getActivity());
			dailyConfig.setLeftButtonText(getString(R.string.days_arg, defaultDailyMode));
			dailyConfig.setRightButtonTextId(R.string.random);

			dailyGamesSetupView.setConfig(dailyConfig);
			dailyGamesSetupView.findViewById(DAILY_RIGHT_BUTTON_ID).setOnClickListener(this);
			dailyGamesSetupView.findViewById(DAILY_LEFT_BUTTON_ID).setOnClickListener(this);
			dailyGamesSetupView.findViewById(DAILY_PLAY_BUTTON_ID).setOnClickListener(this);
		}

		{// Live Games setup
			liveGamesSetupView = (NewGameLiveView) view.findViewById(R.id.liveGamesSetupView);

			NewGameDefaultView.ViewConfig liveConfig = new NewGameDefaultView.ViewConfig();
			liveConfig.setBaseId(LIVE_BASE_ID);
			liveConfig.setHeaderIcon(R.drawable.ic_live_game);
			liveConfig.setHeaderText(R.string.new_live_chess);
			liveConfig.setTitleText(R.string.new_time);
			// set default value
			// TODO remove after debug - we set here a test value
			{
				int initialTime = 13;
				int bonusTime = 0;
				int defaultLiveMode = initialTime | (bonusTime << 8);

				AppData.setDefaultLiveyMode(getActivity(), defaultLiveMode);
			}


			int defaultLiveMode = AppData.getDefaultLiveMode(getActivity());
			int initialTime = defaultLiveMode & 0xFF;
			int bonusTime = defaultLiveMode >> 8;

			if (bonusTime == 0) {
				liveConfig.setLeftButtonText(getString(R.string.min_arg, initialTime));
			} else {
				liveConfig.setLeftButtonText(String.valueOf(initialTime) + " | " + bonusTime);
			}

			liveGamesSetupView.setConfig(liveConfig);
			liveGamesSetupView.findViewById(LIVE_LEFT_BUTTON_ID).setOnClickListener(this);
			liveGamesSetupView.findViewById(LIVE_PLAY_BUTTON_ID).setOnClickListener(this);
		}

		{// Comp Games setup
			compGamesSetupView = (NewGameCompView) view.findViewById(R.id.compGamesSetupView);

			NewGameDefaultView.ViewConfig compConfig = new NewGameDefaultView.ViewConfig();
			compConfig.setBaseId(COMP_BASE_ID);
			compConfig.setHeaderIcon(R.drawable.ic_comp_game);
			compConfig.setHeaderText(R.string.new_comp_chess);
			compConfig.setTitleText(R.string.new_difficulty);
			compConfig.setLeftButtonTextId(R.string.days); // TODO set properly

			compGamesSetupView.setConfig(compConfig);
			compGamesSetupView.findViewById(COMP_LEFT_BUTTON_ID).setOnClickListener(this);
			compGamesSetupView.findViewById(COMP_PLAY_BUTTON_ID).setOnClickListener(this);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d("TEST", "event " + event.getX());

		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
